package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.UserDashboardStatsDTO;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> userDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

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

    @GetMapping("/quizzes")
    public ResponseEntity<?> getUserQuizzes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Triển khai logic lấy quiz của user
        return ResponseEntity.ok(Map.of(
                "message", "Danh sách quiz của bạn",
                "userId", userPrincipal.getId(),
                "quizzes", List.of() // Thay bằng service call thực tế
        ));
    }

    @GetMapping("/progress")
    public ResponseEntity<?> getUserProgress() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Triển khai logic lấy tiến độ học tập
        return ResponseEntity.ok(Map.of(
                "userId", userPrincipal.getId(),
                "completedQuizzes", 5,
                "averageScore", 85,
                "lastActivity", "2023-10-15"
        ));
    }

    @GetMapping("/me/dashboard-stats")
    public ResponseEntity<UserDashboardStatsDTO> getMyDashboardStats(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        UserDashboardStatsDTO statsDTO = userService.getDashboardStatsForUser(userPrincipal.getId());
        return ResponseEntity.ok(statsDTO);
    }
}