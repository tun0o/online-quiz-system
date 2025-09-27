package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.PasswordResetToken;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.repository.PasswordResetTokenRepository;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.util.HashUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private static final int TOKEN_EXPIRY_HOURS = 1;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                EmailService emailService,
                                org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void requestReset(String email) {
        if (email == null || email.isBlank()) return;
        String normalized = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalized).orElse(null);
        if (user == null) return; // ẩn sự tồn tại của email

        // Xóa token cũ
        tokenRepository.deleteByUser_Id(user.getId());

        // Tạo token mới
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = HashUtils.sha256(rawToken);

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS))
                .used(false)
                .build();
        tokenRepository.save(token);

        String frontendBaseUrl = System.getProperty("app.frontend.url", System.getenv().getOrDefault("APP_FRONTEND_URL", "http://localhost:3000"));
        String resetLink = frontendBaseUrl + "/reset-password?token=" + rawToken;
        emailService.sendPasswordResetEmail(normalized, resetLink);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) throw new BusinessException("Thiếu token");
        if (newPassword == null || newPassword.length() < 8) throw new BusinessException("Mật khẩu không hợp lệ");

        // Kiểm tra mật khẩu mạnh
        if (!isValidPassword(newPassword)) {
            throw new BusinessException("Mật khẩu phải có ít nhất 10 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
        }

        String tokenHash = HashUtils.sha256(rawToken);
        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash).orElse(null);
        if (token == null || token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token không hợp lệ hoặc đã hết hạn");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        // Xóa các token reset khác của user (nếu còn)
        try { tokenRepository.deleteByUser_Id(user.getId()); } catch (Exception ignore) {}
    }

    private boolean isValidPassword(String password) {
        // Kiểm tra mật khẩu có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}
