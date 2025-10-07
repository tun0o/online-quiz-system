package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.security.InMemoryOAuth2AuthorizationRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Debug controller for OAuth2 in-memory storage.
 */
@RestController
@RequestMapping("/api/oauth2/memory")
public class OAuth2MemoryDebugController {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2MemoryDebugController.class);

    private final InMemoryOAuth2AuthorizationRequestRepository repository;

    public OAuth2MemoryDebugController(InMemoryOAuth2AuthorizationRequestRepository oauth2AuthorizationRequestRepository) {
        this.repository = oauth2AuthorizationRequestRepository;
    }

    /**
     * Get in-memory storage statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("storageSize", repository.getStorageSize());
        stats.put("storageStats", repository.getStorageStats());
        stats.put("timestamp", System.currentTimeMillis());
        
        logger.info("OAuth2 Memory Storage Stats - Size: {}", repository.getStorageSize());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check for in-memory storage.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        
        int size = repository.getStorageSize();
        boolean isHealthy = size >= 0; // Basic health check
        
        health.put("status", isHealthy ? "HEALTHY" : "UNHEALTHY");
        health.put("storageSize", size);
        health.put("timestamp", System.currentTimeMillis());
        
        if (size > 100) {
            health.put("warning", "High number of stored requests: " + size);
        }
        
        return ResponseEntity.ok(health);
    }
}