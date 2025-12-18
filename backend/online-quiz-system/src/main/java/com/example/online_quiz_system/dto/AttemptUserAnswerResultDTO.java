package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptUserAnswerResultDTO {
    private Long questionId;
    private Long selectedOptionId;
    private Boolean isCorrect; // Chỉ dùng cho trắc nghiệm
    private String answerText; // Dùng cho tự luận
    private Boolean isGraded;
    private BigDecimal score;
    private String feedback; // Tên này khớp với frontend
}
