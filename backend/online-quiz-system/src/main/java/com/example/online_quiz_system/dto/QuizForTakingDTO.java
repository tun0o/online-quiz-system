package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.enums.DifficultyLevel;
import lombok.Data;

import java.util.List;

@Data
public class QuizForTakingDTO {
    private Long id;
    private String title;
    private String description;
    private String subject;
    private DifficultyLevel difficultyLevel;
    private Integer durationMinutes;
    private List<QuestionForTakingDTO> questions;
}
