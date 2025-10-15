package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAdminDTO {
    private Long id;
    private String email;
    private String name;
    private String grade;
    private String goal;
    private Role role;
    private boolean verified;
    private boolean enabled;
    private LocalDateTime createdAt;
    private String provider;
}
