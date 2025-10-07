package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.UserUpdateRequest;
import com.example.online_quiz_system.entity.OAuth2Account;
import com.example.online_quiz_system.service.OAuth2UserInfo;
import com.example.online_quiz_system.entity.Role;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.VerificationToken;
import com.example.online_quiz_system.event.UserCreatedEvent;
import com.example.online_quiz_system.event.UserUpdatedEvent;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.repository.OAuth2AccountRepository;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.repository.VerificationTokenRepository;
import com.example.online_quiz_system.util.LogMaskingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final OAuth2AccountRepository oauth2AccountRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Process OAuth2 post-login
     */
    public User processOAuthPostLogin(String provider, String providerId, OAuth2UserInfo userInfo) {
        log.info("Processing OAuth2 post-login for provider: {}, providerId: {}", provider, providerId);
        
        // Check if OAuth2Account already exists
        Optional<OAuth2Account> existingAccount = oauth2AccountRepository.findByProviderAndProviderId(provider, providerId);
        
        if (existingAccount.isPresent()) {
            OAuth2Account account = existingAccount.get();
            User user = account.getUser();
            
            // Update last used timestamp
            account.setLastUsedAt(LocalDateTime.now());
            oauth2AccountRepository.save(account);
            
            log.info("Found existing OAuth2Account for user: {}", user.getEmail());
            return user;
        }
        
        // Check if user exists by email
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            // Create new OAuth2Account for existing user
            OAuth2Account newAccount = OAuth2Account.builder()
                    .user(user)
                    .provider(provider)
                    .providerId(providerId)
                    .providerName(userInfo.getName())
                    .providerPicture(userInfo.getImageUrl())
                    .providerEmail(userInfo.getEmail())
                    .providerPhone(userInfo.getPhone())
                    .providerBirthday(userInfo.getBirthday())
                    .providerGender(userInfo.getGender())
                    .providerLocale(userInfo.getLocale())
                    .isPrimary(false) // Not primary since user already exists
                    .lastUsedAt(LocalDateTime.now())
                    .linkedAt(LocalDateTime.now())
                    .build();
            
            oauth2AccountRepository.save(newAccount);
            
            log.info("Created new OAuth2Account for existing user: {}", user.getEmail());
            return user;
        }
        
        // Create new user and OAuth2Account
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .passwordHash(null) // OAuth2 users don't have password
                .isVerified(true) // OAuth2 users are pre-verified
                .role(Role.USER)
                .grade(null)
                .goal(null)
                .build();
        
        User savedUser = userRepository.save(newUser);
        
        // Create OAuth2Account
        OAuth2Account oauth2Account = OAuth2Account.builder()
                .user(savedUser)
                .provider(provider)
                .providerId(providerId)
                .providerName(userInfo.getName())
                .providerPicture(userInfo.getImageUrl())
                .providerEmail(userInfo.getEmail())
                .providerPhone(userInfo.getPhone())
                .providerBirthday(userInfo.getBirthday())
                .providerGender(userInfo.getGender())
                .providerLocale(userInfo.getLocale())
                .isPrimary(true) // Primary account for new user
                .lastUsedAt(LocalDateTime.now())
                .linkedAt(LocalDateTime.now())
                .build();
        
        oauth2AccountRepository.save(oauth2Account);
        
        // Publish event to create UserProfile
        eventPublisher.publishEvent(new UserCreatedEvent(savedUser.getId()));
        
        log.info("Created new user and OAuth2Account: {}", savedUser.getEmail());
        return savedUser;
    }

    /**
     * Get user by email with caching
     */
    @Cacheable(value = "users_by_email", key = "#email")
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    /**
     * Get user by OAuth provider and provider ID with caching
     */
    @Cacheable(value = "users_by_provider", key = "#provider + '_' + #providerId")
    public Optional<User> getUserByProviderAndProviderId(String provider, String providerId) {
        return oauth2AccountRepository.findByProviderAndProviderId(provider, providerId)
                .map(OAuth2Account::getUser);
    }

    /**
     * Register new user with email and password
     */
    public User registerUser(String email, String password, String grade, String goal) {
        email = normalizeEmail(email);

        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Email da duoc dang ky");
        }

        // Create new user
        User newUser = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .isVerified(false)
                .role(Role.USER)
                .grade(grade)
                .goal(goal)
                .build();

        User savedUser = userRepository.save(newUser);

        // Publish event to create UserProfile and send verification email
        eventPublisher.publishEvent(new UserCreatedEvent(savedUser.getId()));

        log.info("Registered new user: {}", LogMaskingUtils.maskEmail(email));
        return savedUser;
    }

    /**
     * Resend verification email
     */
    public void resendVerificationByEmail(String email) {
        email = normalizeEmail(email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            // Don't reveal if email exists or not for security
            log.warn("Attempted to resend verification for non-existent email: {}", LogMaskingUtils.maskEmail(email));
            return;
        }

        User user = userOptional.get();
        if (user.isVerified()) {
            throw new BusinessException("Email da duoc xac thuc");
        }

        // Publish event to send verification email
        eventPublisher.publishEvent(new UserCreatedEvent(user.getId()));

        log.info("Resent verification email for user: {}", LogMaskingUtils.maskEmail(email));
    }

    /**
     * Get user by ID
     */
    @Cacheable(value = "users_by_id", key = "#id")
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find all users
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Update user
     */
    @CacheEvict(value = {"users_by_id", "users_by_email", "users_with_profile"}, key = "#id")
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay nguoi dung"));

        Set<String> changedFields = new HashSet<>();
        // Update user fields
        if (request.getGrade() != null && !Objects.equals(user.getGrade(), request.getGrade())) {
            user.setGrade(request.getGrade());
            changedFields.add("grade");
        }
        if (request.getGoal() != null && !Objects.equals(user.getGoal(), request.getGoal())) {
            user.setGoal(request.getGoal());
            changedFields.add("goal");
        }

        User updatedUser = userRepository.save(user);

        // Publish event with changed fields
        if (!changedFields.isEmpty()) {
            eventPublisher.publishEvent(new UserUpdatedEvent(updatedUser.getId(), changedFields));
        }

        log.info("Updated user: {}", user.getEmail());
        return updatedUser;
    }

    /**
     * Normalize email address
     */
    private String normalizeEmail(String email) {
        return email != null ? email.toLowerCase().trim() : null;
    }
}