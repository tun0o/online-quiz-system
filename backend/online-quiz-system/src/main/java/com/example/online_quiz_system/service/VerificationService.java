package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.VerificationToken;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
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
public class VerificationService {
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);
    private static final int TOKEN_EXPIRY_HOURS = 24;
    private static final int RESEND_RATE_MINUTES = 5;

    public VerificationService(VerificationTokenRepository tokenRepository,
                               UserRepository userRepository,
                               EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String createTokenForUser(User user) {
        logger.info("Generating verification token for user: {}", user.getEmail());

        // Xóa token cũ
        tokenRepository.deleteByUser(user);

        // Tạo token mới
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        logger.debug("Raw token generated for {}: {}", user.getEmail(), rawToken);

        String hashedToken = hashToken(rawToken);
        logger.debug("Hashed token for {}: {}", user.getEmail(), hashedToken);

        VerificationToken token = VerificationToken.builder()
                .tokenHash(hashedToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        tokenRepository.save(token);
        logger.info("Token saved to DB for user: {} (expires: {})", user.getEmail(), token.getExpiresAt());

        return rawToken;
    }

    /**
     * Resend verification: enforce rate limiting, create token and send email.
     */
    @Transactional
    public void resendVerification(User user) {
        logger.info("Resending verification for user: {}", user.getEmail());

        // Kiểm tra rate limiting
        Optional<VerificationToken> lastToken = tokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (lastToken.isPresent()) {
            LocalDateTime lastSent = lastToken.get().getCreatedAt();
            logger.debug("Last verification token for {} was sent at {}", user.getEmail(), lastSent);
            if (LocalDateTime.now().isBefore(lastSent.plus(RESEND_RATE_MINUTES, ChronoUnit.MINUTES))) {
                logger.warn("User {} attempted to resend verification within {} minutes", user.getEmail(), RESEND_RATE_MINUTES);
                throw new BusinessException("Vui lòng đợi " + RESEND_RATE_MINUTES + " phút trước khi gửi lại email xác thực");
            }
        }

        // Xóa token cũ, tạo token mới và gửi email
        tokenRepository.deleteByUser(user);
        String rawToken = createTokenForUser(user);

        String verificationLink = "http://localhost:3000/confirm?token=" + rawToken;
        logger.info("Sending verification email to {} with link {}", user.getEmail(), verificationLink);
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
        logger.info("Resend verification email sent to {}", user.getEmail());
    }

    @Transactional
    public boolean verifyToken(String rawToken) {
        logger.info("Starting token verification");

        String hashedToken = hashToken(rawToken);
        logger.debug("Hashed token to verify: {}", hashedToken);

        Optional<VerificationToken> opt = tokenRepository.findByTokenHash(hashedToken);
        if (opt.isEmpty()) {
            logger.warn("Token not found in DB");
            return false;
        }

        VerificationToken token = opt.get();
        User user = token.getUser();

        if (token.isUsed()) {
            logger.warn("Token already used for user: {}", user.getEmail());
            return false;
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("Token expired for user: {}", user.getEmail());
            return false;
        }

        token.setUsed(true);
        tokenRepository.save(token);
        logger.info("Token marked as used for user: {}", user.getEmail());

        user.setVerified(true);
        userRepository.save(user);
        logger.info("User {} verified successfully", user.getEmail());

        return true;
    }

    @Transactional
    public void removeAllTokensForUser(Long userId) {
        logger.info("Removing all tokens for userId={}", userId);
        tokenRepository.deleteByUser_Id(userId);
    }

    public Optional<VerificationToken> findValidTokenForUser(Long userId) {
        return tokenRepository.findAllByUser_Id(userId).stream()
                .filter(t -> !t.isUsed() && t.getExpiresAt().isAfter(LocalDateTime.now()))
                .findFirst();
    }

    @Scheduled(cron = "0 0 * * * *") // mỗi giờ
    @Transactional
    public void cleanupExpiredTokens() {
        List<VerificationToken> expired = tokenRepository.findAllByExpiresAtBefore(LocalDateTime.now());
        if (!expired.isEmpty()) {
            tokenRepository.deleteAll(expired);
            logger.info("Deleted {} expired verification tokens", expired.size());
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
