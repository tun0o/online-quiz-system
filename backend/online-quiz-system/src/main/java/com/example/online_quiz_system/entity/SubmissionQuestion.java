package com.example.online_quiz_system.entity;

import com.example.online_quiz_system.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submission_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    @JsonIgnore
    private QuizSubmission submission;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "question_type")
    private QuestionType questionType;

    private String explanation;

    @Column(name = "max_score")
    private BigDecimal maxScore = BigDecimal.valueOf(10.0);

    @Column(name = "essay_guidelines", columnDefinition = "TEXT")
    private String essayGuidelines;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SubmissionAnswerOption> answerOptions = new ArrayList<>();
}
