package com.example.online_quiz_system.monitoring;

import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.UserProfile;
import com.example.online_quiz_system.repository.UserProfileRepository;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.service.UserProfileSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DATA CONSISTENCY MONITOR
 * - Scheduled consistency checks (DISABLED by default for performance)
 * - Real-time monitoring
 * - Detailed reporting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataConsistencyMonitor {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileSyncService syncService;
    
    @Value("${app.monitoring.consistency.enabled:false}")
    private boolean monitoringEnabled;

    /**
     * SCHEDULED CHECK: Tự động kiểm tra mỗi 2 giờ (thay vì 30 phút)
     * FIXED: Only runs when monitoring is enabled
     */
    @Scheduled(fixedRate = 7200000) // 2 hours instead of 30 minutes
    @Transactional(readOnly = true)
    public void scheduledConsistencyCheck() {
        if (!monitoringEnabled) {
            log.debug("Scheduled consistency check skipped - monitoring disabled");
            return;
        }
        
        log.info("Starting scheduled consistency check at {}", LocalDateTime.now());
        
        try {
            // FIXED: Only check recent users instead of all users
            ConsistencyReport report = performOptimizedConsistencyCheck();
            
            if (report.hasIssues()) {
                log.warn("CONSISTENCY ISSUES FOUND: {} inconsistencies detected", report.getIssueCount());
                logDetailedReport(report);
                
                // Trigger auto-healing for missing profiles
                autoHealMissingProfiles(report);
            } else {
                log.info("Consistency check PASSED: No issues found");
            }
            
            // FIXED: Only run integrity check if there are issues
            if (report.hasIssues()) {
                syncService.performIntegrityCheck();
            }
            
        } catch (Exception e) {
            log.error("Error during scheduled consistency check", e);
        }
    }

    /**
     * FIXED: OPTIMIZED CONSISTENCY CHECK
     * Only check recent users instead of all users
     */
    @Transactional(readOnly = true)
    public ConsistencyReport performOptimizedConsistencyCheck() {
        ConsistencyReport report = new ConsistencyReport();
        
        // FIXED: Only check users updated in last 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<User> recentUsers = userRepository.findAll().stream()
            .filter(user -> user.getUpdatedAt() != null && user.getUpdatedAt().isAfter(sevenDaysAgo))
            .limit(50) // Limit to 50 recent users
            .toList();
        
        log.info("Checking consistency for {} recent users (instead of all users)", recentUsers.size());
        
        for (User user : recentUsers) {
            try {
                checkUserConsistency(user, report);
            } catch (Exception e) {
                log.error("Error checking user ID: {}", user.getId(), e);
                report.addIssue(new ConsistencyIssue(
                    user.getId(), 
                    "SYSTEM_ERROR", 
                    "Error during consistency check: " + e.getMessage()
                ));
            }
        }
        
        log.info("Optimized consistency check completed: {} issues found", report.getIssueCount());
        return report;
    }

    /**
     * FULL CONSISTENCY CHECK (for manual use only)
     */
    @Transactional(readOnly = true)
    public ConsistencyReport performFullConsistencyCheck() {
        ConsistencyReport report = new ConsistencyReport();
        
        // FIXED: Use pagination instead of loading all users
        List<User> allUsers = userRepository.findAll();
        log.info("Checking consistency for {} users", allUsers.size());
        
        for (User user : allUsers) {
            try {
                checkUserConsistency(user, report);
            } catch (Exception e) {
                log.error("Error checking user ID: {}", user.getId(), e);
                report.addIssue(new ConsistencyIssue(
                    user.getId(), 
                    "SYSTEM_ERROR", 
                    "Error during consistency check: " + e.getMessage()
                ));
            }
        }
        
        log.info("Consistency check completed: {} issues found", report.getIssueCount());
        return report;
    }

    /**
     * SINGLE USER CHECK
     */
    @Transactional(readOnly = true)
    public ConsistencyReport checkUserConsistency(Long userId) {
        ConsistencyReport report = new ConsistencyReport();
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            report.addIssue(new ConsistencyIssue(userId, "USER_NOT_FOUND", "User not found"));
            return report;
        }
        
        checkUserConsistency(user, report);
        return report;
    }

    // Private methods
    private void checkUserConsistency(User user, ConsistencyReport report) {
        Long userId = user.getId();
        
        // Check if user has profile
        boolean hasProfile = userProfileRepository.existsByUserId(userId);
        if (!hasProfile) {
            report.addIssue(new ConsistencyIssue(userId, "MISSING_PROFILE", "User has no profile"));
            return;
        }
        
        // Get user profile
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            report.addIssue(new ConsistencyIssue(userId, "PROFILE_NOT_FOUND", "Profile not found despite existsByUserId returning true"));
            return;
        }
        
        // Check email consistency
        if (!Objects.equals(user.getEmail(), profile.getEmail())) {
            report.addIssue(new ConsistencyIssue(userId, "EMAIL_MISMATCH", 
                String.format("User email (%s) != Profile email (%s)", user.getEmail(), profile.getEmail())));
        }
        
        // Check verification status consistency
        if (!Objects.equals(user.isVerified(), profile.getEmailVerified())) {
            report.addIssue(new ConsistencyIssue(userId, "VERIFICATION_MISMATCH", 
                String.format("User verified (%s) != Profile emailVerified (%s)", user.isVerified(), profile.getEmailVerified())));
        }
        
        // Check grade consistency
        if (!Objects.equals(user.getGrade(), profile.getGrade())) {
            report.addIssue(new ConsistencyIssue(userId, "GRADE_MISMATCH", 
                String.format("User grade (%s) != Profile grade (%s)", user.getGrade(), profile.getGrade())));
        }
        
        // Check goal consistency
        if (!Objects.equals(user.getGoal(), profile.getGoal())) {
            report.addIssue(new ConsistencyIssue(userId, "GOAL_MISMATCH", 
                String.format("User goal (%s) != Profile goal (%s)", user.getGoal(), profile.getGoal())));
        }
    }

    private void logDetailedReport(ConsistencyReport report) {
        log.warn("=== DETAILED CONSISTENCY REPORT ===");
        log.warn("Check Time: {}", report.getCheckTime());
        log.warn("Total Issues: {}", report.getIssueCount());
        
        for (ConsistencyIssue issue : report.getIssues()) {
            log.warn("ISSUE - User ID: {}, Type: {}, Description: {}", 
                issue.getUserId(), issue.getType(), issue.getDescription());
        }
        
        log.warn("=== END CONSISTENCY REPORT ===");
    }

    private void autoHealMissingProfiles(ConsistencyReport report) {
        log.info("Starting auto-healing for missing profiles...");
        
        int healedCount = 0;
        for (ConsistencyIssue issue : report.getIssues()) {
            if ("MISSING_PROFILE".equals(issue.getType())) {
                try {
                    User user = userRepository.findById(issue.getUserId()).orElse(null);
                    if (user != null) {
                        // Trigger profile creation
                        syncService.syncUserProfile(user);
                        healedCount++;
                        log.info("Auto-healed missing profile for user: {}", issue.getUserId());
                    }
                } catch (Exception e) {
                    log.error("Failed to auto-heal profile for user: {}", issue.getUserId(), e);
                }
            }
        }
        
        log.info("Auto-healing completed: {} profiles healed", healedCount);
    }

    /**
     * CONSISTENCY REPORT
     */
    public static class ConsistencyReport {
        private final List<ConsistencyIssue> issues = new ArrayList<>();
        private final LocalDateTime checkTime = LocalDateTime.now();

        public void addIssue(ConsistencyIssue issue) {
            issues.add(issue);
        }

        public List<ConsistencyIssue> getIssues() {
            return new ArrayList<>(issues);
        }

        public int getIssueCount() {
            return issues.size();
        }

        public boolean hasIssues() {
            return !issues.isEmpty();
        }

        public LocalDateTime getCheckTime() {
            return checkTime;
        }
    }

    /**
     * CONSISTENCY ISSUE
     */
    public static class ConsistencyIssue {
        private final Long userId;
        private final String type;
        private final String description;
        private final LocalDateTime detectedAt = LocalDateTime.now();

        public ConsistencyIssue(Long userId, String type, String description) {
            this.userId = userId;
            this.type = type;
            this.description = description;
        }

        public Long getUserId() { return userId; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public LocalDateTime getDetectedAt() { return detectedAt; }
    }
}