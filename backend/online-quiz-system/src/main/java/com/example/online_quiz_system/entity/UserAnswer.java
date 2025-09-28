package com.example.online_quiz_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    private QuizAttempt quizAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SubmissionQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private SubmissionAnswerOption selectedOption;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "admin_feedback", columnDefinition = "TEXT")
    private String adminFeedback;
}
