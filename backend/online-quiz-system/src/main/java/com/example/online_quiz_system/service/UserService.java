package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.*;
import com.example.online_quiz_system.entity.QuizAttempt;
import com.example.online_quiz_system.entity.UserRanking;
import com.example.online_quiz_system.enums.Role;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.enums.SubmissionStatus;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.mapper.UserMapper;
import com.example.online_quiz_system.repository.QuizAttemptRepository;
import com.example.online_quiz_system.repository.QuizSubmissionRepository;
import com.example.online_quiz_system.repository.UserRankingRepository;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.util.OAuth2Validation;
import com.example.online_quiz_system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final UserRankingRepository userRankingRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, VerificationService verificationService, EmailService emailService, UserMapper userMapper,
                       UserRankingRepository userRankingRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       QuizSubmissionRepository quizSubmissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.emailService = emailService;
        this.userMapper = userMapper;
        this.userRankingRepository = userRankingRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
    }

    public void registerUser(String email, String password, String grade, String goal) {
        if (email == null) throw new BusinessException("Email là bắt buộc");

        String normalizedEmail = email.trim().toLowerCase();
        String name = email.trim().split("@")[0];

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Email đã được đăng ký");
        }

        validateBusinessRules(normalizedEmail, password, grade);

        User user = User.builder()
                .email(normalizedEmail)
                .name(name)
                .passwordHash(passwordEncoder.encode(password))
                .grade(grade)
                .goal(goal)
                .verified(false)
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String rawToken = verificationService.createTokenForUser(user);
        String verificationLink = "http://localhost:3000/confirm?token=" + rawToken;
        emailService.sendVerificationEmail(normalizedEmail, verificationLink);

        logger.info("Registered new user: {}", normalizedEmail);
    }

    /**
     * Processes OAuth2 post-login by finding existing user or creating new one.
     * Handles linking social accounts to existing users and creates new users when needed.
     * 
     * @param provider The OAuth2 provider (google, facebook)
     * @param userInfo User information from OAuth2 provider
     * @return User entity (existing or newly created)
     * @throws BusinessException if there are conflicts with existing accounts
     */
    public User processOAuthPostLogin(String provider, OAuth2UserInfo userInfo) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        Objects.requireNonNull(userInfo, "UserInfo cannot be null");
        
        String providerId = userInfo.getId();
        String email = normalizeEmail(userInfo.getEmail());
        String name = userInfo.getName();

        // Validate OAuth2 data
        OAuth2Validation.validateOAuth2UserData(provider, providerId, email, name);

        logger.info("Processing OAuth2 post-login for provider: {}, email: {}", provider, email);

        // 1. Find existing user by provider and provider ID
        Optional<User> existingByProvider = userRepository.findByProviderAndProviderId(provider, providerId);
        if (existingByProvider.isPresent()) {
            User user = existingByProvider.get();

            if(!user.isEnabled())
                throw new AccessDeniedException("User has been disable by admin!");

            logger.info("Found existing user by provider: {}", user.getEmail());
            return updateUserFromOAuth2(user, userInfo);
        }

        // 2. If email exists, find user by email and link social account
        if (email != null) {
            Optional<User> existingByEmail = userRepository.findByEmail(email);
            if (existingByEmail.isPresent()) {
                User user = existingByEmail.get();

                if (!user.isEnabled())
                    throw new AccessDeniedException("User has been disable by admin!");

                logger.info("Found existing user by email, linking social account: {}", user.getEmail());
                return linkSocialAccountToUser(user, provider, providerId, userInfo);
            }
        }

        // 3. Create new user
        logger.info("Creating new user for OAuth2 login: {}", email);
        return createNewOAuth2User(provider, providerId, email, name, userInfo);
    }

    /**
     * Updates existing user with fresh OAuth2 data.
     */
    private User updateUserFromOAuth2(User user, OAuth2UserInfo userInfo) {
        boolean updated = false;
        
        // Update verification status
        if (userInfo.isEmailVerified() && !user.isVerified()) {
            user.setVerified(true);
            updated = true;
        }
        
        // Update name if not set or if OAuth2 provides better name
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            if (userInfo.getName() != null && !userInfo.getName().trim().isEmpty()) {
                user.setName(userInfo.getName());
                updated = true;
            }
        }
        
        if (updated) {
            userRepository.save(user);
            logger.info("Updated user from OAuth2: {}", user.getEmail());
        }
        
        return user;
    }

    /**
     * Links social account to existing user.
     */
    private User linkSocialAccountToUser(User user, String provider, String providerId, OAuth2UserInfo userInfo) {
        // Check if user already has a different provider
        if (user.getProvider() != null && !user.getProvider().equals(provider)) {
            throw new BusinessException(
                String.format("Email đã được đăng ký với %s. Vui lòng đăng nhập bằng %s hoặc sử dụng email khác.", 
                    user.getProvider(), user.getProvider())
            );
        }
        
        // Link the social account
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setVerified(user.isVerified() || userInfo.isEmailVerified());
        
        // Update name if not set
        if ((user.getName() == null || user.getName().trim().isEmpty()) && 
            userInfo.getName() != null && !userInfo.getName().trim().isEmpty()) {
            user.setName(userInfo.getName());
        }
        
        userRepository.save(user);
        logger.info("Linked social account {} to user: {}", provider, user.getEmail());
        return user;
    }

    /**
     * Creates new user from OAuth2 data.
     */
    private User createNewOAuth2User(String provider, String providerId, String email, String name, OAuth2UserInfo userInfo) {
        // Generate fallback email if none provided
        String finalEmail = email != null ? email : generateFallbackEmail(provider, providerId);
        
        User newUser = User.builder()
                .email(finalEmail)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .name(name)
                .verified(userInfo.isEmailVerified())
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER)
                .build();

        userRepository.save(newUser);
        logger.info("Created new OAuth2 user: {} with provider: {}", finalEmail, provider);
        return newUser;
    }

    /**
     * Normalizes email address.
     */
    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    /**
     * Generates fallback email for users without email from OAuth2.
     */
    private String generateFallbackEmail(String provider, String providerId) {
        return String.format("no-email-%s-%s@example.com", provider, providerId);
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

    public Page<UserAdminDTO> getAllUserForAdmin(String keyword, String role, String enabled, String verified, Pageable pageable){
        Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            String keywordPattern = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("email")), keywordPattern),
                            cb.like(cb.lower(root.get("name")), keywordPattern)
                    )
            );
        }

        if (role != null && !role.isBlank()) {
            try {
                Role roleEnum = Role.valueOf(role.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), roleEnum));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid role value provided: {}", role);
            }
        }

        if (enabled != null && !enabled.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("enabled"), Boolean.parseBoolean(enabled)));
        }

        if (verified != null && !verified.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("verified"), Boolean.parseBoolean(verified)));
        }

        return userRepository.findAll(spec, pageable).map(userMapper::toUserAdminDTO);
    }

    public UserAdminDTO getUserByIdForAdmin(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        return  userMapper.toUserAdminDTO(user);
    }

    @Transactional
    public UserAdminDTO updateUserByAdmin(Long userId, UserUpdateRequest updateRequest, UserPrincipal adminPrincipal){
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if(userToUpdate.getId().equals(adminPrincipal.getId()))
            throw new AccessDeniedException("Admin không thể tự cập nhật thông tin của chính mình qua API này.");

        if(userToUpdate.getRole() == Role.ADMIN && updateRequest.getRole() != Role.ADMIN)
            throw new AccessDeniedException("Không thể thay đổi vai trò của một Admin khác.");

        userToUpdate.setName(updateRequest.getName());
        userToUpdate.setGrade(updateRequest.getGrade());
        userToUpdate.setGoal(updateRequest.getGoal());
        userToUpdate.setRole(updateRequest.getRole());
        userToUpdate.setEnabled(updateRequest.getEnabled());
        userToUpdate.setVerified(updateRequest.getVerified());

        User updatedUser = userRepository.save(userToUpdate);
        return userMapper.toUserAdminDTO(updatedUser);
    }

    @Transactional
    public UserAdminDTO createUserByAdmin(UserCreateRequest createRequest) {
        String normalizedEmail = createRequest.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Email đã tồn tại trong hệ thống.");
        }

        User newUser = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(createRequest.getPassword()))
                .name(createRequest.getName())
                .grade(null)
                .goal(null)
                .role(createRequest.getRole())
                .verified(true) // Tài khoản do admin tạo được xác thực sẵn
                .enabled(true) // và được kích hoạt sẵn
                .build();

        User savedUser = userRepository.save(newUser);
        logger.info("Admin created a new user: {} with role: {}", savedUser.getEmail(), savedUser.getRole());
        return userMapper.toUserAdminDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDashboardStatsDTO getDashboardStatsForUser(Long userId){
        UserRanking ranking = userRankingRepository.findByUserId(userId)
                .orElse(new UserRanking());

        if(ranking.getUserId() == null) ranking.setUserId(userId);

        Integer totalPoints = ranking.getTotalPoints();
        Integer currentStreak = ranking.getCurrentStreak();
        Integer rank = userRankingRepository.findUserRankByUserId(userId);

        long quizzesTaken = quizAttemptRepository.countByUserIdAndStatus(userId, "COMPLETED");

        long submitted = quizSubmissionRepository.countByContributorId(userId);
        long approved = quizSubmissionRepository.countByContributorIdAndStatus(userId, SubmissionStatus.APPROVED);
        long pending = quizSubmissionRepository.countByContributorIdAndStatus(userId, SubmissionStatus.PENDING);
        long rejected = quizSubmissionRepository.countByContributorIdAndStatus(userId, SubmissionStatus.REJECTED);
        ContributionStatsDTO contributionStatsDTO = new ContributionStatsDTO(submitted, approved, pending, rejected);

        List<QuizAttempt> recentAttemptRaw = quizAttemptRepository.findByUserIdAndEndTimeIsNotNullOrderByEndTimeDesc(
                userId,
                PageRequest.of(0, 5)
        );

        List<RecentAttemptDTO> recentAttemptDTOS = recentAttemptRaw.stream()
                .map(this::mapToRecentAttemptDTO)
                .toList();

        return new UserDashboardStatsDTO(
                totalPoints,
                currentStreak,
                rank,
                quizzesTaken,
                contributionStatsDTO,
                recentAttemptDTOS
        );
    }

    private RecentAttemptDTO mapToRecentAttemptDTO(QuizAttempt attempt){
        return new RecentAttemptDTO(
                attempt.getId(),
                attempt.getQuizSubmission().getTitle(),
                attempt.getEndTime(),
                attempt.getScore(),
                attempt.getCorrectAnswers(),
                attempt.getTotalQuestions()
        );
    }
}