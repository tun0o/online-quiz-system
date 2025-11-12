package com.example.online_quiz_system.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeratorDashboardStatsDto {
    
    private Long totalPendingSubmissions;
    private Long totalPendingGradings;
    private List<DailyActivityDto> moderationActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyActivityDto {
        private LocalDate date;
        private long approvedCount;
        private long rejectedCount;
    }
}
