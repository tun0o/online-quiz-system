package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.UserProfileUpdateRequest;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.UserProfile;
import com.example.online_quiz_system.event.UserCreatedEvent;
import com.example.online_quiz_system.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * UserProfile Service - Quản lý thông tin profile của user
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Lấy UserProfile theo userId
     */
    public Optional<UserProfile> getUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }

    /**
     * Lấy UserProfile theo userId với caching
     */
    public Optional<UserProfile> getUserProfileWithCache(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }

    /**
     * Tạo UserProfile mới cho User nếu chưa tồn tại
     */
    @Transactional
    public UserProfile createProfileForUser(Long userId) {
        log.info("Creating new user profile for user ID: {}", userId);
        
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        UserProfile profile = new UserProfile(user);
        return userProfileRepository.save(profile);
    }

    /**
     * Cập nhật các trường của UserProfile từ request
     */
    private void updateProfileFields(UserProfile profile, UserProfileUpdateRequest request) {
        // Validation fields
        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        
        if (request.getProvince() != null) {
            profile.setProvince(request.getProvince());
        }
        
        if (request.getSchool() != null) {
            profile.setSchool(request.getSchool());
        }
        
        if (request.getGrade() != null) {
            profile.setGrade(request.getGrade());
        }
        
        if (request.getGoal() != null) {
            profile.setGoal(request.getGoal());
        }
        
        if (request.getEmergencyPhone() != null) {
            profile.setEmergencyPhone(request.getEmergencyPhone());
        }
        
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        
        if (request.getEmail() != null) {
            profile.setEmail(request.getEmail());
        }
        
        if (request.getEmailVerified() != null) {
            profile.setEmailVerified(request.getEmailVerified());
        }
    }

    /**
     * Cập nhật UserProfile
     */
    @Transactional
    public UserProfile updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        log.info("Updating user profile for user ID: {}", userId);
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found for user ID: " + userId));
        
        updateProfileFields(profile, request);
        
        return userProfileRepository.save(profile);
    }

    /**
     * Cập nhật UserProfile với Map (cho avatar upload)
     */
    @Transactional
    public UserProfile updateUserProfile(Long userId, java.util.Map<String, Object> updates) {
        log.info("Updating user profile for user ID: {} with map updates", userId);
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found for user ID: " + userId));
        
        // Update fields from map
        if (updates.containsKey("avatarUrl")) {
            profile.setAvatarUrl((String) updates.get("avatarUrl"));
        }
        if (updates.containsKey("fullName")) {
            profile.setFullName((String) updates.get("fullName"));
        }
        if (updates.containsKey("email")) {
            profile.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("bio")) {
            profile.setBio((String) updates.get("bio"));
        }
        // Add more fields as needed
        
        return userProfileRepository.save(profile);
    }

    /**
     * Lấy tất cả UserProfile
     */
    public List<UserProfile> getAllUserProfiles() {
        return userProfileRepository.findAll();
    }

    /**
     * Xóa UserProfile
     */
    @Transactional
    public void deleteUserProfile(Long userId) {
        log.info("Deleting user profile for user ID: {}", userId);
        userProfileRepository.deleteByUserId(userId);
    }

    /**
     * Kiểm tra UserProfile có tồn tại không
     */
    public boolean existsByUserId(Long userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    /**
     * Tạo UserProfile tự động khi User được tạo
     */
    @Transactional
    public void handleUserCreatedEvent(Long userId) {
        log.info("Handling UserCreatedEvent for user ID: {}", userId);
        
        // Kiểm tra xem UserProfile đã tồn tại chưa
        if (!userProfileRepository.existsByUserId(userId)) {
            try {
                createProfileForUser(userId);
                log.info("Created UserProfile for user ID: {}", userId);
            } catch (Exception e) {
                log.error("Failed to create UserProfile for user ID: {}", userId, e);
            }
        } else {
            log.info("UserProfile already exists for user ID: {}", userId);
        }
    }
}