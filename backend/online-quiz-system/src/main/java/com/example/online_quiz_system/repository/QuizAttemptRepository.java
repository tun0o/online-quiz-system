package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
}
