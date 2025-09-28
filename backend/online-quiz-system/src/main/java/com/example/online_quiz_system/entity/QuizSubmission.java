package com.example.online_quiz_system.entity;

import com.example.online_quiz_system.enums.DifficultyLevel;
import com.example.online_quiz_system.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String subject;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;

    @Column(name = "contributor_id", nullable = false)
    private Long contributorId;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Column(name = "admin_feedback")
    private String adminFeedback;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SubmissionQuestion> questions = new ArrayList<>();
}
