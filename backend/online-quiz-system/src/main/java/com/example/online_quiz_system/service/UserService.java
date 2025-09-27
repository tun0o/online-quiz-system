package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.Role;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       VerificationService verificationService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.emailService = emailService;
    }

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
        String verificationLink = "http://localhost:3000/confirm?token=" + rawToken;
        emailService.sendVerificationEmail(normalizedEmail, verificationLink);

        logger.info("Registered new user: {}", normalizedEmail);
    }

    public User processOAuthPostLogin(String provider, OAuth2UserInfo userInfo) {
        String providerId = userInfo.getId();
        String email = userInfo.getEmail() != null ?
                userInfo.getEmail().trim().toLowerCase() : null;

        // 1. Tìm theo provider info
        Optional<User> byProvider = userRepository.findByProviderAndProviderId(provider, providerId);
        if (byProvider.isPresent()) {
            return byProvider.get();
        }

        // 2. Nếu có email -> tìm user theo email
        if (email != null) {
            Optional<User> byEmail = userRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                User existing = byEmail.get();
                existing.setProvider(provider);
                existing.setProviderId(providerId);
                existing.setVerified(existing.isVerified() || userInfo.isEmailVerified());
                if (existing.getName() == null && userInfo.getName() != null) {
                    existing.setName(userInfo.getName());
                }
                userRepository.save(existing);
                return existing;
            }
        }

        // 3. Tạo user mới
        User newUser = User.builder()
                .email(email != null ? email : "no-email-" + provider + "-" + providerId + "@example.com")
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .name(userInfo.getName())
                .isVerified(userInfo.isEmailVerified())
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER)
                .build();

        userRepository.save(newUser);
        return newUser;
    }

    private void validateBusinessRules(String email, String password, String grade) {
        if (!email.endsWith("@gmail.com") && !email.endsWith("@email.com")) {
            throw new BusinessException("Chỉ chấp nhận email từ Gmail hoặc Email");
        }

        if (!isValidGrade(grade)) {
            throw new BusinessException("Lớp học không hợp lệ. Chỉ chấp nhận lớp 10, 11, 12");
        }

        if (password == null || password.isEmpty()) {
            throw new BusinessException("Mật khẩu là bắt buộc");
        }

        if (password.toLowerCase().contains("password") || password.contains("123456")) {
            throw new BusinessException("Mật khẩu quá yếu và dễ đoán");
        }
    }

    private boolean isValidGrade(String grade) {
        return grade != null && (grade.equals("10") || grade.equals("11") || grade.equals("12"));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String normalized = email.trim().toLowerCase();
        return userRepository.findByEmail(normalized);
    }

    public void resendVerificationByEmail(String email) {
        if (email == null || email.isBlank()) return;

        String normalized = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalized);
        if (userOpt.isPresent()) {
            logger.info("Resend verification requested for existing email: {}", normalized);
            verificationService.resendVerification(userOpt.get());
        } else {
            logger.debug("Resend verification requested for non-existing email: {}", normalized);
        }
    }
}