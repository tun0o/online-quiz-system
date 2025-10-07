package com.example.online_quiz_system.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * SIMPLE HEALTH INDICATOR
 * - Basic database connectivity check
 * - No external dependencies on Actuator classes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomHealthIndicator {

    private final DataSource dataSource;

    /**
     * Simple health check that returns Map
     */
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test database connection
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5);
                
                health.put("status", isValid ? "UP" : "DOWN");
                health.put("database", isValid ? "CONNECTED" : "DISCONNECTED");
                
                if (isValid) {
                    health.put("database_url", connection.getMetaData().getURL());
                    health.put("database_driver", connection.getMetaData().getDriverName());
                }
                
            }
        } catch (Exception e) {
            log.error("Health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return health;
    }
}

