package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizDifficultyStats {
    private Double averageScore;
    private long attemptCount;
}
