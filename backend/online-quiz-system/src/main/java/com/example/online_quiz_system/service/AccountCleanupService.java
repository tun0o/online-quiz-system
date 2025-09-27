package com.example.online_quiz_system.service;

import com.example.online_quiz_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(AccountCleanupService.class);

    private final UserRepository userRepository;

    @Value("${app.account.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${app.account.cleanup.grace-days:14}")
    private int graceDays;

    public AccountCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "${app.account.cleanup.cron:0 0 2 * * *}")
    @Transactional
    public void deleteOldUnverifiedAccounts() {
        if (!cleanupEnabled) {
            return;
        }
        LocalDateTime threshold = LocalDateTime.now().minusDays(graceDays);
        int deleted = userRepository.deleteUnverifiedUsersCreatedBefore(threshold);
        if (deleted > 0) {
            logger.info("Deleted {} unverified accounts older than {} days (threshold: {})", deleted, graceDays, threshold);
        } else {
            logger.debug("No unverified accounts to delete (threshold: {})", threshold);
        }
    }
}


