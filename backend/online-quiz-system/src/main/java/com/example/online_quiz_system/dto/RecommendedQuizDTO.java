package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.enums.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedQuizDTO {

    private Long id;
    private String title;
    private String description;
    private String subject;
    private DifficultyLevel difficultyLevel;
    private Integer durationMinutes;

    public static RecommendedQuizDTO fromEntity(QuizSubmission quiz) {
        return RecommendedQuizDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .subject(quiz.getSubject())
                .difficultyLevel(quiz.getDifficultyLevel())
                .durationMinutes(quiz.getDurationMinutes())
                .build();
    }
}
