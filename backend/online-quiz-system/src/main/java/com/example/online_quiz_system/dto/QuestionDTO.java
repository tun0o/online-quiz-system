package com.example.online_quiz_system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    @NotBlank(message = "Câu hỏi không được để trống")
    private String questionText;

    private String questionType = "MULTIPLE_CHOICE";
    private String explanation;
    private Integer difficultyLevel = 1;

    @Valid
    private List<AnswerOptionDTO> answerOptions;
}
