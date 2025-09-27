package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<?> adminDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of(
                "message", "Chào mừng Admin " + userPrincipal.getEmail() + " đến trang quản trị",
                "features", List.of("Quản lý người dùng", "Duyệt bài quiz", "Xem báo cáo thống kê"),
                "userInfo", Map.of(
                        "id", userPrincipal.getId(),
                        "email", userPrincipal.getEmail(),
                        "role", userPrincipal.getAuthorities()
                )
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        // Triển khai logic lấy danh sách người dùng
        return ResponseEntity.ok(Map.of(
                "message", "Danh sách người dùng",
                "users", List.of() // Thay bằng service call thực tế
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        // Triển khai logic thống kê hệ thống
        return ResponseEntity.ok(Map.of(
                "totalUsers", 100,
                "totalQuizzes", 500,
                "pendingApprovals", 15
        ));
    }
}