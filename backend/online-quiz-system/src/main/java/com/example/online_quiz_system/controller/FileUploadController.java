package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.service.FileUploadService;
import com.example.online_quiz_system.service.UserProfileService;
import com.example.online_quiz_system.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * File Upload Controller
 * Xử lý upload file lên MinIO Object Storage
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final UserProfileService userProfileService;

    /**
     * Upload avatar image
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            log.info("Uploading avatar for user: {}", userId);
            
            // Upload file to MinIO
            String fileUrl = fileUploadService.uploadAvatar(file, userId);
            
            // Update user profile with new avatar URL
            userProfileService.updateUserProfile(userId, Map.of("avatarUrl", fileUrl));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avatar uploaded successfully");
            response.put("fileUrl", fileUrl);
            
            log.info("Avatar uploaded successfully for user {}: {}", userId, fileUrl);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file upload request: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Failed to upload avatar", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Delete avatar
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<?> deleteAvatar() {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            log.info("Deleting avatar for user: {}", userId);
            
            // Get current profile to find avatar URL
            var profileOpt = userProfileService.getUserProfile(userId);
            if (profileOpt.isPresent()) {
                var profile = profileOpt.get();
                if (profile.getAvatarUrl() != null) {
                    // Delete from MinIO
                    fileUploadService.deleteAvatar(profile.getAvatarUrl());
                    
                    // Update profile to remove avatar URL
                    userProfileService.updateUserProfile(userId, Map.of("avatarUrl", null));
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avatar deleted successfully");
            
            log.info("Avatar deleted successfully for user: {}", userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to delete avatar", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to delete avatar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
