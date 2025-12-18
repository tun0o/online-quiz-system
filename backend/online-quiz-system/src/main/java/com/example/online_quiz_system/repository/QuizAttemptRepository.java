package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.QuizAttempt;
import com.example.online_quiz_system.dto.CountByDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    long countByUserIdAndStatus(Long userId, String status);
    Page<QuizAttempt> findByUserIdAndEndTimeIsNotNullOrderByEndTimeDesc(Long userId, Pageable pageable);

    @Query(value = "SELECT TO_CHAR(end_time, 'YYYY-MM-DD') as date, COUNT(*) as count " +
                   "FROM quiz_attempts " +
                   "WHERE user_id = :userId AND end_time IS NOT NULL AND end_time >= CURRENT_DATE - INTERVAL '6 days' " +
                   "GROUP BY TO_CHAR(end_time, 'YYYY-MM-DD') " +
                   "ORDER BY date ASC", nativeQuery = true)
    List<CountByDate> countAttemptsByUserIdLast7Days(Long userId);

    @Query("SELECT AVG(a.score) FROM QuizAttempt a WHERE a.quizSubmission.id = :quizId AND a.score IS NOT NULL")
    Double findAverageScoreByQuizId(Long quizId);

    long countByQuizSubmission_Id(Long quizId);

    long countByUserId(Long userId);

    long countByUserIdAndStatusIgnoreCase(Long userId, String status);

    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);

    long countByUserIdAndStatusIgnoreCaseAndCreatedAtAfter(Long userId, String status, LocalDateTime since);

    @Query("SELECT AVG(a.score) FROM QuizAttempt a WHERE a.userId = :userId AND a.score IS NOT NULL")
    Double findAverageScoreByUserId(Long userId);

    @Query("SELECT AVG(a.score) FROM QuizAttempt a WHERE a.userId = :userId AND a.score IS NOT NULL AND a.createdAt > :since")
    Double findAverageScoreByUserIdSince(Long userId, LocalDateTime since);
}
