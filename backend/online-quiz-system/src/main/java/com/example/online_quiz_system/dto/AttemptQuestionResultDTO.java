package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptQuestionResultDTO {
    private Long id;
    private String questionText;
    private String explanation;
    private QuestionType questionType;
    private BigDecimal maxScore;
    private List<AnswerOptionDTO> options; // Dùng lại AnswerOptionDTO để có isCorrect
}

