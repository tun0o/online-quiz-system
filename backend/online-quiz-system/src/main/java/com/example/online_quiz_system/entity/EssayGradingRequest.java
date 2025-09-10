package com.example.online_quiz_system.entity;

import com.example.online_quiz_system.enums.GradingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "essay_grading_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssayGradingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quiz_attempt_id", nullable = false)
    private Long quizAttemptId;

    @Enumerated(EnumType.STRING)
    private GradingStatus status = GradingStatus.PENDING;

    @CreationTimestamp
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "total_essay_questions")
    private Integer totalEssayQuestions = 0;

    @Column(name = "graded_questions")
    private Integer gradedQuestions = 0;
}
