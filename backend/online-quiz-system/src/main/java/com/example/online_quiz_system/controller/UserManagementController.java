package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.UserProfileUpdateRequest;
import com.example.online_quiz_system.dto.UserUpdateRequest;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.UserProfile;
import com.example.online_quiz_system.event.UserCreatedEvent;
import com.example.online_quiz_system.service.UserProfileService;
import com.example.online_quiz_system.service.UserProfileSyncService;
import com.example.online_quiz_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * USER MANAGEMENT CONTROLLER - Admin quản lý tất cả users
 * - Quản lý user cho admin
 * - Quản lý profile của bất kỳ user nào
 * - Sync và integrity check
 * - System management endpoints
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final UserProfileSyncService syncService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Lấy danh sách tất cả users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy thông tin user theo ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userService.getUserById(userId);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(userOptional.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cập nhật thông tin user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        try {
            User updatedUser = userService.updateUser(userId, request);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy UserProfile theo userId
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long userId) {
        try {
            Optional<UserProfile> profile = userProfileService.getUserProfile(userId);
            if (profile.isPresent()) {
                return ResponseEntity.ok(profile.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cập nhật UserProfile
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfile> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        try {
            UserProfile updatedProfile = userProfileService.updateUserProfile(userId, request);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Tạo UserProfile mới
     */
    @PostMapping("/{userId}/profile")
    public ResponseEntity<UserProfile> createUserProfile(@PathVariable Long userId) {
        try {
            UserProfile profile = userProfileService.createProfileForUser(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Xóa UserProfile
     */
    @DeleteMapping("/{userId}/profile")
    public ResponseEntity<String> deleteUserProfile(@PathVariable Long userId) {
        try {
            userProfileService.deleteUserProfile(userId);
            return ResponseEntity.ok("UserProfile deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete UserProfile: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra UserProfile có tồn tại không
     */
    @GetMapping("/{userId}/profile/exists")
    public ResponseEntity<Map<String, Boolean>> checkProfileExists(@PathVariable Long userId) {
        try {
            boolean exists = userProfileService.existsByUserId(userId);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy tất cả UserProfile
     */
    @GetMapping("/profiles")
    public ResponseEntity<List<UserProfile>> getAllUserProfiles() {
        try {
            List<UserProfile> profiles = userProfileService.getAllUserProfiles();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Sync UserProfile với User data
     */
    @PostMapping("/{userId}/sync")
    public ResponseEntity<String> syncUserProfile(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userService.getUserById(userId);
            if (userOptional.isPresent()) {
                syncService.syncUserProfile(userOptional.get());
                return ResponseEntity.ok("Sync completed for user: " + userId);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Sync failed: " + e.getMessage());
        }
    }

    /**
     * Force full sync
     */
    @PostMapping("/{userId}/force-sync")
    public ResponseEntity<String> forceFullSync(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userService.getUserById(userId);
            if (userOptional.isPresent()) {
                syncService.fullSyncUserProfile(userOptional.get());
                return ResponseEntity.ok("Force sync completed for user: " + userId);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Force sync failed: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra tính toàn vẹn dữ liệu
     */
    @GetMapping("/{userId}/integrity-check")
    public ResponseEntity<Map<String, Object>> checkDataIntegrity(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userService.getUserById(userId);
            if (userOptional.isPresent()) {
                Map<String, Object> result = syncService.checkDataIntegrity(userOptional.get());
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy thống kê sync
     */
    @GetMapping("/{userId}/sync-stats")
    public ResponseEntity<Map<String, Object>> getSyncStats(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userService.getUserById(userId);
            if (userOptional.isPresent()) {
                Map<String, Object> stats = syncService.getSyncStats(userOptional.get());
                return ResponseEntity.ok(stats);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Test UserCreatedEvent
     */
    @PostMapping("/{userId}/test-event")
    public ResponseEntity<String> testUserCreatedEvent(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userService.getUserById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                eventPublisher.publishEvent(new UserCreatedEvent(user.getId()));
                return ResponseEntity.ok("UserCreatedEvent published for user: " + userId + 
                                       " (email: " + user.getEmail() + ")");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to publish event: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê tổng quan
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        try {
            List<User> users = userService.findAll();
            List<UserProfile> profiles = userProfileService.getAllUserProfiles();
            
            Map<String, Object> stats = Map.of(
                "totalUsers", users.size(),
                "totalProfiles", profiles.size(),
                "usersWithoutProfiles", users.size() - profiles.size()
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Bulk sync tất cả users
     */
    @PostMapping("/bulk-sync")
    public ResponseEntity<String> bulkSyncAllUsers() {
        try {
            List<User> users = userService.findAll();
            int syncedCount = 0;
            
            for (User user : users) {
                try {
                    syncService.syncUserProfile(user);
                    syncedCount++;
                } catch (Exception e) {
                    // Log error but continue with other users
                    System.err.println("Failed to sync user " + user.getId() + ": " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok("Bulk sync completed. Synced " + syncedCount + " out of " + users.size() + " users.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Bulk sync failed: " + e.getMessage());
        }
    }
}