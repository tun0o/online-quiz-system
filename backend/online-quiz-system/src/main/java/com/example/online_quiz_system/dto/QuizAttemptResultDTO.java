package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptResultDTO {
    private Long attemptId;
    private String quizTitle;
    private LocalDateTime completedAt;
    private BigDecimal score;
    private BigDecimal maxScore = BigDecimal.TEN;
    private int correctAnswers;
    private int totalQuestions;
    private int essayQuestionsCount;
    private int gradedEssayCount;
    private List<AttemptQuestionResultDTO> questions;
    private List<AttemptUserAnswerResultDTO> userAnswers;
}
