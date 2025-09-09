package com.example.online_quiz_system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class QuizSubmissionDTO {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    @NotBlank(message = "Môn học không được để trống")
    private String subject;

    @NotNull(message = "Thời gian làm bài không được để trống")
    @Positive(message = "Thời gian làm bài phải lớn hơn 0")
    private Integer durationMinutes;

    @Valid
    private List<QuestionDTO> questions;
}
