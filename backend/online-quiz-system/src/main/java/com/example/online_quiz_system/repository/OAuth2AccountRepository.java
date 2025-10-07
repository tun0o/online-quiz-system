package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.OAuth2Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for OAuth2Account entity
 * Handles OAuth2 authentication data for multiple providers per user
 */
@Repository
public interface OAuth2AccountRepository extends JpaRepository<OAuth2Account, Long> {
    
    /**
     * Find OAuth2Account by user ID and provider
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.user.id = :userId AND o.provider = :provider")
    Optional<OAuth2Account> findByUserAndProvider(@Param("userId") Long userId, @Param("provider") String provider);
    
    /**
     * Find OAuth2Account by provider and provider ID
     */
    Optional<OAuth2Account> findByProviderAndProviderId(String provider, String providerId);
    
    /**
     * Find all OAuth2Accounts for a user by user ID
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.user.id = :userId")
    List<OAuth2Account> findByUserId(@Param("userId") Long userId);
    
    /**
     * FIXED: Find user IDs that have OAuth2Accounts
     */
    @Query("SELECT DISTINCT o.user.id FROM OAuth2Account o WHERE o.user.id IN :userIds")
    List<Long> findByUserId(@Param("userIds") List<Long> userIds);
    
    /**
     * Find all OAuth2Accounts for a user ordered by last used
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.user.id = :userId ORDER BY o.lastUsedAt DESC")
    List<OAuth2Account> findByUserIdOrderByLastUsedAtDesc(@Param("userId") Long userId);
    
    /**
     * Find primary OAuth2Account for a user
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.user.id = :userId AND o.isPrimary = true")
    Optional<OAuth2Account> findPrimaryByUserId(@Param("userId") Long userId);
    
    /**
     * Check if user has OAuth2Account for specific provider
     */
    @Query("SELECT COUNT(o) > 0 FROM OAuth2Account o WHERE o.user.id = :userId AND o.provider = :provider")
    boolean existsByUserAndProvider(@Param("userId") Long userId, @Param("provider") String provider);
    
    /**
     * Count OAuth2Accounts for a user
     */
    @Query("SELECT COUNT(o) FROM OAuth2Account o WHERE o.user.id = :userId")
    int countByUserId(@Param("userId") Long userId);
    
    /**
     * Find OAuth2Accounts by provider
     */
    List<OAuth2Account> findByProvider(String provider);
    
    /**
     * Find OAuth2Accounts by provider email
     */
    List<OAuth2Account> findByProviderEmail(String email);
    
    /**
     * Find OAuth2Accounts by provider name
     */
    List<OAuth2Account> findByProviderNameContainingIgnoreCase(String name);
    
    /**
     * Find OAuth2Accounts by provider gender
     */
    List<OAuth2Account> findByProviderGender(String gender);
    
    /**
     * Find OAuth2Accounts by provider locale
     */
    List<OAuth2Account> findByProviderLocale(String locale);
    
    /**
     * Find recently used OAuth2Accounts
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.lastUsedAt IS NOT NULL ORDER BY o.lastUsedAt DESC")
    List<OAuth2Account> findRecentlyUsed();
    
    /**
     * Find OAuth2Accounts linked after specific date
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.linkedAt >= :date ORDER BY o.linkedAt DESC")
    List<OAuth2Account> findLinkedAfter(@Param("date") LocalDateTime date);
    
    /**
     * Find OAuth2Accounts by user ID and provider (with user data)
     */
    @Query("SELECT o FROM OAuth2Account o LEFT JOIN FETCH o.user WHERE o.user.id = :userId AND o.provider = :provider")
    Optional<OAuth2Account> findByUserIdAndProviderWithUser(@Param("userId") Long userId, @Param("provider") String provider);
    
    /**
     * Find all OAuth2Accounts with user data
     */
    @Query("SELECT o FROM OAuth2Account o LEFT JOIN FETCH o.user WHERE o.user.id = :userId")
    List<OAuth2Account> findByUserIdWithUser(@Param("userId") Long userId);
    
    /**
     * Find OAuth2Accounts by multiple providers
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.user.id = :userId AND o.provider IN :providers")
    List<OAuth2Account> findByUserIdAndProviderIn(@Param("userId") Long userId, @Param("providers") List<String> providers);
    
    /**
     * Find OAuth2Accounts by provider and gender
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.provider = :provider AND o.providerGender = :gender")
    List<OAuth2Account> findByProviderAndGender(@Param("provider") String provider, @Param("gender") String gender);
    
    /**
     * Find OAuth2Accounts by provider and locale
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.provider = :provider AND o.providerLocale = :locale")
    List<OAuth2Account> findByProviderAndLocale(@Param("provider") String provider, @Param("locale") String locale);
    
    /**
     * Count OAuth2Accounts by provider
     */
    @Query("SELECT COUNT(o) FROM OAuth2Account o WHERE o.provider = :provider")
    int countByProvider(@Param("provider") String provider);
    
    /**
     * Find OAuth2Accounts with specific phone number
     */
    List<OAuth2Account> findByProviderPhone(String phone);
    
    /**
     * Find OAuth2Accounts with specific birthday
     */
    List<OAuth2Account> findByProviderBirthday(String birthday);
    
    /**
     * Find OAuth2Accounts by user ID and is primary
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.user.id = :userId AND o.isPrimary = :isPrimary")
    List<OAuth2Account> findByUserIdAndIsPrimary(@Param("userId") Long userId, @Param("isPrimary") Boolean isPrimary);
    
    /**
     * Find OAuth2Accounts by user ID and provider with last used date
     */
    @Query("SELECT o FROM OAuth2Account o WHERE o.user.id = :userId AND o.provider = :provider AND o.lastUsedAt >= :date")
    List<OAuth2Account> findByUserIdAndProviderAndLastUsedAfter(@Param("userId") Long userId, @Param("provider") String provider, @Param("date") LocalDateTime date);
}