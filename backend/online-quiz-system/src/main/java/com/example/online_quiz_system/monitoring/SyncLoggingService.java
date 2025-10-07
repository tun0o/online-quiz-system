package com.example.online_quiz_system.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

/**
 * SYNC LOGGING SERVICE
 * - Structured logging for sync operations
 * - Performance metrics
 * - Error tracking
 */
@Service
@Slf4j
public class SyncLoggingService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * LOG SYNC START
     */
    public void logSyncStart(String operation, Long userId, Set<String> fields) {
        log.info("SYNC_START | Operation: {} | User: {} | Fields: {} | Time: {}", 
                operation, userId, fields, getCurrentTimestamp());
    }

    /**
     * LOG SYNC SUCCESS
     */
    public void logSyncSuccess(String operation, Long userId, Set<String> fields, long durationMs) {
        log.info("SYNC_SUCCESS | Operation: {} | User: {} | Fields: {} | Duration: {}ms | Time: {}", 
                operation, userId, fields, durationMs, getCurrentTimestamp());
    }

    /**
     * LOG SYNC FAILURE
     */
    public void logSyncFailure(String operation, Long userId, Set<String> fields, Exception error, long durationMs) {
        log.error("SYNC_FAILURE | Operation: {} | User: {} | Fields: {} | Duration: {}ms | Error: {} | Time: {}", 
                operation, userId, fields, durationMs, error.getMessage(), getCurrentTimestamp());
    }

    /**
     * LOG VALIDATION FAILURE
     */
    public void logValidationFailure(String operation, Long userId, String validationErrors) {
        log.warn("VALIDATION_FAILURE | Operation: {} | User: {} | Errors: {} | Time: {}", 
                operation, userId, validationErrors, getCurrentTimestamp());
    }

    /**
     * LOG CONSISTENCY CHECK
     */
    public void logConsistencyCheck(Long userId, boolean isConsistent, String details) {
        if (isConsistent) {
            log.debug("CONSISTENCY_CHECK | User: {} | Status: CONSISTENT | Time: {}", 
                    userId, getCurrentTimestamp());
        } else {
            log.warn("CONSISTENCY_CHECK | User: {} | Status: INCONSISTENT | Details: {} | Time: {}", 
                    userId, details, getCurrentTimestamp());
        }
    }

    /**
     * LOG PERFORMANCE METRICS
     */
    public void logPerformanceMetrics(String operation, Map<String, Object> metrics) {
        log.info("PERFORMANCE_METRICS | Operation: {} | Metrics: {} | Time: {}", 
                operation, metrics, getCurrentTimestamp());
    }

    /**
     * LOG EVENT PROCESSING
     */
    public void logEventProcessing(String eventType, Long userId, boolean async, long processingTimeMs) {
        String mode = async ? "ASYNC" : "SYNC";
        log.info("EVENT_PROCESSING | Type: {} | User: {} | Mode: {} | Duration: {}ms | Time: {}", 
                eventType, userId, mode, processingTimeMs, getCurrentTimestamp());
    }

    /**
     * LOG MANUAL OPERATION
     */
    public void logManualOperation(String operation, Long userId, String reason, String initiator) {
        log.warn("MANUAL_OPERATION | Operation: {} | User: {} | Reason: {} | Initiator: {} | Time: {}", 
                operation, userId, reason, initiator, getCurrentTimestamp());
    }

    /**
     * LOG SYSTEM HEALTH
     */
    public void logSystemHealth(String component, String status, Map<String, Object> healthData) {
        log.info("SYSTEM_HEALTH | Component: {} | Status: {} | Data: {} | Time: {}", 
                component, status, healthData, getCurrentTimestamp());
    }

    /**
     * STRUCTURED LOG for JSON processing
     */
    public void logStructured(String level, String category, Map<String, Object> data) {
        String logMessage = String.format("STRUCTURED_LOG | Category: %s | Data: %s | Time: %s", 
                category, data, getCurrentTimestamp());
        
        switch (level.toUpperCase()) {
            case "ERROR" -> log.error(logMessage);
            case "WARN" -> log.warn(logMessage);
            case "INFO" -> log.info(logMessage);
            case "DEBUG" -> log.debug(logMessage);
            default -> log.info(logMessage);
        }
    }

    // Helper methods
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }

    /**
     * METRIC BUILDER for easy metrics creation
     */
    public static class MetricBuilder {
        private final java.util.Map<String, Object> metrics = new java.util.HashMap<>();

        public static MetricBuilder create() {
            return new MetricBuilder();
        }

        public MetricBuilder add(String key, Object value) {
            metrics.put(key, value);
            return this;
        }

        public MetricBuilder addDuration(long durationMs) {
            metrics.put("duration_ms", durationMs);
            return this;
        }

        public MetricBuilder addCount(String name, int count) {
            metrics.put(name + "_count", count);
            return this;
        }

        public MetricBuilder addSuccess(boolean success) {
            metrics.put("success", success);
            return this;
        }

        public Map<String, Object> build() {
            return new java.util.HashMap<>(metrics);
        }
    }
}
