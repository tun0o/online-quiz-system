package com.example.online_quiz_system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeDTO {
    private Long userAnswerId;
    private BigDecimal score;
    private String feedback;
}
