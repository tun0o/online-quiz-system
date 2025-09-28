package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GradingDetailDTO {
    private Long attemptId;
    private Long quizId;
    private Long userId;
    private List<EssayQuestionToGradeDTO> essayQuestions;
}
