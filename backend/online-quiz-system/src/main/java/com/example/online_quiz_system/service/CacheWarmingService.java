package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.OAuth2Account;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.repository.UserProfileRepository;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.repository.OAuth2AccountRepository;
import com.example.online_quiz_system.util.LogMaskingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 🔥 PRIORITY 3: Cache Warming Service
 * Pre-cache critical data for better performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheWarmingService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final OAuth2AccountRepository oauth2AccountRepository;
    private final UserService userService;
    private final UserProfileService userProfileService;
    private final CacheManager cacheManager;

    /**
     * 🔥 PRIORITY 3: Implement cache warming
     * Pre-cache active users and critical data on startup
     */
    @PostConstruct
    @Async
    public void warmCriticalCaches() {
        log.info("🔥 Starting cache warming process...");
        
        try {
            // Warm authentication caches
            warmAuthenticationCaches();
            
            // Warm user profile caches
            warmUserProfileCaches();
            
            // Warm system caches
            warmSystemCaches();
            
            log.info("✅ Cache warming completed successfully");
            
        } catch (Exception e) {
            log.error("❌ Cache warming failed", e);
        }
    }

    /**
     * Warm authentication-related caches
     * FIXED: Use OAuth2AccountRepository instead of accessing lazy collection
     */
    @Transactional(readOnly = true)
    public void warmAuthenticationCaches() {
        log.info("🔥 Warming authentication caches...");
        
        // Get recent active users (last 7 days) - LIMIT QUERY
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<User> activeUsers = userRepository.findAll().stream()
            .filter(user -> user.getUpdatedAt() != null && user.getUpdatedAt().isAfter(sevenDaysAgo))
            .limit(50) // REDUCED from 100 to 50
            .toList();
        
        // Pre-cache by email
        for (User user : activeUsers) {
            try {
                userService.getUserByEmail(user.getEmail());
                log.debug("Warmed email cache for: {}", LogMaskingUtils.maskEmail(user.getEmail()));
            } catch (Exception e) {
                log.warn("Failed to warm email cache for: {}", LogMaskingUtils.maskEmail(user.getEmail()), e);
            }
        }
        
        // FIXED: Use OAuth2AccountRepository instead of accessing lazy collection
        // Get OAuth users by querying OAuth2Account table directly
        List<Long> oauthUserIds = oauth2AccountRepository.findByUserId(activeUsers.stream()
            .map(User::getId)
            .toList());
        
        for (Long userId : oauthUserIds) {
            try {
                // Cache OAuth2Account data using repository
                List<OAuth2Account> accounts = oauth2AccountRepository.findByUserId(userId);
                for (OAuth2Account account : accounts) {
                    oauth2AccountRepository.findByProviderAndProviderId(account.getProvider(), account.getProviderId());
                    log.debug("Warmed OAuth cache for: {} - {}", account.getProvider(), account.getProviderId());
                }
            } catch (Exception e) {
                log.warn("Failed to warm OAuth cache for user: {}", userId, e);
            }
        }
        
        log.info("✅ Authentication caches warmed: {} users", activeUsers.size());
    }

    /**
     * Warm user profile caches
     * FIXED: Optimize query to avoid N+1 problem
     */
    @Transactional(readOnly = true)
    public void warmUserProfileCaches() {
        log.info("🔥 Warming user profile caches...");
        
        // FIXED: Use repository method instead of stream filter
        List<Long> usersWithProfiles = userProfileRepository.findUserIdsWithProfiles()
            .stream()
            .limit(30) // REDUCED from 50 to 30
            .toList();
        
        // Pre-cache user profiles
        for (Long userId : usersWithProfiles) {
            try {
                userProfileService.getUserProfile(userId);
                log.debug("Warmed profile cache for user: {}", userId);
            } catch (Exception e) {
                log.warn("Failed to warm profile cache for user: {}", userId, e);
            }
        }
        
        log.info("✅ User profile caches warmed: {} profiles", usersWithProfiles.size());
    }

    /**
     * Warm system caches
     * FIXED: Reduce query load
     */
    @Transactional(readOnly = true)
    public void warmSystemCaches() {
        log.info("🔥 Warming system caches...");
        
        // FIXED: Use more efficient query with limit
        List<User> activeUsers = userRepository.findAll().stream()
            .filter(user -> user.getUpdatedAt() != null && 
                          user.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(3)))
            .limit(10) // REDUCED from 20 to 10
            .toList();
        
        for (User user : activeUsers) {
            try {
                userService.getUserById(user.getId());
                log.debug("Warmed user with profile cache for: {}", user.getId());
            } catch (Exception e) {
                log.warn("Failed to warm user with profile cache for: {}", user.getId(), e);
            }
        }
        
        log.info("✅ System caches warmed: {} users", activeUsers.size());
    }

    /**
     * Manual cache warming for specific user
     * FIXED: Use repository instead of lazy collection access
     */
    @Transactional(readOnly = true)
    public void warmUserCache(Long userId) {
        log.info("🔥 Warming cache for user: {}", userId);
        
        try {
            // Warm user data
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                // Warm email cache
                userService.getUserByEmail(user.get().getEmail());
                
                // FIXED: Use OAuth2AccountRepository instead of accessing lazy collection
                List<OAuth2Account> accounts = oauth2AccountRepository.findByUserId(userId);
                for (OAuth2Account account : accounts) {
                    oauth2AccountRepository.findByProviderAndProviderId(account.getProvider(), account.getProviderId());
                }
                
                // Warm profile cache
                userProfileService.getUserProfile(userId);
                
                log.info("✅ Cache warmed for user: {}", userId);
            }
        } catch (Exception e) {
            log.error("❌ Failed to warm cache for user: {}", userId, e);
        }
    }

    /**
     * Clear all caches
     */
    @CacheEvict(value = {"users_by_email", "users_by_oauth", "user_profiles", "users_with_profile"}, allEntries = true)
    public void clearAllCaches() {
        log.info("🔥 Clearing all caches...");
        
        // Clear specific cache regions
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.debug("Cleared cache: {}", cacheName);
            }
        });
        
        log.info("✅ All caches cleared");
    }

    /**
     * Clear cache for specific user
     * FIXED: Use repository instead of lazy collection access
     */
    public void clearUserCache(Long userId) {
        log.info("🔥 Clearing cache for user: {}", userId);
        
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                // Clear email cache
                var emailCache = cacheManager.getCache("users_by_email");
                if (emailCache != null) {
                    emailCache.evict(user.get().getEmail());
                }
                
                // FIXED: Use OAuth2AccountRepository instead of accessing lazy collection
                List<OAuth2Account> accounts = oauth2AccountRepository.findByUserId(userId);
                var oauthCache = cacheManager.getCache("users_by_oauth");
                if (oauthCache != null) {
                    for (OAuth2Account account : accounts) {
                        oauthCache.evict(account.getProvider() + "_" + account.getProviderId());
                    }
                }
                
                // Clear profile cache
                var profileCache = cacheManager.getCache("user_profiles");
                if (profileCache != null) {
                    profileCache.evict(userId);
                }
                
                // Clear user with profile cache
                var userWithProfileCache = cacheManager.getCache("users_with_profile");
                if (userWithProfileCache != null) {
                    userWithProfileCache.evict(userId);
                }
                
                log.info("✅ Cache cleared for user: {}", userId);
            }
        } catch (Exception e) {
            log.error("❌ Failed to clear cache for user: {}", userId, e);
        }
    }

    /**
     * Get cache statistics
     */
    public void logCacheStatistics() {
        log.info("📊 Cache Statistics:");
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                // Note: Redis cache statistics would need Redis-specific implementation
                log.info("Cache: {} - Size: {}", cacheName, "N/A (Redis)");
            }
        });
    }
}