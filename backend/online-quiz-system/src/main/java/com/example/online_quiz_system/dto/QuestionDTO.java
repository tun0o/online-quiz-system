package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QuestionDTO {
    @NotBlank(message = "Câu hỏi không được để trống")
    private String questionText;

    private QuestionType questionType = QuestionType.MULTIPLE_CHOICE;
    private String explanation;
    private BigDecimal maxScore = BigDecimal.valueOf(10.0);
    private String essayGuidelines;

    @Valid
    private List<AnswerOptionDTO> answerOptions;
}
