package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardStatsDTO {
    private Integer totalPoints;
    private Integer currentStreak;
    private Integer rank;
    private Long quizzesTaken;
    private Integer consumptionPoints;
    private ContributionStatsDTO contributions;
    private List<RecentAttemptDTO> recentAttempts;
    private List<ChartDataPoint> quizAttemptsOverTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDataPoint {
        private String date;
        private long count;
    }
}
