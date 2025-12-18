package com.example.online_quiz_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectSubmissionDTO {
    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;
}
