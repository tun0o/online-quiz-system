package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.UserProfile;
import com.example.online_quiz_system.event.UserCreatedEvent;
import com.example.online_quiz_system.event.UserUpdatedEvent;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.monitoring.SyncLoggingService;
import com.example.online_quiz_system.repository.UserProfileRepository;
import com.example.online_quiz_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.Set;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * UNIFIED UserProfileSyncService
 * - Event-driven sync (User -> UserProfile)
 * - Bi-directional sync capability
 * - Comprehensive monitoring & logging
 * - Data consistency validation
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class UserProfileSyncService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SyncLoggingService syncLoggingService;
    
    // 🔥 Clean & Extensible Field Syncers
    private final Map<String, BiConsumer<UserProfile, User>> FIELD_SYNCERS = Map.of(
        "email", (profile, user) -> profile.setEmail(user.getEmail()),
        "isVerified", (profile, user) -> profile.setEmailVerified(user.getIsVerified()),
        "grade", (profile, user) -> profile.setGrade(user.getGrade()),
        "goal", (profile, user) -> profile.setGoal(user.getGoal())
    );

    // ===== PRIMARY SYNC: Event-driven =====

    /**
     * USER CREATED: Tự động tạo UserProfile khi User được tạo
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("userProfileSyncExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserCreated(UserCreatedEvent event) {
        long startTime = System.currentTimeMillis();
        Long userId = event.getUserId();
        
        try {
            log.info("Processing UserCreatedEvent for user ID: {} (after commit)", userId);
            
            // Load fresh user from DB với new transaction
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found: " + userId));
            
            // Tối ưu: Chỉ 1 query thay vì existsByUserId() + findByUserId()
            Optional<UserProfile> existingProfileOpt = userProfileRepository.findByUserId(userId);
            
            if (existingProfileOpt.isPresent()) {
                log.info("UserProfile already exists for user ID: {}, syncing data...", userId);
                
                // Sync dữ liệu từ User sang UserProfile hiện có
                UserProfile existingProfile = existingProfileOpt.get();
                syncSpecificFields(existingProfile, user, Set.of("email", "isVerified", "grade", "goal"));
                userProfileRepository.save(existingProfile);
                log.info("Synced existing UserProfile for user ID: {}", userId);
            } else {
                // 🔥 IDEMPOTENT CREATE: Sử dụng idempotent create method
                UserProfile profile = createNewProfile(user);
                log.info("Created/Retrieved UserProfile for user ID: {}", userId);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            syncLoggingService.logSyncSuccess("USER_CREATED", userId, Set.of("profile_creation"), duration);
            
            log.info("Successfully processed UserCreatedEvent for user ID: {} in {}ms", userId, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Failed to create UserProfile for user ID: {}", userId, e);
            syncLoggingService.logSyncFailure("USER_CREATED", userId, Set.of("profile_creation"), e, duration);
        }
    }

    /**
     * PRIMARY SYNC: User -> UserProfile (Event-driven)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("userProfileSyncExecutor")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void handleUserUpdated(UserUpdatedEvent event) {
        long startTime = System.currentTimeMillis();
        Long userId = event.getUserId();
        Set<String> changedFields = event.getChangedFields();
        
        syncLoggingService.logSyncStart("USER_TO_PROFILE_SYNC", userId, changedFields);
        
        try {
            // Load fresh user from DB để tránh stale data
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found: " + userId));
            
            UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createNewProfile(user));

            boolean updated = syncSpecificFields(profile, user, changedFields);
            
            if (updated) {
                userProfileRepository.save(profile);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            syncLoggingService.logSyncSuccess("USER_TO_PROFILE_SYNC", userId, changedFields, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            syncLoggingService.logSyncFailure("USER_TO_PROFILE_SYNC", userId, changedFields, e, duration);
        }
    }

    // ===== BI-DIRECTIONAL SYNC =====

    /**
     * BI-DIRECTIONAL SYNC: UserProfile -> User
     */
    @Transactional
    public void syncUserFromProfile(Long userId) {
        long startTime = System.currentTimeMillis();
        syncLoggingService.logSyncStart("PROFILE_TO_USER_SYNC", userId, Set.of("reverse_sync"));
        
        try {
            UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("UserProfile not found for user ID: " + userId));
            
            User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new BusinessException("User not found for profile ID: " + profile.getUserId()));
            Set<String> changedFields = new java.util.HashSet<>();
            
            // Sync fields
            if (!user.getEmail().equals(profile.getEmail())) {
                user.setEmail(profile.getEmail());
                changedFields.add("email");
            }
            
            if (!java.util.Objects.equals(user.getGrade(), profile.getGrade())) {
                user.setGrade(profile.getGrade());
                changedFields.add("grade");
            }
            
            if (!java.util.Objects.equals(user.getGoal(), profile.getGoal())) {
                user.setGoal(profile.getGoal());
                changedFields.add("goal");
            }
            
            if (!changedFields.isEmpty()) {
                userRepository.save(user);
                eventPublisher.publishEvent(new UserUpdatedEvent(user.getId(), changedFields));
            }
            
            long duration = System.currentTimeMillis() - startTime;
            syncLoggingService.logSyncSuccess("PROFILE_TO_USER_SYNC", userId, changedFields, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            syncLoggingService.logSyncFailure("PROFILE_TO_USER_SYNC", userId, Set.of("reverse_sync"), e, duration);
            throw e;
        }
    }

    // ===== VALIDATION & CONSISTENCY =====

    /**
     * VALIDATION: Kiểm tra consistency (read-only)
     */
    @Transactional(readOnly = true)
    public boolean isConsistent(Long userId) {
        try {
            UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
            if (profile == null) {
                syncLoggingService.logConsistencyCheck(userId, false, "Profile not found");
                return false;
            }

            User user = userRepository.findById(profile.getUserId()).orElse(null);
            if (user == null) {
                syncLoggingService.logConsistencyCheck(userId, false, "User not found");
                return false;
            }

            boolean isConsistent = user.getEmail().equals(profile.getEmail()) &&
                   user.getIsVerified().equals(profile.getEmailVerified()) &&
                   java.util.Objects.equals(user.getGrade(), profile.getGrade()) &&
                   java.util.Objects.equals(user.getGoal(), profile.getGoal());

            syncLoggingService.logConsistencyCheck(userId, isConsistent, 
                isConsistent ? "All fields consistent" : "Data inconsistency detected");
            
            return isConsistent;

        } catch (Exception e) {
            log.error("Error checking consistency for user ID: {}", userId, e);
            syncLoggingService.logConsistencyCheck(userId, false, "Error during check: " + e.getMessage());
            return false;
        }
    }

    /**
     * VALIDATE AND SYNC CONSISTENCY: Kiểm tra và tự động sửa inconsistency
     */
    @Transactional
    public void validateAndSyncConsistency(Long userId) {
        syncLoggingService.logManualOperation("VALIDATE_AND_SYNC", userId, "Validate and auto-fix consistency", "admin");
        
        try {
            UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("UserProfile not found for user ID: " + userId));
            
            User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new BusinessException("User not found for profile ID: " + profile.getUserId()));

            boolean hasInconsistency = false;
            Set<String> changedFields = new java.util.HashSet<>();
            
            // Check and fix email consistency
            if (!user.getEmail().equals(profile.getEmail())) {
                log.warn("Email inconsistency detected for user ID: {} - User: '{}', Profile: '{}'", 
                        userId, user.getEmail(), profile.getEmail());
                profile.setEmail(user.getEmail());
                changedFields.add("email");
                hasInconsistency = true;
            }
            
            // Check and fix email verification consistency
            if (!user.getIsVerified().equals(profile.getEmailVerified())) {
                log.warn("Email verification inconsistency detected for user ID: {} - User: {}, Profile: {}", 
                        userId, user.getIsVerified(), profile.getEmailVerified());
                profile.setEmailVerified(user.getIsVerified());
                changedFields.add("emailVerified");
                hasInconsistency = true;
            }
            
            // Check and fix grade consistency
            if (!java.util.Objects.equals(user.getGrade(), profile.getGrade())) {
                log.warn("Grade inconsistency detected for user ID: {} - User: '{}', Profile: '{}'", 
                        userId, user.getGrade(), profile.getGrade());
                profile.setGrade(user.getGrade());
                changedFields.add("grade");
                hasInconsistency = true;
            }
            
            // Check and fix goal consistency
            if (!java.util.Objects.equals(user.getGoal(), profile.getGoal())) {
                log.warn("Goal inconsistency detected for user ID: {} - User: '{}', Profile: '{}'", 
                        userId, user.getGoal(), profile.getGoal());
                profile.setGoal(user.getGoal());
                changedFields.add("goal");
                hasInconsistency = true;
            }
            
            if (hasInconsistency) {
                userProfileRepository.save(profile);
                log.info("Fixed consistency issues for user ID: {} - Fields: {}", userId, changedFields);
                syncLoggingService.logSyncSuccess("VALIDATE_AND_SYNC", userId, changedFields, 0);
            } else {
                log.debug("Data consistency validated successfully for user ID: {}", userId);
                syncLoggingService.logConsistencyCheck(userId, true, "No inconsistencies found");
            }
            
        } catch (Exception e) {
            log.error("Error during validateAndSyncConsistency for user ID: {}", userId, e);
            syncLoggingService.logSyncFailure("VALIDATE_AND_SYNC", userId, Set.of("validation"), e, 0);
            throw e;
        }
    }

    /**
     * FULL SYNC: Đồng bộ hoàn toàn UserProfile từ User
     */
    @Transactional
    public void fullSyncUserProfile(User user) {
        long startTime = System.currentTimeMillis();
        Set<String> allFields = Set.of("email", "isVerified", "grade", "goal");
        syncLoggingService.logSyncStart("FULL_SYNC", user.getId(), allFields);
        
        try {
            UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> createNewProfile(user));

            // Sync all fields
            profile.setEmail(user.getEmail());
            profile.setEmailVerified(user.getIsVerified());
            profile.setGrade(user.getGrade());
            profile.setGoal(user.getGoal());

            userProfileRepository.save(profile);
            
            long duration = System.currentTimeMillis() - startTime;
            syncLoggingService.logSyncSuccess("FULL_SYNC", user.getId(), allFields, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            syncLoggingService.logSyncFailure("FULL_SYNC", user.getId(), allFields, e, duration);
            throw e;
        }
    }

    /**
     * MANUAL SYNC: Cho trường hợp khẩn cấp
     */
    @Transactional
    public void manualSync(Long userId) {
        syncLoggingService.logManualOperation("MANUAL_SYNC", userId, "Emergency sync", "admin");
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("User not found: " + userId));

        fullSyncUserProfile(user);
    }

    // ===== SYSTEM INTEGRITY & DATA CONSISTENCY =====

    /**
     * INTEGRITY CHECK: Kiểm tra toàn bộ hệ thống
     */
    @Transactional(readOnly = true)
    public void performIntegrityCheck() {
        log.info("Starting system-wide data integrity check");
        
        var usersWithProfiles = userRepository.findAll().stream()
            .filter(user -> userProfileRepository.existsByUserId(user.getId()))
            .toList();
        
        int inconsistentCount = 0;
        
        for (User user : usersWithProfiles) {
            if (!isConsistent(user.getId())) {
                inconsistentCount++;
            }
        }
        
        var metrics = SyncLoggingService.MetricBuilder.create()
            .add("total_users", usersWithProfiles.size())
            .add("inconsistent_users", inconsistentCount)
            .add("consistency_rate", (double)(usersWithProfiles.size() - inconsistentCount) / usersWithProfiles.size())
            .build();
            
        syncLoggingService.logPerformanceMetrics("INTEGRITY_CHECK", metrics);
        
        log.info("Integrity check completed. Total: {}, Inconsistent: {}", 
                usersWithProfiles.size(), inconsistentCount);
    }

    /**
     * ENSURE PROFILE EXISTS: Đảm bảo mọi User đều có UserProfile
     */
    @Transactional
    public void ensureProfileExists(Long userId) {
        syncLoggingService.logManualOperation("ENSURE_PROFILE", userId, "Check and create if missing", "system");
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("User not found: " + userId));
        
        if (!userProfileRepository.existsByUserId(userId)) {
            log.info("Creating missing profile for user ID: {}", userId);
            UserProfile profile = createNewProfile(user);
            syncLoggingService.logManualOperation("PROFILE_CREATED", userId, "Auto-created missing profile", "system");
        }
    }

    /**
     * FIND ORPHAN PROFILES: Tìm UserProfile không có User tương ứng
     */
    @Transactional(readOnly = true)
    public java.util.List<Long> findOrphanProfiles() {
        log.info("Searching for orphan profiles");
        
        return userProfileRepository.findAll().stream()
            .filter(profile -> !userRepository.existsById(profile.getUserId()))
            .map(UserProfile::getUserId)
            .toList();
    }

    /**
     * FORCE SYNC ALL: Đồng bộ force cho toàn bộ hệ thống
     */
    @Transactional
    public void forceSyncAll() {
        log.warn("Performing FORCE SYNC for all users");
        syncLoggingService.logManualOperation("FORCE_SYNC_ALL", null, "System-wide force sync", "admin");
        
        java.util.List<User> allUsers = userRepository.findAll();
        int syncCount = 0;
        int errorCount = 0;
        
        for (User user : allUsers) {
            try {
                // Ensure profile exists
                ensureProfileExists(user.getId());
                
                // Perform full sync
                fullSyncUserProfile(user);
                syncCount++;
                
            } catch (Exception e) {
                log.error("Force sync failed for user ID: {}", user.getId(), e);
                errorCount++;
            }
        }
        
        var metrics = SyncLoggingService.MetricBuilder.create()
            .add("total_users", allUsers.size())
            .add("sync_success", syncCount)
            .add("sync_errors", errorCount)
            .add("success_rate", (double) syncCount / allUsers.size())
            .build();
            
        syncLoggingService.logPerformanceMetrics("FORCE_SYNC_ALL", metrics);
        
        log.warn("Force sync completed. Success: {}, Errors: {}, Total: {}", 
                syncCount, errorCount, allUsers.size());
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * SYNC SPECIFIC FIELDS: Clean & Extensible với Map-based approach
     */
    private boolean syncSpecificFields(UserProfile profile, User user, Set<String> changedFields) {
        AtomicBoolean updated = new AtomicBoolean(false);
        
        changedFields.forEach(field -> {
            BiConsumer<UserProfile, User> syncer = FIELD_SYNCERS.get(field);
            if (syncer != null) {
                syncer.accept(profile, user);
                updated.set(true);
                log.debug("Synced field '{}' for user ID: {}", field, user.getId());
            } else {
                log.warn("No syncer found for field: {}", field);
            }
        });
        
        return updated.get();
    }

    /**
     * CREATE NEW PROFILE - Idempotent với unique constraint guard
     */
    private UserProfile createNewProfile(User user) {
        log.info("Creating new UserProfile for user ID: {}", user.getId());
        
        // Sử dụng constructor mới để tránh lưu User object
        UserProfile profile = new UserProfile(
            user.getId(), 
            user.getEmail(), 
            user.getIsVerified(), 
            user.getGrade(), 
            user.getGoal()
        );
        
        try {
            return userProfileRepository.save(profile);
        } catch (DataIntegrityViolationException ex) {
            // 🔥 Concurrent create -> load existing and return after syncing
            log.info("UserProfile created concurrently for user ID: {}, loading and syncing...", user.getId());
            UserProfile existing = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("UserProfile not found after concurrent creation: " + user.getId()));
            
            // Sync fields to ensure consistency
            syncSpecificFields(existing, user, Set.of("email", "isVerified", "grade", "goal"));
            return userProfileRepository.save(existing);
        }
    }

    // ===== NEW METHODS FOR CONTROLLER =====

    /**
     * SYNC USER PROFILE: Manual sync method for controller
     */
    @Transactional
    public void syncUserProfile(User user) {
        fullSyncUserProfile(user);
    }

    /**
     * CHECK DATA INTEGRITY: Check data integrity for a user
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkDataIntegrity(User user) {
        boolean isConsistent = isConsistent(user.getId());
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("userId", user.getId());
        result.put("isConsistent", isConsistent);
        result.put("timestamp", System.currentTimeMillis());
        
        if (!isConsistent) {
            result.put("message", "Data inconsistency detected");
        } else {
            result.put("message", "Data is consistent");
        }
        
        return result;
    }

    /**
     * GET SYNC STATS: Get sync statistics for a user
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSyncStats(User user) {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("userId", user.getId());
        stats.put("hasProfile", userProfileRepository.existsByUserId(user.getId()));
        stats.put("isConsistent", isConsistent(user.getId()));
        stats.put("lastChecked", System.currentTimeMillis());
        
        return stats;
    }
}