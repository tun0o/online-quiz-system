package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * USER CONTROLLER - Chức năng của người dùng hiện tại
 * - Dashboard, quiz, tiến độ học tập
 * - Chỉ truy cập được với role USER
 * - Tự động lấy thông tin user từ SecurityContext
 */
@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class UserController {

    /**
     * User Dashboard - Trang chủ của user
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> userDashboard() {
        UserPrincipal userPrincipal = getCurrentUser();
        
        return ResponseEntity.ok(Map.of(
            "message", "Chào mừng " + userPrincipal.getEmail() + " đến trang người dùng",
            "features", List.of("Làm bài quiz", "Xem kết quả", "Theo dõi tiến độ học tập"),
            "userInfo", Map.of(
                "id", userPrincipal.getId(),
                "email", userPrincipal.getEmail(),
                "role", userPrincipal.getAuthorities()
            )
        ));
    }

    /**
     * Lấy danh sách quiz của user
     */
    @GetMapping("/quizzes")
    public ResponseEntity<?> getUserQuizzes() {
        UserPrincipal userPrincipal = getCurrentUser();
        
        // TODO: Implement quiz service
        return ResponseEntity.ok(Map.of(
            "message", "Quiz service chưa được implement",
            "userId", userPrincipal.getId(),
            "quizzes", List.of()
        ));
    }

    /**
     * Lấy tiến độ học tập của user
     */
    @GetMapping("/progress")
    public ResponseEntity<?> getUserProgress() {
        UserPrincipal userPrincipal = getCurrentUser();
        
        // TODO: Implement progress service
        return ResponseEntity.ok(Map.of(
            "message", "Progress service chưa được implement",
            "userId", userPrincipal.getId(),
            "progress", Map.of(
                "totalQuizzes", 0,
                "completedQuizzes", 0,
                "averageScore", 0.0
            )
        ));
    }

    /**
     * Lấy thông tin cơ bản của user
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo() {
        UserPrincipal userPrincipal = getCurrentUser();
        
        return ResponseEntity.ok(Map.of(
            "id", userPrincipal.getId(),
            "email", userPrincipal.getEmail(),
            "name", userPrincipal.getName(),
            "authorities", userPrincipal.getAuthorities()
        ));
    }

    /**
     * Helper method để lấy user hiện tại từ SecurityContext
     */
    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserPrincipal) authentication.getPrincipal();
    }
}