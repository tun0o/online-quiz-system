package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.PasswordResetToken;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.repository.PasswordResetTokenRepository;
import com.example.online_quiz_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_EXPIRY_HOURS = 1; // 1 giờ
    private static final int RESEND_RATE_MINUTES = 5; // 5 phút
    
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                               UserRepository userRepository,
                               EmailService emailService,
                               PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public String createResetTokenForUser(User user) {
        logger.info("Generating password reset token for user: {}", user.getEmail());
        
        // Xóa token cũ
        tokenRepository.deleteByUser(user);
        
        // Tạo token mới
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        
        String hashedToken = hashToken(rawToken);
        
        PasswordResetToken token = PasswordResetToken.builder()
                .tokenHash(hashedToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        tokenRepository.save(token);
        logger.info("Password reset token saved for user: {} (expires: {})", user.getEmail(), token.getExpiresAt());
        
        return rawToken;
    }
    
    @Transactional
    public void sendPasswordResetEmail(String email) {
        email = email.trim().toLowerCase();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Email không tồn tại trong hệ thống"));
        
        // Kiểm tra rate limiting
        Optional<PasswordResetToken> lastToken = tokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (lastToken.isPresent()) {
            LocalDateTime lastSent = lastToken.get().getCreatedAt();
            if (LocalDateTime.now().isBefore(lastSent.plus(RESEND_RATE_MINUTES, ChronoUnit.MINUTES))) {
                throw new BusinessException("Vui lòng đợi " + RESEND_RATE_MINUTES + " phút trước khi gửi lại email reset mật khẩu");
            }
        }
        
        // Xóa token cũ, tạo token mới và gửi email
        tokenRepository.deleteByUser(user);
        String rawToken = createResetTokenForUser(user);
        
        String resetLink = "http://localhost:3000/reset-password?token=" + rawToken;
        logger.info("Sending password reset email to {} with link {}", user.getEmail(), resetLink);
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        logger.info("Password reset email sent to {}", user.getEmail());
    }
    
    @Transactional
    public boolean resetPassword(String rawToken, String newPassword) {
        logger.info("Processing password reset");
        
        String hashedToken = hashToken(rawToken);
        Optional<PasswordResetToken> opt = tokenRepository.findByTokenHash(hashedToken);
        
        if (opt.isEmpty()) {
            logger.warn("Password reset token not found");
            return false;
        }
        
        PasswordResetToken token = opt.get();
        User user = token.getUser();
        
        if (token.isUsed()) {
            logger.warn("Password reset token already used for user: {}", user.getEmail());
            return false;
        }
        
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("Password reset token expired for user: {}", user.getEmail());
            return false;
        }
        
        // Cập nhật mật khẩu
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
        
        // Đánh dấu token đã sử dụng
        token.setUsed(true);
        tokenRepository.save(token);
        
        logger.info("Password reset successfully for user: {}", user.getEmail());
        return true;
    }
    
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Người dùng không tồn tại"));
        
        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng");
        }
        
        // Cập nhật mật khẩu mới
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
        
        logger.info("Password changed successfully for user: {}", user.getEmail());
    }
    
    @Scheduled(cron = "0 0 * * * *") // mỗi giờ
    @Transactional
    public void cleanupExpiredTokens() {
        List<PasswordResetToken> expired = tokenRepository.findAllByExpiresAtBefore(LocalDateTime.now());
        if (!expired.isEmpty()) {
            tokenRepository.deleteAll(expired);
            logger.info("Deleted {} expired password reset tokens", expired.size());
        }
    }
    
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Token hashing failed", e);
        }
    }
}

