package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.AdminDashboardStatsDTO;
import com.example.online_quiz_system.dto.UserAdminDTO;
import com.example.online_quiz_system.dto.UserCreateRequest;
import com.example.online_quiz_system.dto.UserUpdateRequest;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.AdminService;
import com.example.online_quiz_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats() {
        AdminDashboardStatsDTO stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserAdminDTO>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String enabled,
            @RequestParam(required = false) String verified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getAllUserForAdmin(keyword, role, enabled, verified, pageable));
    }

    @GetMapping("users/{id}")
    public ResponseEntity<UserAdminDTO> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserByIdForAdmin(id));
    }

    @PutMapping("users/{id}")
    public ResponseEntity<UserAdminDTO> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UserUpdateRequest updateRequest){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal adminPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateUserByAdmin(id, updateRequest, adminPrincipal));
    }

    @PostMapping("/users")
    public ResponseEntity<UserAdminDTO> createUser(@Valid @RequestBody UserCreateRequest createRequest) {
        UserAdminDTO newUser = userService.createUserByAdmin(createRequest);
        return ResponseEntity.status(201).body(newUser);
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