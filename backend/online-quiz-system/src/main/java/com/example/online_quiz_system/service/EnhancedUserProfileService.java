package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.UserProfileUpdateRequest;
import com.example.online_quiz_system.dto.UnifiedProfileData;
import com.example.online_quiz_system.entity.UserProfile;
import com.example.online_quiz_system.repository.UserProfileRepository;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.repository.OAuth2AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Enhanced UserProfile Service với Real-time Sync
 * - Tự động sync dữ liệu từ User và OAuth2Account
 * - Đảm bảo tính nhất quán dữ liệu
 * - Cung cấp unified profile data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedUserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final OAuth2AccountRepository oauth2AccountRepository;
    private final UserProfileService userProfileService;

    /**
     * Lấy profile với real-time sync
     */
    @Transactional(readOnly = true)
    public Optional<UserProfile> getProfileWithSync(Long userId) {
        try {
            log.info("Getting profile with sync for user ID: {}", userId);
            
            // Lấy profile hiện tại
            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
            
            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                log.info("Found existing profile for user ID: {}", userId);
                return Optional.of(profile);
            } else {
                // Tạo profile mới nếu chưa có
                log.info("No profile found for user ID: {}, creating new one", userId);
                UserProfile newProfile = userProfileService.createProfileForUser(userId);
                return Optional.of(newProfile);
            }
        } catch (Exception e) {
            log.error("Failed to get profile with sync for user ID: {}", userId, e);
            throw new RuntimeException("Failed to get profile: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật profile với sync
     */
    @Transactional
    public UserProfile updateProfileWithSync(Long userId, UserProfileUpdateRequest request) {
        try {
            log.info("Updating profile with sync for user ID: {}", userId);
            
            // Sử dụng UserProfileService hiện có
            UserProfile updatedProfile = userProfileService.updateUserProfile(userId, request);
            
            log.info("Successfully updated profile for user ID: {}", userId);
            return updatedProfile;
            
        } catch (Exception e) {
            log.error("Failed to update profile with sync for user ID: {}", userId, e);
            throw new RuntimeException("Failed to update profile: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy unified profile data từ tất cả nguồn
     */
    @Transactional(readOnly = true)
    public UnifiedProfileData getUnifiedProfileData(Long userId) {
        try {
            log.info("Getting unified profile data for user ID: {}", userId);
            
            // Lấy dữ liệu từ User
            var userOpt = userRepository.findById(userId);
            var user = userOpt.orElse(null);
            
            // Lấy dữ liệu từ UserProfile
            var profileOpt = userProfileRepository.findByUserId(userId);
            var profile = profileOpt.orElse(null);
            
            // Lấy dữ liệu từ OAuth2Account
            var oauth2List = oauth2AccountRepository.findByUserId(userId);
            var oauth2 = oauth2List.isEmpty() ? null : oauth2List.get(0);
            
            // Tạo unified data với priority logic
            UnifiedProfileData unifiedData = new UnifiedProfileData();
            
            if (user != null) {
                unifiedData.setUserId(user.getId());
                unifiedData.setEmail(user.getEmail());
                unifiedData.setIsVerified(user.getIsVerified());
                unifiedData.setGrade(user.getGrade());
                unifiedData.setGoal(user.getGoal());
            }
            
            if (profile != null) {
                if (unifiedData.getFullName() == null) {
                    unifiedData.setFullName(profile.getFullName());
                }
                if (unifiedData.getEmail() == null) {
                    unifiedData.setEmail(profile.getEmail());
                }
                if (unifiedData.getAvatarUrl() == null) {
                    unifiedData.setAvatarUrl(profile.getAvatarUrl());
                }
                if (unifiedData.getBio() == null) {
                    unifiedData.setBio(profile.getBio());
                }
                if (unifiedData.getDateOfBirth() == null) {
                    unifiedData.setDateOfBirth(profile.getDateOfBirth());
                }
                if (unifiedData.getGender() == null) {
                    unifiedData.setGender(profile.getGender());
                }
                if (unifiedData.getProvince() == null) {
                    unifiedData.setProvince(profile.getProvince());
                }
                if (unifiedData.getSchool() == null) {
                    unifiedData.setSchool(profile.getSchool());
                }
                if (unifiedData.getEmergencyPhone() == null) {
                    unifiedData.setEmergencyPhone(profile.getEmergencyPhone());
                }
            }
            
            if (oauth2 != null) {
                if (unifiedData.getFullName() == null) {
                    unifiedData.setFullName(oauth2.getProviderName());
                }
                if (unifiedData.getEmail() == null) {
                    unifiedData.setEmail(oauth2.getProviderEmail());
                }
                if (unifiedData.getAvatarUrl() == null) {
                    unifiedData.setAvatarUrl(oauth2.getProviderPicture());
                }
            }
            
            log.info("Successfully created unified profile data for user ID: {}", userId);
            return unifiedData;
            
        } catch (Exception e) {
            log.error("Failed to get unified profile data for user ID: {}", userId, e);
            throw new RuntimeException("Failed to get unified profile data: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra consistency của dữ liệu
     */
    @Transactional(readOnly = true)
    public boolean checkDataConsistency(Long userId) {
        try {
            log.info("Checking data consistency for user ID: {}", userId);
            
            // Lấy dữ liệu từ tất cả nguồn
            var userOpt = userRepository.findById(userId);
            var profileOpt = userProfileRepository.findByUserId(userId);
            var oauth2List = oauth2AccountRepository.findByUserId(userId);
            
            // Kiểm tra consistency
            boolean isConsistent = true;
            
            if (userOpt.isPresent() && profileOpt.isPresent()) {
                var user = userOpt.get();
                var profile = profileOpt.get();
                
                // Kiểm tra email consistency
                if (!user.getEmail().equals(profile.getEmail())) {
                    log.warn("Email inconsistency detected for user ID: {} - User: {}, Profile: {}", 
                        userId, user.getEmail(), profile.getEmail());
                    isConsistent = false;
                }
            }
            
            log.info("Data consistency check completed for user ID: {} - Consistent: {}", userId, isConsistent);
            return isConsistent;
            
        } catch (Exception e) {
            log.error("Failed to check data consistency for user ID: {}", userId, e);
            return false;
        }
    }
}