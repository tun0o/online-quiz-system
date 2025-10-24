package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateDTO {

    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 50, message = "Lớp học không được vượt quá 50 ký tự")
    private String grade;

    @Size(max = 100, message = "Mục tiêu không được vượt quá 100 ký tự")
    private String goal;
}
