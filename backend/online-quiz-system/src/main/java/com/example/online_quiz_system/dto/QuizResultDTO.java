package com.example.online_quiz_system.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QuizResultDTO {
    private Long attemptId;
    private BigDecimal score;
    private int totalQuestions;
    private int correctAnswers;
    private List<QuestionResultDTO> results;
    private int pointsEarned; //cho xep hang
    private BigDecimal maxScore;
}
