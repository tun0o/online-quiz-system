package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.QuizAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    long countByUserIdAndStatus(Long userId, String status);
    List<QuizAttempt> findByUserIdAndEndTimeIsNotNullOrderByEndTimeDesc(Long userId, Pageable pageable);
}
