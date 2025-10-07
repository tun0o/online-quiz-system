package com.example.online_quiz_system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Debug controller for OAuth2 session issues.
 */
@RestController
@RequestMapping("/api/oauth2/debug")
public class OAuth2SessionDebugController {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SessionDebugController.class);

    /**
     * Debug OAuth2 session information.
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> debugSession(HttpServletRequest request) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        // Session information
        HttpSession session = request.getSession(false);
        if (session != null) {
            debugInfo.put("sessionId", session.getId());
            debugInfo.put("sessionCreationTime", session.getCreationTime());
            debugInfo.put("sessionLastAccessedTime", session.getLastAccessedTime());
            debugInfo.put("sessionMaxInactiveInterval", session.getMaxInactiveInterval());
            debugInfo.put("sessionIsNew", session.isNew());
        } else {
            debugInfo.put("sessionId", "NO_SESSION");
        }
        
        // Cookie information
        Map<String, String> cookies = new HashMap<>();
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
        }
        debugInfo.put("cookies", cookies);
        
        // OAuth2 specific cookie
        String oauth2Cookie = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("oauth2_auth_request".equals(cookie.getName())) {
                    oauth2Cookie = cookie.getValue();
                    break;
                }
            }
        }
        debugInfo.put("oauth2Cookie", oauth2Cookie != null ? "PRESENT" : "ABSENT");
        debugInfo.put("oauth2CookieLength", oauth2Cookie != null ? oauth2Cookie.length() : 0);
        
        // Request information
        debugInfo.put("requestURI", request.getRequestURI());
        debugInfo.put("requestURL", request.getRequestURL().toString());
        debugInfo.put("queryString", request.getQueryString());
        debugInfo.put("remoteAddr", request.getRemoteAddr());
        debugInfo.put("userAgent", request.getHeader("User-Agent"));
        
        logger.info("OAuth2 Session Debug - Session: {}, OAuth2 Cookie: {}", 
                   session != null ? session.getId() : "NO_SESSION", 
                   oauth2Cookie != null ? "PRESENT" : "ABSENT");
        
        return ResponseEntity.ok(debugInfo);
    }

    /**
     * Clear OAuth2 session and cookies.
     */
    @GetMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearSession(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        // Clear session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            result.put("sessionCleared", true);
            logger.info("OAuth2 session cleared: {}", session.getId());
        } else {
            result.put("sessionCleared", false);
        }
        
        result.put("message", "OAuth2 session and cookies cleared");
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Test OAuth2 authorization request storage.
     */
    @GetMapping("/test-storage")
    public ResponseEntity<Map<String, Object>> testStorage(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        // Check if OAuth2 cookie exists
        boolean oauth2CookieExists = false;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("oauth2_auth_request".equals(cookie.getName())) {
                    oauth2CookieExists = true;
                    result.put("oauth2CookieValue", cookie.getValue());
                    result.put("oauth2CookieMaxAge", cookie.getMaxAge());
                    result.put("oauth2CookiePath", cookie.getPath());
                    result.put("oauth2CookieHttpOnly", cookie.isHttpOnly());
                    result.put("oauth2CookieSecure", cookie.getSecure());
                    break;
                }
            }
        }
        
        result.put("oauth2CookieExists", oauth2CookieExists);
        result.put("totalCookies", request.getCookies() != null ? request.getCookies().length : 0);
        
        return ResponseEntity.ok(result);
    }
}
