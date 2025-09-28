package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class EssayQuestionToGradeDTO {
    private Long questionId;
    private Long userAnswerId;
    private String questionText;
    private String userAnswerText;
    private String essayGuidelines;
    private BigDecimal maxScore;
}
