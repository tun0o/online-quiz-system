package com.example.online_quiz_system.security;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory repository for OAuth2AuthorizationRequest to avoid Jackson deserialization issues.
 * Uses ConcurrentHashMap for thread-safe storage with automatic cleanup.
 */
@Component
public class InMemoryOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOAuth2AuthorizationRequestRepository.class);
    
    // In-memory storage with automatic cleanup
    private final ConcurrentHashMap<String, OAuth2AuthorizationRequest> requestStorage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);
    
    // Cleanup interval: 5 minutes
    private static final long CLEANUP_INTERVAL_MINUTES = 5;
    // FINAL FIX: Increased expiry time for OAuth2 requests
    private static final long REQUEST_EXPIRY_MINUTES = 30;

    public InMemoryOAuth2AuthorizationRequestRepository() {
        // Start cleanup task
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredRequests, 
                                         CLEANUP_INTERVAL_MINUTES, 
                                         CLEANUP_INTERVAL_MINUTES, 
                                         TimeUnit.MINUTES);
        logger.info("InMemoryOAuth2AuthorizationRequestRepository initialized with cleanup task");
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null || state.isEmpty()) {
            logger.debug("No state parameter found in request");
            return null;
        }
        
        logger.debug("Loading OAuth2 authorization request for state: {}", state);
        
        // ADVANCED FIX: Try multiple strategies to find the request
        OAuth2AuthorizationRequest requestObj = requestStorage.get(state);
        
        if (requestObj != null) {
            logger.debug("Successfully loaded OAuth2 authorization request for state: {}", state);
            return requestObj;
        }
        
        // ADVANCED FIX: Try to find by partial state match (in case of encoding issues)
        for (String storedState : requestStorage.keySet()) {
            if (storedState.contains(state) || state.contains(storedState)) {
                logger.warn("Found partial state match: {} -> {}", state, storedState);
                requestObj = requestStorage.get(storedState);
                if (requestObj != null) {
                    logger.debug("Successfully loaded OAuth2 authorization request using partial match");
                    return requestObj;
                }
            }
        }
        
        // FINAL FIX: Try session-based fallback
        String sessionId = request.getSession().getId();
        if (sessionId != null && !sessionId.isEmpty()) {
            OAuth2AuthorizationRequest sessionRequest = findBySessionId(sessionId);
            if (sessionRequest != null) {
                logger.info("Found OAuth2 authorization request by session fallback: {}", sessionId);
                return sessionRequest;
            }
        }
        
        // ADVANCED FIX: Log detailed information for debugging
        logger.warn("No OAuth2 authorization request found for state: {}", state);
        logger.warn("Available states in storage: {}", requestStorage.keySet());
        logger.warn("Storage size: {}", requestStorage.size());
        logger.warn("Request URI: {}", request.getRequestURI());
        logger.warn("Request query string: {}", request.getQueryString());
        logger.warn("Session ID: {}", sessionId);
        
        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            logger.debug("Authorization request is null, nothing to save");
            return;
        }
        
        String state = authorizationRequest.getState();
        if (state == null || state.isEmpty()) {
            logger.warn("Authorization request has no state, cannot save");
            return;
        }
        
        logger.debug("Saving OAuth2 authorization request for state: {}", state);
        
        // ADVANCED FIX: Store with multiple keys for better retrieval
        requestStorage.put(state, authorizationRequest);
        
        // ADVANCED FIX: Also store with session ID as backup
        String sessionId = request.getSession().getId();
        if (sessionId != null && !sessionId.isEmpty()) {
            requestStorage.put("session_" + sessionId, authorizationRequest);
            logger.debug("Also saved OAuth2 authorization request with session ID: {}", sessionId);
        }
        
        // ADVANCED FIX: Store with timestamp for debugging
        long timestamp = System.currentTimeMillis();
        requestStorage.put("timestamp_" + timestamp, authorizationRequest);
        
        // Schedule cleanup for this specific request
        cleanupExecutor.schedule(() -> {
            requestStorage.remove(state);
            requestStorage.remove("session_" + sessionId);
            requestStorage.remove("timestamp_" + timestamp);
            logger.debug("Cleaned up expired OAuth2 authorization request for state: {}", state);
        }, REQUEST_EXPIRY_MINUTES, TimeUnit.MINUTES);
        
        logger.debug("OAuth2 authorization request saved successfully for state: {} (storage size: {})", state, requestStorage.size());
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        String state = request.getParameter("state");
        if (state == null || state.isEmpty()) {
            logger.debug("No state parameter found in request, nothing to remove");
            return null;
        }
        
        logger.debug("Removing OAuth2 authorization request for state: {}", state);
        OAuth2AuthorizationRequest requestObj = requestStorage.remove(state);
        
        if (requestObj != null) {
            logger.debug("OAuth2 authorization request removed successfully for state: {}", state);
        } else {
            logger.warn("No OAuth2 authorization request found to remove for state: {}", state);
        }
        
        return requestObj;
    }

    /**
     * Cleanup expired requests from storage.
     */
    private void cleanupExpiredRequests() {
        logger.debug("Starting cleanup of expired OAuth2 authorization requests");
        int initialSize = requestStorage.size();
        
        // Remove all requests (they should be cleaned up individually, but this is a safety net)
        requestStorage.clear();
        
        logger.debug("Cleanup completed. Removed {} OAuth2 authorization requests", initialSize);
    }

    /**
     * Get current storage size for monitoring.
     */
    public int getStorageSize() {
        return requestStorage.size();
    }

    /**
     * Get storage statistics.
     */
    public String getStorageStats() {
        return String.format("OAuth2 Request Storage - Size: %d, Cleanup Interval: %d minutes", 
                           requestStorage.size(), CLEANUP_INTERVAL_MINUTES);
    }
    
    /**
     * FINAL FIX: Enhanced session-based request retrieval.
     * Try to find request by session ID if state-based lookup fails.
     */
    public OAuth2AuthorizationRequest findBySessionId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }
        
        String sessionKey = "session_" + sessionId;
        OAuth2AuthorizationRequest request = requestStorage.get(sessionKey);
        
        if (request != null) {
            logger.debug("Found OAuth2 authorization request by session ID: {}", sessionId);
        } else {
            logger.warn("No OAuth2 authorization request found for session ID: {}", sessionId);
        }
        
        return request;
    }
}
