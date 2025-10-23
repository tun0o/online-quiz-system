package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.enums.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long>, JpaSpecificationExecutor<QuizSubmission> {

    Page<QuizSubmission> findByStatus(SubmissionStatus status, Pageable pageable);

    Page<QuizSubmission> findByContributorId(Long contributorId, Pageable pageable);

    @Query("SELECT qs FROM QuizSubmission qs LEFT JOIN FETCH qs.questions WHERE qs.id = :id")
    QuizSubmission findByIdWithQuestions(@Param("id") Long id);

    long countByContributorIdAndStatus(Long contributorId, SubmissionStatus status);

    long countByContributorId(Long contributorId);
}
