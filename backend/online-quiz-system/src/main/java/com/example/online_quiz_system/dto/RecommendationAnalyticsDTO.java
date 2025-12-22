package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RecommendationAnalyticsDTO {

    private long totalAttempts;
    private long completedAttempts;
    private double completionRate;

    private long totalAttemptsSince;
    private long completedAttemptsSince;
    private double completionRateSince;

    private Double averageScore;
    private Double averageScoreSince;
}
