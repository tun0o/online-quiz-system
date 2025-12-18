package com.example.online_quiz_system.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyModerationStat {
    private LocalDate date;
    private Long approvedCount;
    private Long rejectedCount;
}
