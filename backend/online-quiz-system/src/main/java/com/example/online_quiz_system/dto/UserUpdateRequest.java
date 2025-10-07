package com.example.online_quiz_system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @Email(message = "Email phải có định dạng hợp lệ")
    private String email;
    
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
    
    @Size(max = 50, message = "Lớp không được vượt quá 50 ký tự")
    private String grade;
    
    @Size(max = 255, message = "Mục tiêu không được vượt quá 255 ký tự")
    private String goal;
    
    private Boolean isVerified;
    
    // Getters and setters are handled by @Data
}
