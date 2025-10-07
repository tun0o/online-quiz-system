package com.example.online_quiz_system.health;

import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * COMPREHENSIVE HEALTH CHECK SERVICE
 * - Database connectivity
 * - Repository functionality  
 * - Data integrity status
 * - System metrics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private final DataSource dataSource;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    /**
     * Get comprehensive health status
     */
    public Map<String, Object> getHealthStatus() {
        try {
            Map<String, Object> healthDetails = new HashMap<>();
            
            // Check database connectivity
            boolean dbHealthy = checkDatabaseHealth(healthDetails);
            
            // Check repository functionality
            boolean repoHealthy = checkRepositoryHealth(healthDetails);
            
            // Check data integrity
            boolean dataHealthy = checkDataIntegrity(healthDetails);
            
            // Add system metrics
            addSystemMetrics(healthDetails);
            
            // Overall health status
            boolean isHealthy = dbHealthy && repoHealthy && dataHealthy;
            
            healthDetails.put("timestamp", LocalDateTime.now());
            healthDetails.put("overall_status", isHealthy ? "HEALTHY" : "UNHEALTHY");
            healthDetails.put("status_code", isHealthy ? "UP" : "DOWN");
            
            return healthDetails;
                
        } catch (Exception e) {
            log.error("Health check failed", e);
            return Map.of(
                "status_code", "DOWN",
                "overall_status", "ERROR",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
        }
    }

    /**
     * DATABASE HEALTH CHECK
     */
    private boolean checkDatabaseHealth(Map<String, Object> details) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5); // 5 second timeout
                details.put("database_status", isValid ? "CONNECTED" : "DISCONNECTED");
                details.put("database_url", connection.getMetaData().getURL());
                details.put("database_driver", connection.getMetaData().getDriverName());
                return isValid;
            }
        } catch (Exception e) {
            log.error("Database health check failed", e);
            details.put("database_status", "ERROR");
            details.put("database_error", e.getMessage());
            return false;
        }
    }

    /**
     * REPOSITORY HEALTH CHECK
     */
    private boolean checkRepositoryHealth(Map<String, Object> details) {
        try {
            // Test basic repository operations
            long userCount = userRepository.count();
            long profileCount = userProfileRepository.count();
            
            details.put("repository_status", "FUNCTIONAL");
            details.put("total_users", userCount);
            details.put("total_profiles", profileCount);
            
            // Check for basic data consistency
            double profileRatio = userCount > 0 ? (double) profileCount / userCount : 0;
            details.put("profile_ratio", Math.round(profileRatio * 100) / 100.0);
            
            return true;
            
        } catch (Exception e) {
            log.error("Repository health check failed", e);
            details.put("repository_status", "ERROR");
            details.put("repository_error", e.getMessage());
            return false;
        }
    }

    /**
     * DATA INTEGRITY CHECK
     */
    private boolean checkDataIntegrity(Map<String, Object> details) {
        try {
            // Check for users without profiles
            long usersWithoutProfiles = userRepository.findAll().stream()
                .filter(user -> !userProfileRepository.existsByUserId(user.getId()))
                .count();
            
            // Check for orphan profiles (should be 0)
            long orphanProfiles = userProfileRepository.findAll().stream()
                .filter(profile -> !userRepository.existsById(profile.getUserId()))
                .count();
            
            details.put("data_integrity_status", "CHECKED");
            details.put("users_without_profiles", usersWithoutProfiles);
            details.put("orphan_profiles", orphanProfiles);
            
            boolean isIntegrityHealthy = usersWithoutProfiles == 0 && orphanProfiles == 0;
            details.put("data_integrity_healthy", isIntegrityHealthy);
            
            if (!isIntegrityHealthy) {
                details.put("data_integrity_warning", "Some data inconsistencies detected");
            }
            
            return true; // Data integrity issues are warnings, not health failures
            
        } catch (Exception e) {
            log.error("Data integrity check failed", e);
            details.put("data_integrity_status", "ERROR");
            details.put("data_integrity_error", e.getMessage());
            return false;
        }
    }

    /**
     * SYSTEM METRICS
     */
    private void addSystemMetrics(Map<String, Object> details) {
        try {
            Runtime runtime = Runtime.getRuntime();
            
            Map<String, Object> systemMetrics = new HashMap<>();
            systemMetrics.put("total_memory_mb", runtime.totalMemory() / (1024 * 1024));
            systemMetrics.put("free_memory_mb", runtime.freeMemory() / (1024 * 1024));
            systemMetrics.put("used_memory_mb", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
            systemMetrics.put("max_memory_mb", runtime.maxMemory() / (1024 * 1024));
            systemMetrics.put("available_processors", runtime.availableProcessors());
            
            details.put("system_metrics", systemMetrics);
            
        } catch (Exception e) {
            log.warn("Failed to collect system metrics", e);
            details.put("system_metrics_error", e.getMessage());
        }
    }

    /**
     * DETAILED HEALTH CHECK (For admin endpoints)
     */
    public Map<String, Object> getDetailedHealthStatus() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        // Run all health checks
        checkDatabaseHealth(healthStatus);
        checkRepositoryHealth(healthStatus);
        checkDataIntegrity(healthStatus);
        addSystemMetrics(healthStatus);
        
        // Add additional details
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("health_check_version", "1.0");
        
        return healthStatus;
    }

    /**
     * SIMPLE HEALTH CHECK
     */
    public boolean isHealthy() {
        try {
            Map<String, Object> health = getHealthStatus();
            return "HEALTHY".equals(health.get("overall_status"));
        } catch (Exception e) {
            log.error("Error checking health", e);
            return false;
        }
    }
}