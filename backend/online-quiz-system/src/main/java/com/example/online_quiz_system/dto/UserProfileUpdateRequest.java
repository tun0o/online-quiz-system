package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    // Validation fields theo yêu cầu - Optional for updates
    @Size(min = 2, max = 100, message = "Tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại Việt Nam không hợp lệ")
    private String emergencyPhone;

    @Pattern(regexp = "^https?://.*", message = "URL không hợp lệ")
    private String avatarUrl;

    @Size(max = 1000, message = "Bio không được vượt quá 1000 ký tự")
    private String bio;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    // Non-validation fields
    private Gender gender;
    private String province;
    private String school;
    private String grade;
    private String goal;
    
    // Thêm field emailVerified
    private Boolean emailVerified;
}