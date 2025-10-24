package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.UserDashboardStatsDTO;
import com.example.online_quiz_system.dto.UserProfileUpdateDTO;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me/dashboard-stats")
    public ResponseEntity<UserDashboardStatsDTO> getMyDashboardStats(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        UserDashboardStatsDTO statsDTO = userService.getDashboardStatsForUser(userPrincipal.getId());
        return ResponseEntity.ok(statsDTO);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                  @Valid @RequestBody UserProfileUpdateDTO updateDTO){
        User updateUser = userService.updateUserProfile(userPrincipal.getId(), updateDTO);
        return ResponseEntity.ok(updateUser);
    }
}