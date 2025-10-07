package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.event.UserCreatedEvent;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.util.LogMaskingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event Listener for handling email verification when user is created
 * This service listens to UserCreatedEvent and sends verification email
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class EmailVerificationEventListener {

    private final VerificationService verificationService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    
    @Value("${app.frontend.origin:http://localhost:3000}")
    private String frontendOrigin;

    /**
     * Handle UserCreatedEvent - Send verification email for new users
     * Only sends email for users that are not verified (regular registration)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("emailVerificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserCreated(UserCreatedEvent event) {
        Long userId = event.getUserId();
        
        log.info("🔥 EMAIL VERIFICATION LISTENER TRIGGERED for user ID: {}", userId);
        
        try {
            log.info("Processing email verification for user ID: {}", userId);
            
            // Load fresh user from DB with new transaction
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            // Only send verification email for unverified users (regular registration)
            if (!user.isVerified()) {
                log.info("Sending verification email for unverified user: {}", LogMaskingUtils.maskEmail(user.getEmail()));
                
                // Create verification token
                String rawToken = verificationService.createTokenForUser(user);
                
                // Build verification link using configurable frontend origin
                String verificationLink = frontendOrigin + "/confirm?token=" + rawToken;
                
                // Send verification email
                emailService.sendVerificationEmail(user.getEmail(), verificationLink);
                
                log.info("Verification email sent successfully to: {}", LogMaskingUtils.maskEmail(user.getEmail()));
            } else {
                log.info("User {} is already verified, skipping email verification", LogMaskingUtils.maskEmail(user.getEmail()));
            }
            
        } catch (Exception e) {
            log.error("Failed to send verification email for user ID: {}", userId, e);
            // Don't rethrow exception to avoid breaking the main transaction
        }
    }
}
