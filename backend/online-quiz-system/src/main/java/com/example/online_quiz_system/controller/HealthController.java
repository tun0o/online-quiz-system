package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.health.HealthCheckService;
import com.example.online_quiz_system.monitoring.DataConsistencyMonitor;
import com.example.online_quiz_system.service.UserProfileSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * HEALTH CHECK CONTROLLER
 * - System health endpoints
 * - Database status
 * - Data integrity checks
 * - Performance metrics
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthCheckService healthCheckService;
    private final DataConsistencyMonitor consistencyMonitor;
    private final UserProfileSyncService syncService;

    /**
     * BASIC HEALTH CHECK
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> basicHealth() {
        Map<String, Object> health = healthCheckService.getHealthStatus();
        
        String statusCode = (String) health.get("status_code");
        boolean isHealthy = "UP".equals(statusCode);
        
        return isHealthy ?
            ResponseEntity.ok(health) :
            ResponseEntity.status(503).body(health);
    }

    /**
     * DETAILED HEALTH CHECK (Admin only)
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> healthStatus = healthCheckService.getDetailedHealthStatus();
        
        // Add consistency check results
        var consistencyReport = consistencyMonitor.performFullConsistencyCheck();
        healthStatus.put("consistency_check", Map.of(
            "has_issues", consistencyReport.hasIssues(),
            "issue_count", consistencyReport.getIssueCount(),
            "check_time", consistencyReport.getCheckTime()
        ));
        
        return ResponseEntity.ok(healthStatus);
    }

    /**
     * DATABASE CONNECTIVITY CHECK
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        try {
            Map<String, Object> health = healthCheckService.getHealthStatus();
            Object dbStatus = health.get("database_status");
            
            Map<String, Object> response = Map.of(
                "database_status", dbStatus != null ? dbStatus : "unknown",
                "overall_health", health.get("status_code"),
                "timestamp", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "database_status", "ERROR",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
            
            return ResponseEntity.status(503).body(errorResponse);
        }
    }

    /**
     * DATA CONSISTENCY CHECK
     */
    @GetMapping("/consistency")
    public ResponseEntity<Map<String, Object>> consistencyHealth() {
        try {
            var report = consistencyMonitor.performFullConsistencyCheck();
            
            Map<String, Object> response = Map.of(
                "status", report.hasIssues() ? "ISSUES_FOUND" : "HEALTHY",
                "issue_count", report.getIssueCount(),
                "check_time", report.getCheckTime(),
                "has_issues", report.hasIssues(),
                "issues", report.getIssues().stream()
                    .map(issue -> Map.of(
                        "user_id", issue.getUserId(),
                        "type", issue.getType(),
                        "description", issue.getDescription(),
                        "detected_at", issue.getDetectedAt()
                    )).toList()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "status", "ERROR",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * SYNC SERVICE HEALTH
     */
    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncHealth() {
        try {
            // Check if sync service is responsive
            var orphanProfiles = syncService.findOrphanProfiles();
            
            Map<String, Object> response = Map.of(
                "sync_service_status", "RESPONSIVE",
                "orphan_profiles_count", orphanProfiles.size(),
                "orphan_profiles", orphanProfiles,
                "timestamp", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "sync_service_status", "ERROR",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * SYSTEM METRICS
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> systemMetrics() {
        Runtime runtime = Runtime.getRuntime();
        
        Map<String, Object> metrics = Map.of(
            "memory", Map.of(
                "total_mb", runtime.totalMemory() / (1024 * 1024),
                "free_mb", runtime.freeMemory() / (1024 * 1024),
                "used_mb", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                "max_mb", runtime.maxMemory() / (1024 * 1024)
            ),
            "system", Map.of(
                "processors", runtime.availableProcessors(),
                "timestamp", LocalDateTime.now()
            )
        );
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * READINESS CHECK (K8s/Docker)
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> readinessCheck() {
        try {
            boolean isReady = healthCheckService.isHealthy();
            
            Map<String, String> response = Map.of(
                "status", isReady ? "READY" : "NOT_READY",
                "timestamp", LocalDateTime.now().toString()
            );
            
            return isReady ? 
                ResponseEntity.ok(response) :
                ResponseEntity.status(503).body(response);
                
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                "status", "ERROR",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.status(503).body(errorResponse);
        }
    }

    /**
     * LIVENESS CHECK (K8s/Docker)
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> livenessCheck() {
        // Simple liveness check - just return OK if the application is running
        Map<String, String> response = Map.of(
            "status", "ALIVE",
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.ok(response);
    }
}
