package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    public List<UserAnswer> findByQuizAttemptId(Long attemptId);
}
