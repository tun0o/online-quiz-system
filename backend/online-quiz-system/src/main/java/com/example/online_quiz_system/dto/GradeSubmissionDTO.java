package com.example.online_quiz_system.dto;

import lombok.Data;

import java.util.List;

@Data
public class GradeSubmissionDTO {
    private Long attemptId;
    private List<GradeDTO> grades;
}
