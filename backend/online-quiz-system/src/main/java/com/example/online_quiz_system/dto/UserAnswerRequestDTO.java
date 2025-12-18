package com.example.online_quiz_system.dto;

import lombok.Data;

@Data
public class UserAnswerRequestDTO {
    private Long questionId;
    private Long selectedOptionId;
    private String answerText;
}
