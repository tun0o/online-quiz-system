package com.example.online_quiz_system.dto;

import java.time.LocalDate;

public interface DailyModerationStatProjection {
    LocalDate getDate();
    Long getApprovedCount();
    Long getRejectedCount();
}