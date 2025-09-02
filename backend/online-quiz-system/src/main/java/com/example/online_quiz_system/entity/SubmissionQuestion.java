package com.example.online_quiz_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "question_type")
    private String questionType = "MULTIPLE_CHOICE";

    @Column(name = "correct_answer")
    private String correctAnswer;

    private String explanation;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel = 1;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SubmissionAnswerOption> answerOptions = new ArrayList<>();
}
