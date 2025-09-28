package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderBoardEntryDTO {
    private Integer rank;
    private Long userId;
    private String userName;
    private Integer totalPoints;
    private Integer weeklyPoints;
    private Integer currentStreak;
    private String medal;
}
