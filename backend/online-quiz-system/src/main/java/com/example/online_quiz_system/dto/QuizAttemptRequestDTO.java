package com.example.online_quiz_system.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizAttemptRequestDTO {
    private List<UserAnswerRequestDTO> answers;
    private boolean requestEssayGrading;
}
