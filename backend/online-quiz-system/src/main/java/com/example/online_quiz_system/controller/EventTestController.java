package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.UserProfile;
import com.example.online_quiz_system.event.UserCreatedEvent;
import com.example.online_quiz_system.repository.UserProfileRepository;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.service.UserProfileSyncService;
import com.example.online_quiz_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * EVENT TEST CONTROLLER
 * - Test endpoints để kiểm tra event flow
 * - Chỉ dành cho testing và debugging
 */
@RestController
@RequestMapping("/api/test/event")
@RequiredArgsConstructor
public class EventTestController {

    private final UserService userService;
    private final UserProfileSyncService syncService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Test 1: Tạo user thông qua UserService và kiểm tra event flow
     */
    @PostMapping("/test-user-creation")
    public ResponseEntity<Map<String, Object>> testUserCreation() {
        try {
            String testEmail = "test-event-" + System.currentTimeMillis() + "@gmail.com";
            
            // Tạo user thông qua UserService (sẽ publish event)
            userService.registerUser(testEmail, "password123", "10", "Test event flow");
            
            // Tìm user vừa tạo
            User user = userRepository.findByEmail(testEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Đợi một chút để async processing hoàn thành
            Thread.sleep(2000);
            
            // Kiểm tra UserProfile
            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", user.getId());
            result.put("email", user.getEmail());
            result.put("profileExists", profileOpt.isPresent());
            
            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                result.put("profileUserId", profile.getUserId());
                result.put("profileEmail", profile.getEmail());
                result.put("profileGrade", profile.getGrade());
                result.put("profileGoal", profile.getGoal());
                result.put("profileFullName", profile.getFullName());
                result.put("profileEmailVerified", profile.getEmailVerified());
            }
            
            result.put("message", "User creation and event flow test completed");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Test 2: Publish event trực tiếp và kiểm tra listener
     */
    @PostMapping("/test-direct-event")
    public ResponseEntity<Map<String, Object>> testDirectEvent() {
        try {
            // Tạo user trực tiếp (không qua UserService)
            User user = User.builder()
                .email("direct-event-" + System.currentTimeMillis() + "@gmail.com")
                .passwordHash("hashed")
                .isVerified(false)
                .role(com.example.online_quiz_system.entity.Role.USER)
                .grade("11")
                .goal("Test direct event")
                .build();
            
            User savedUser = userRepository.save(user);
            
            // Publish event trực tiếp
            eventPublisher.publishEvent(new UserCreatedEvent(savedUser.getId()));
            
            // Đợi async processing
            Thread.sleep(2000);
            
            // Kiểm tra UserProfile
            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(savedUser.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", savedUser.getId());
            result.put("email", savedUser.getEmail());
            result.put("profileExists", profileOpt.isPresent());
            
            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                result.put("profileUserId", profile.getUserId());
                result.put("profileEmail", profile.getEmail());
                result.put("profileGrade", profile.getGrade());
                result.put("profileGoal", profile.getGoal());
            }
            
            result.put("message", "Direct event publishing test completed");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Test 3: Gọi sync service method trực tiếp
     */
    @PostMapping("/test-sync-method")
    public ResponseEntity<Map<String, Object>> testSyncMethod() {
        try {
            // Tạo user trực tiếp
            User user = User.builder()
                .email("sync-method-" + System.currentTimeMillis() + "@gmail.com")
                .passwordHash("hashed")
                .isVerified(true)
                .role(com.example.online_quiz_system.entity.Role.USER)
                .grade("12")
                .goal("Test sync method")
                .build();
            
            User savedUser = userRepository.save(user);
            
            // Gọi sync service method trực tiếp
            UserCreatedEvent event = new UserCreatedEvent(savedUser.getId());
            syncService.handleUserCreated(event);
            
            // Kiểm tra UserProfile
            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(savedUser.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", savedUser.getId());
            result.put("email", savedUser.getEmail());
            result.put("profileExists", profileOpt.isPresent());
            
            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                result.put("profileUserId", profile.getUserId());
                result.put("profileEmail", profile.getEmail());
                result.put("profileGrade", profile.getGrade());
                result.put("profileGoal", profile.getGoal());
                result.put("profileEmailVerified", profile.getEmailVerified());
            }
            
            result.put("message", "Sync method test completed");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Test 4: Kiểm tra consistency
     */
    @GetMapping("/consistency/{userId}")
    public ResponseEntity<Map<String, Object>> testConsistency(@PathVariable Long userId) {
        try {
            boolean isConsistent = syncService.isConsistent(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("isConsistent", isConsistent);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

