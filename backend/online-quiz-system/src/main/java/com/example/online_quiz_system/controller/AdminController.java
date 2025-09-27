package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.online_quiz_system.repository.AuthAuditLogRepository;
import com.example.online_quiz_system.entity.AuthAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private final AuthAuditLogRepository auditRepo;

    @Autowired
    public AdminController(AuthAuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

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

    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuthAuditLog> result = auditRepo.findAll(pageable);
        return ResponseEntity.ok(Map.of(
                "items", result.getContent(),
                "total", result.getTotalElements(),
                "page", page,
                "size", size
        ));
    }
}