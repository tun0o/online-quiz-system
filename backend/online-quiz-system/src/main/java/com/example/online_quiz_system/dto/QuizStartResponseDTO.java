package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizStartResponseDTO {
    private Long attemptId;
    private QuizForTakingDTO quizData;
}
