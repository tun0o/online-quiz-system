package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.QuizAttempt;
import com.example.online_quiz_system.dto.CountByDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    long countByUserIdAndStatus(Long userId, String status);
    List<QuizAttempt> findByUserIdAndEndTimeIsNotNullOrderByEndTimeDesc(Long userId, Pageable pageable);

    @Query(value = "SELECT TO_CHAR(end_time, 'YYYY-MM-DD') as date, COUNT(*) as count " +
                   "FROM quiz_attempts " +
                   "WHERE user_id = :userId AND end_time IS NOT NULL AND end_time >= CURRENT_DATE - INTERVAL '6 days' " +
                   "GROUP BY TO_CHAR(end_time, 'YYYY-MM-DD') " +
                   "ORDER BY date ASC", nativeQuery = true)
    List<CountByDate> countAttemptsByUserIdLast7Days(Long userId);
}
