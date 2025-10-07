package com.example.online_quiz_system.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

/**
 * FLYWAY CONFIGURATION
 * - Handle baseline for existing schemas
 * - Custom migration strategy
 * - Error handling
 */
@Configuration
@Slf4j
public class FlywayConfig {

    /**
     * Custom Flyway Migration Strategy
     * - Automatically handle baseline for existing schemas
     * - Log migration info
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return new FlywayMigrationStrategy() {
            @Override
            public void migrate(Flyway flyway) {
                try {
                    log.info("Starting Flyway migration...");
                    
                    // Check if schema exists and has tables
                    var info = flyway.info();
                    var currentVersion = info.current();
                    
                    if (currentVersion == null) {
                        log.info("No Flyway history found. Checking if schema is empty...");
                        
                        // Try to get schema info
                        var allInfos = info.all();
                        if (allInfos.length == 0) {
                            log.info("Schema appears to be empty. Proceeding with normal migration.");
                        } else {
                            log.info("Schema has existing structure. Setting baseline...");
                            
                            // Set baseline first
                            flyway.baseline();
                            log.info("Flyway baseline set successfully.");
                        }
                    }
                    
                    // Run migration
                    var result = flyway.migrate();
                    log.info("Flyway migration completed successfully. Applied {} migrations.", 
                            result.migrationsExecuted);
                    
                } catch (Exception e) {
                    log.error("Flyway migration failed", e);
                    
                    // Try baseline and migrate again
                    try {
                        log.info("Attempting baseline and retry...");
                        flyway.baseline();
                        var result = flyway.migrate();
                        log.info("Flyway migration completed after baseline. Applied {} migrations.", 
                                result.migrationsExecuted);
                    } catch (Exception retryException) {
                        log.error("Flyway migration failed even after baseline", retryException);
                        throw new RuntimeException("Flyway migration failed", retryException);
                    }
                }
            }
        };
    }
}

