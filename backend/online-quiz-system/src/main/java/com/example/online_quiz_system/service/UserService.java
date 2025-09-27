package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.LoginSession;
import com.example.online_quiz_system.entity.Role;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.exception.BusinessException;
 
import com.example.online_quiz_system.repository.UserRepository;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.online_quiz_system.entity.AuditLog;
import com.example.online_quiz_system.entity.RefreshToken;
import com.example.online_quiz_system.repository.RefreshTokenRepository;
import com.example.online_quiz_system.repository.AuditLogRepository;
import java.time.LocalDateTime;

import java.util.Base64;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogRepository auditLogRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       VerificationService verificationService,
                       EmailService emailService,
                       RefreshTokenRepository refreshTokenRepository,
                       AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.emailService = emailService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // Đăng ký người dùng mới
    public void registerUser(String email, String password, String grade, String goal) {
        if (email == null) throw new BusinessException("Email là bắt buộc");

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Email đã được đăng ký");
        }

        validateBusinessRules(normalizedEmail, password, grade);

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(password))
                .grade(grade)
                .goal(goal)
                .isVerified(false)
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String rawToken = verificationService.createTokenForUser(user);
        String frontendBaseUrl = System.getProperty("app.frontend.url", System.getenv().getOrDefault("APP_FRONTEND_URL", "http://localhost:3000"));
        String verificationLink = frontendBaseUrl + "/confirm?token=" + rawToken;
        emailService.sendVerificationEmail(normalizedEmail, verificationLink);

        logger.info("Registered new user: {}", normalizedEmail);
    }

    // Gửi lại email xác thực theo địa chỉ email
    public void resendVerificationByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email là bắt buộc");
        }
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        if (user == null) {
            throw new BusinessException("Email không tồn tại trong hệ thống");
        }
        verificationService.resendVerification(user);
    }

    

    // Validate các quy tắc nghiệp vụ
    private void validateBusinessRules(String email, String password, String grade) {
        // Email format is validated at entity level (@Email); do not restrict domains here.
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email không hợp lệ");
        }

        if (!isValidGrade(grade)) {
            throw new BusinessException("Lớp học không hợp lệ. Chỉ chấp nhận lớp 10, 11, 12");
        }

        if (password == null || password.isEmpty()) {
            throw new BusinessException("Mật khẩu là bắt buộc");
        }
        // Strengthened policy: length >= 10, include upper, lower, digit, special
        if (password.length() < 10) {
            throw new BusinessException("Mật khẩu phải có ít nhất 10 ký tự");
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;:'\",.<>/?`~".indexOf(c) >= 0);
        if (!(hasUpper && hasLower && hasDigit && hasSpecial)) {
            throw new BusinessException("Mật khẩu phải chứa chữ hoa, chữ thường, số và ký tự đặc biệt");
        }
        String lower = password.toLowerCase();
        if (lower.contains("password") || lower.contains("123456") || lower.contains("qwerty") || lower.contains("admin")) {
            throw new BusinessException("Mật khẩu quá phổ biến và dễ đoán");
        }
    }

    private boolean isValidGrade(String grade) {
        return grade != null && (grade.equals("10") || grade.equals("11") || grade.equals("12"));
    }

    // Đổi mật khẩu và thu hồi toàn bộ refresh tokens của người dùng
    @Transactional
    public void changePasswordAndRevokeAll(User user, String currentPassword, String newPassword) {
        if (user == null) throw new BusinessException("Người dùng không tồn tại");
        if (currentPassword == null || newPassword == null) throw new BusinessException("Thiếu mật khẩu");
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException("Mật khẩu mới quá ngắn");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xóa tất cả refresh token của user trong DB (thu hồi toàn bộ thiết bị)
        try {
            refreshTokenRepository.deleteByUser_Id(user.getId());
        } catch (Exception ignore) {}
    }

    // Validate IP trước khi lưu session
    @SuppressWarnings("unused")
    public void saveLoginSession(LoginSession session) {
        if (!InetAddressValidator.getInstance().isValid(session.getIpAddress())) {
            throw new IllegalArgumentException("Invalid IP address");
        }
        // ...code lưu session...
    }

    // Ghi audit log
    private void logAction(User user, String action, String ip, String device, String details) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setIpAddress(ip);
        log.setDeviceFingerprint(device);
        log.setDescription(details);
         auditLogRepository.save(log);
    }

    // Xử lý refresh token: kiểm tra device/IP, rotate token, revoke token cũ, ghi log
    @SuppressWarnings("unused")
    public String handleRefreshToken(String refreshTokenPlain, String deviceFingerprint, String ipAddress) {
        String tokenHash = hashToken(refreshTokenPlain); // SHA256
        RefreshToken oldToken = refreshTokenRepository.findByTokenHash(tokenHash).orElse(null);
        if (oldToken == null || oldToken.isRevoked() || oldToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            logAction(null, "REFRESH_FAIL", ipAddress, deviceFingerprint, "Token not found or expired");
            throw new BusinessException("Refresh token invalid or expired");
        }
        // Kiểm tra device/IP
        if (!deviceFingerprint.equals(oldToken.getDeviceFingerprint()) || !ipAddress.equals(oldToken.getIpAddress())) {
            logAction(oldToken.getUser(), "REFRESH_FAIL", ipAddress, deviceFingerprint, "Device/IP mismatch");
            // Có thể khóa tài khoản hoặc cảnh báo nếu nghi ngờ
            throw new BusinessException("Device or IP mismatch");
        }
        // Rotate: tạo token mới, revoke token cũ
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        String newRefreshTokenPlain = generateSecureToken();
        String newTokenHash = hashToken(newRefreshTokenPlain);
        RefreshToken newToken = RefreshToken.builder()
                .user(oldToken.getUser())
                .tokenHash(newTokenHash)
                .deviceFingerprint(deviceFingerprint)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(newToken);
        logAction(oldToken.getUser(), "REFRESH_SUCCESS", ipAddress, deviceFingerprint, "Refresh token rotated");
        return newRefreshTokenPlain;
    }

    // Hàm sinh token ngẫu nhiên mạnh
    private String generateSecureToken() {
        // Use SecureRandom + Base64 url-safe to generate a strong token and avoid unnecessary toString warnings
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // Hàm hash token SHA256
    private String hashToken(String tokenPlain) {
        // Use project's HashUtils to return consistent hex-format SHA-256 across the codebase
        return com.example.online_quiz_system.util.HashUtils.sha256(tokenPlain);
    }

    // Các hàm ghi log AuditLog cho các hành động nhạy cảm
    // Đăng nhập, refresh token (rotation), logout/revoke session
    public void logLogin(User user, String ip, String device) {
        logAction(user, "LOGIN", ip, device, "User logged in");
    }

    public void logLogout(User user, String ip, String device) {
        logAction(user, "LOGOUT", ip, device, "User logged out");
    }

    public void logRefreshTokenRotation(User user, String ip, String device) {
        logAction(user, "REFRESH_ROTATE", ip, device, "Refresh token rotated");
    }

    @SuppressWarnings("unused")
	public void logRevokeSession(User user, String ip, String device) {
        logAction(user, "REVOKE_SESSION", ip, device, "Session revoked");
    }
}
