package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContributionStatsDTO {
    private long submitted;
    private long approved;
    private long pending;
    private long rejected;
}
