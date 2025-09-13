package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.entity.SubmissionAnswerOption;
import lombok.Data;

@Data
public class QuestionResultDTO {
    private Long questionId;
    private String questionText;
    private UserAnswerRequestDTO userAnswer;
    private SubmissionAnswerOption correctAnswer;
    private Boolean isCorrect;
    private String explanation;
}
