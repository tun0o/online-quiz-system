package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.enums.ChallengeType;
import com.example.online_quiz_system.enums.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyChallengeDTO {
    private Long id;
    private String title;
    private String description;
    private ChallengeType challengeType;
    private DifficultyLevel difficultyLevel;
    private Integer targetValue;
    private Integer rewardPoints;
    private Integer currentProgress;
    private Boolean isCompleted;
    private Integer progressPercentage;
}
