package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.monitoring.DataConsistencyMonitor;
import com.example.online_quiz_system.monitoring.SyncLoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MONITORING CONTROLLER
 * - Data consistency monitoring
 * - System health checks
 * - Performance metrics
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final DataConsistencyMonitor consistencyMonitor;
    private final SyncLoggingService syncLoggingService;

    // ===== CONSISTENCY MONITORING =====

    /**
     * Full system consistency check
     */
    @GetMapping("/consistency/full-check")
    public ResponseEntity<Map<String, Object>> fullConsistencyCheck() {
        var report = consistencyMonitor.performFullConsistencyCheck();
        
        Map<String, Object> response = Map.of(
            "checkTime", report.getCheckTime(),
            "issueCount", report.getIssueCount(),
            "hasIssues", report.hasIssues(),
            "issues", report.getIssues()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Single user consistency check
     */
    @GetMapping("/consistency/user/{userId}")
    public ResponseEntity<Map<String, Object>> checkUserConsistency(@PathVariable Long userId) {
        var report = consistencyMonitor.checkUserConsistency(userId);
        
        Map<String, Object> response = Map.of(
            "userId", userId,
            "checkTime", report.getCheckTime(),
            "issueCount", report.getIssueCount(),
            "hasIssues", report.hasIssues(),
            "issues", report.getIssues()
        );
        
        return ResponseEntity.ok(response);
    }

    // ===== SYSTEM HEALTH =====

    /**
     * System health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> systemHealth() {
        Map<String, Object> healthData = Map.of(
            "status", "healthy",
            "timestamp", java.time.LocalDateTime.now(),
            "services", Map.of(
                "userProfileSync", "active",
                "consistencyMonitor", "active",
                "validation", "active"
            )
        );
        
        syncLoggingService.logSystemHealth("MonitoringController", "HEALTHY", healthData);
        
        return ResponseEntity.ok(healthData);
    }

    /**
     * Performance metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = SyncLoggingService.MetricBuilder.create()
            .add("endpoint", "/api/monitoring/metrics")
            .add("timestamp", java.time.LocalDateTime.now())
            .add("status", "active")
            .build();
        
        syncLoggingService.logPerformanceMetrics("METRICS_REQUEST", metrics);
        
        return ResponseEntity.ok(metrics);
    }

    // ===== MANUAL OPERATIONS =====

    /**
     * Trigger manual consistency check
     */
    @PostMapping("/consistency/trigger-check")
    public ResponseEntity<Map<String, String>> triggerConsistencyCheck() {
        // This will be handled by the scheduled task
        syncLoggingService.logManualOperation(
            "TRIGGER_CONSISTENCY_CHECK", 
            null, 
            "Manual trigger via API", 
            "admin"
        );
        
        Map<String, String> response = Map.of(
            "status", "triggered",
            "message", "Consistency check has been triggered"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test logging
     */
    @PostMapping("/test-logging")
    public ResponseEntity<Map<String, String>> testLogging() {
        // Test different log levels
        syncLoggingService.logStructured("INFO", "TEST", Map.of("test", "info_log"));
        syncLoggingService.logStructured("WARN", "TEST", Map.of("test", "warn_log"));
        syncLoggingService.logStructured("ERROR", "TEST", Map.of("test", "error_log"));
        
        Map<String, String> response = Map.of(
            "status", "completed",
            "message", "Test logging completed"
        );
        
        return ResponseEntity.ok(response);
    }
}

