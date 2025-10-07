package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Unified Profile Data DTO
 * Chứa dữ liệu profile thống nhất từ tất cả nguồn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedProfileData {
    
    private Long userId;
    private String fullName;
    private String email;
    private Boolean isVerified;
    private String avatarUrl;
    private String bio;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String province;
    private String school;
    private String grade;
    private String goal;
    private String emergencyPhone;
    private String oauth2Provider;
    private String oauth2Name;
    private String oauth2Picture;
}