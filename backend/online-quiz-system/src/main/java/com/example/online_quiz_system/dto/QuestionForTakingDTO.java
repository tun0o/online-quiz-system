package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.enums.QuestionType;
import lombok.Data;

import java.util.List;

@Data
public class QuestionForTakingDTO {
    private Long id;
    private String questionText;
    private QuestionType questionType;
    private List<AnswerOptionForTakingDTO> answerOptions;
}
