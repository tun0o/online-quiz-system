package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.JwtResponseDTO;
import com.example.online_quiz_system.dto.RecentAttemptDTO;
import com.example.online_quiz_system.dto.UserDashboardStatsDTO;
import com.example.online_quiz_system.dto.UserProfileUpdateDTO;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.MinioService;
import com.example.online_quiz_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MinioService minioService;

    @GetMapping("/me/dashboard-stats")
    public ResponseEntity<UserDashboardStatsDTO> getMyDashboardStats(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        UserDashboardStatsDTO statsDTO = userService.getDashboardStatsForUser(userPrincipal.getId());
        return ResponseEntity.ok(statsDTO);
    }

    @PutMapping("/me")
    public ResponseEntity<JwtResponseDTO.UserDTO> updateUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                    @Valid @RequestBody UserProfileUpdateDTO updateDTO){
        User updateUser = userService.updateUserProfile(userPrincipal.getId(), updateDTO);

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        JwtResponseDTO.UserDTO userDTO = new JwtResponseDTO.UserDTO(updateUser.getId(), updateUser.getEmail(),
                updateUser.getName(), updateUser.getProvider(), updateUser.getGrade(), updateUser.getGoal(), updateUser.getCreatedAt(),
                roles, updateUser.isVerified(), updateUser.getAvatarUrl());
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/me/history")
    public ResponseEntity<Page<RecentAttemptDTO>> getMyAttemptHistory( @PageableDefault(size = 10) Pageable pageable){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Page<RecentAttemptDTO> recentAttemptDTOS = userService.getAttemptsHistory(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(recentAttemptDTOS);
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file")MultipartFile file,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File không được để trống"));
        }
        try {
            String avatarUrl = minioService.uploadFile(file, "avatars");

            userService.updateAvatar(userPrincipal.getId(), avatarUrl);

            return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi tải ảnh lên: " + e.getMessage()));
        }
    }
}