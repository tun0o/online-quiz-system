package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.dto.CountByDate;
import com.example.online_quiz_system.enums.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long>, JpaSpecificationExecutor<QuizSubmission> {

    Page<QuizSubmission> findByStatus(SubmissionStatus status, Pageable pageable);

    Page<QuizSubmission> findByContributorId(Long contributorId, Pageable pageable);

    @Query("SELECT qs FROM QuizSubmission qs LEFT JOIN FETCH qs.questions WHERE qs.id = :id")
    QuizSubmission findByIdWithQuestions(@Param("id") Long id);

    long countByContributorIdAndStatus(Long contributorId, SubmissionStatus status);

    long countByContributorId(Long contributorId);

    long countByStatus(SubmissionStatus status);

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM-DD') as date, COUNT(*) as count " +
                   "FROM quiz_submissions " +
                   "WHERE created_at >= CURRENT_DATE - INTERVAL '6 days' " +
                   "GROUP BY TO_CHAR(created_at, 'YYYY-MM-DD') " +
                   "ORDER BY date ASC", nativeQuery = true)
    List<CountByDate> countNewSubmissionsLast7Days();
}
