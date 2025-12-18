package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentAttemptDTO {
    private Long id;
    private String quizTitle;
    private LocalDateTime completedAt;
    private BigDecimal score;
    private Integer correctAnswers;
    private Integer totalQuestions;
}
