package com.example.online_quiz_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnswerOptionDTO {
    @NotBlank(message = "Đáp án không được để trống")
    private String optionText;

    private Boolean isCorrect = false;
}
