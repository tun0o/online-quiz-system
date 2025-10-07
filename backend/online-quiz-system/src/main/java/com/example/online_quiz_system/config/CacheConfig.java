package com.example.online_quiz_system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * CACHE CONFIGURATION
 * - Redis cache manager for Spring Cache abstraction only
 * - No Hibernate L2 cache integration
 * - Custom serialization configuration
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis Cache Manager với custom configuration
     * - Chỉ dùng cho Spring Cache abstraction
     * - Không inject EntityManagerFactory để tránh circular dependency
     * - Different TTL for different cache regions
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        // Sử dụng shared ObjectMapper từ RedisConfig
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Configure serialization
        RedisSerializationContext.SerializationPair<Object> pair =
            RedisSerializationContext.SerializationPair.fromSerializer(serializer);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(pair)
            .entryTtl(Duration.ofMinutes(60)); // Default TTL: 60 minutes

        // 🔥 PRIORITY 1: Critical authentication caches - Longer TTL
        RedisCacheConfiguration authConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(pair)
            .entryTtl(Duration.ofHours(2)); // 2 hours for auth data

        // 🔥 PRIORITY 2: User profile caches - Medium TTL  
        RedisCacheConfiguration profileConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(pair)
            .entryTtl(Duration.ofMinutes(30)); // 30 minutes for profiles

        // 🔥 PRIORITY 3: System caches - Shorter TTL
        RedisCacheConfiguration systemConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(pair)
            .entryTtl(Duration.ofMinutes(15)); // 15 minutes for system data

        // Cache configurations map
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Authentication caches - CRITICAL
        cacheConfigurations.put("users_by_email", authConfig);
        cacheConfigurations.put("users_by_oauth", authConfig);
        
        // User profile caches - HIGH FREQUENCY
        cacheConfigurations.put("user_profiles", profileConfig);
        
        // System caches - MEDIUM FREQUENCY
        cacheConfigurations.put("users_with_profile", systemConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}

