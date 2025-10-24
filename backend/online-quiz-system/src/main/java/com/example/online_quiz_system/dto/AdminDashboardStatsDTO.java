package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDTO {
    private long totalUsers;
    private long totalApprovedQuizzes;
    private long totalPendingSubmissions;
    private long totalRejectedSubmissions;
    private long totalPendingGradings;
    private List<ChartDataPoint> userRegistrations;
    private List<ChartDataPoint> quizSubmissions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDataPoint {
        private String date;
        private long count;
    }
}