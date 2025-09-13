package com.example.online_quiz_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_essay_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEssayAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "quiz_attempt_id")
    private Long quizAttemptId;

    @Column(name = "answer_text", nullable = false)
    private String answerText;

    private BigDecimal score;

    @Column(name = "max_score")
    private BigDecimal maxScore = BigDecimal.valueOf(10.0);

    @Column(name = "graded_by")
    private Long gradedBy;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "admin_feedback", columnDefinition = "TEXT")
    private String adminFeedback;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
