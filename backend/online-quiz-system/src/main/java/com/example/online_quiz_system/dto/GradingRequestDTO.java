package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GradingRequestDTO {
    private Long requestId;
    private Long attemptId;
    private String quizTitle;
    private Long userId;
    private LocalDateTime requestedAt;
    private Integer totalEssayQuestions;
}
