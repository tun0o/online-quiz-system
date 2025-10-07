package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.service.CacheWarmingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 🔥 Cache Management Controller
 * Provides endpoints for cache management and monitoring
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheManagementController {

    private final CacheWarmingService cacheWarmingService;
    private final CacheManager cacheManager;

    /**
     * 🔥 Warm critical caches
     */
    @PostMapping("/warm")
    public ResponseEntity<Map<String, Object>> warmCaches() {
        log.info("🔥 Manual cache warming requested");
        
        try {
            cacheWarmingService.warmCriticalCaches();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cache warming completed");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Cache warming failed", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Cache warming failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 🔥 Warm cache for specific user
     */
    @PostMapping("/warm/user/{userId}")
    public ResponseEntity<Map<String, Object>> warmUserCache(@PathVariable Long userId) {
        log.info("🔥 Warming cache for user: {}", userId);
        
        try {
            cacheWarmingService.warmUserCache(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User cache warmed for ID: " + userId);
            response.put("userId", userId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to warm cache for user: {}", userId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to warm user cache: " + e.getMessage());
            response.put("userId", userId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 🔥 Clear all caches
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        log.info("🔥 Clearing all caches");
        
        try {
            cacheWarmingService.clearAllCaches();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All caches cleared");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to clear caches", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to clear caches: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 🔥 Clear cache for specific user
     */
    @DeleteMapping("/clear/user/{userId}")
    public ResponseEntity<Map<String, Object>> clearUserCache(@PathVariable Long userId) {
        log.info("🔥 Clearing cache for user: {}", userId);
        
        try {
            cacheWarmingService.clearUserCache(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User cache cleared for ID: " + userId);
            response.put("userId", userId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to clear cache for user: {}", userId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to clear user cache: " + e.getMessage());
            response.put("userId", userId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 📊 Get cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        log.info("📊 Getting cache statistics");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            Map<String, Object> cacheInfo = new HashMap<>();
            
            // Get cache names and basic info
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Map<String, Object> cacheDetails = new HashMap<>();
                    cacheDetails.put("name", cacheName);
                    cacheDetails.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                    cacheDetails.put("size", "N/A (Redis)");
                    cacheInfo.put(cacheName, cacheDetails);
                }
            });
            
            stats.put("cacheRegions", cacheInfo);
            stats.put("totalRegions", cacheManager.getCacheNames().size());
            stats.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Failed to get cache statistics", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get cache statistics: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 📊 Log cache statistics
     */
    @PostMapping("/stats/log")
    public ResponseEntity<Map<String, Object>> logCacheStats() {
        log.info("📊 Logging cache statistics");
        
        try {
            cacheWarmingService.logCacheStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cache statistics logged to console");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to log cache statistics", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to log cache statistics: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 🔥 Health check for cache system
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> cacheHealth() {
        log.info("🔥 Checking cache health");
        
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("cacheManager", cacheManager.getClass().getSimpleName());
            health.put("cacheRegions", cacheManager.getCacheNames().size());
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("❌ Cache health check failed", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
