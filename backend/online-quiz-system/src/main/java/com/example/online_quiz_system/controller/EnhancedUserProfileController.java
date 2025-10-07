package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.UserProfileUpdateRequest;
import com.example.online_quiz_system.dto.UnifiedProfileData;
import com.example.online_quiz_system.entity.UserProfile;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.EnhancedUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ENHANCED UserProfile Controller với Real-time Sync
 * - Tự động sync dữ liệu từ User và OAuth2Account
 * - Đảm bảo tính nhất quán dữ liệu
 * - Cung cấp unified profile data
 */
@RestController
@RequestMapping("/api/user/profile")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class EnhancedUserProfileController {

    private final EnhancedUserProfileService enhancedUserProfileService;

    /**
     * 🔥 ENHANCED GET PROFILE: Lấy profile với real-time sync
     */
    @GetMapping
    public ResponseEntity<?> getMyProfile() {
        UserPrincipal userPrincipal = getCurrentUser();
        
        try {
            Optional<UserProfile> profile = enhancedUserProfileService.getProfileWithSync(userPrincipal.getId());
            return profile.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get profile: " + e.getMessage()
            ));
        }
    }

    /**
     * 🔥 UNIFIED PROFILE DATA: Lấy dữ liệu unified từ tất cả nguồn
     */
    @GetMapping("/unified")
    public ResponseEntity<?> getUnifiedProfile() {
        UserPrincipal userPrincipal = getCurrentUser();
        
        try {
            UnifiedProfileData unifiedData = enhancedUserProfileService.getUnifiedProfileData(userPrincipal.getId());
            return ResponseEntity.ok(unifiedData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get unified profile: " + e.getMessage()
            ));
        }
    }

    /**
     * 🔥 ENHANCED UPDATE PROFILE: Cập nhật với sync
     */
    @PutMapping
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        
        try {
            UserProfile updatedProfile = enhancedUserProfileService.updateProfileWithSync(userPrincipal.getId(), request);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to update profile: " + e.getMessage()
            ));
        }
    }

    /**
     * 🔥 PROFILE COMPLETION: Lấy thông tin hoàn thiện profile
     */
    @GetMapping("/completion")
    public ResponseEntity<?> getProfileCompletion() {
        UserPrincipal userPrincipal = getCurrentUser();
        
        try {
            UnifiedProfileData unifiedData = enhancedUserProfileService.getUnifiedProfileData(userPrincipal.getId());
            
            Map<String, Object> completion = new HashMap<>();
            
            // Tính completion score
            int score = 0;
            if (unifiedData.getFullName() != null && !unifiedData.getFullName().isEmpty()) score += 20;
            if (unifiedData.getEmail() != null && !unifiedData.getEmail().isEmpty()) score += 20;
            if (unifiedData.getAvatarUrl() != null && !unifiedData.getAvatarUrl().isEmpty()) score += 20;
            if (unifiedData.getBio() != null && !unifiedData.getBio().isEmpty()) score += 10;
            if (unifiedData.getDateOfBirth() != null) score += 10;
            if (unifiedData.getGender() != null) score += 10;
            if (unifiedData.getProvince() != null && !unifiedData.getProvince().isEmpty()) score += 10;
            
            completion.put("score", score);
            completion.put("hasOAuth2Data", unifiedData.getOauth2Provider() != null);
            completion.put("bestDisplayName", unifiedData.getFullName() != null ? unifiedData.getFullName() : unifiedData.getOauth2Name());
            completion.put("bestAvatarUrl", unifiedData.getAvatarUrl() != null ? unifiedData.getAvatarUrl() : unifiedData.getOauth2Picture());
            completion.put("isComplete", score >= 80);
            completion.put("syncStatus", "synced");
            
            return ResponseEntity.ok(completion);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get completion info: " + e.getMessage()
            ));
        }
    }

    /**
     * 🔥 SYNC STATUS: Kiểm tra trạng thái sync
     */
    @GetMapping("/sync-status")
    public ResponseEntity<?> getSyncStatus() {
        UserPrincipal userPrincipal = getCurrentUser();
        
        try {
            // Kiểm tra consistency
            boolean isConsistent = enhancedUserProfileService.checkDataConsistency(userPrincipal.getId());
            
            Map<String, Object> status = new HashMap<>();
            status.put("userId", userPrincipal.getId());
            status.put("isConsistent", isConsistent);
            status.put("lastChecked", System.currentTimeMillis());
            status.put("message", isConsistent ? "Data is consistent" : "Data inconsistency detected");
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to check sync status: " + e.getMessage()
            ));
        }
    }

    /**
     * 🔥 FORCE SYNC: Buộc sync dữ liệu
     */
    @PostMapping("/force-sync")
    public ResponseEntity<?> forceSync() {
        UserPrincipal userPrincipal = getCurrentUser();
        
        try {
            // Lấy profile với sync
            Optional<UserProfile> profile = enhancedUserProfileService.getProfileWithSync(userPrincipal.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userPrincipal.getId());
            result.put("synced", profile.isPresent());
            result.put("timestamp", System.currentTimeMillis());
            result.put("message", profile.isPresent() ? "Profile synced successfully" : "Profile not found");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to force sync: " + e.getMessage()
            ));
        }
    }

    /**
     * Helper method để lấy user hiện tại từ SecurityContext
     */
    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserPrincipal) authentication.getPrincipal();
    }
}



