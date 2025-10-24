package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.dto.GradingRequestDTO;
import com.example.online_quiz_system.enums.GradingStatus;
import com.example.online_quiz_system.entity.EssayGradingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EssayGradingRequestRepository extends JpaRepository<EssayGradingRequest, Long> {

    Optional<EssayGradingRequest> findByQuizAttemptId(Long quizAttemptId);

    @Query("SELECT new com.example.online_quiz_system.dto.GradingRequestDTO(" +
            "egr.id, " +
            "qa.id, " +
            "qs.title, " +
            "qa.userId, " +
            "egr.requestedAt, " +
            "egr.totalEssayQuestions) " +
            "FROM EssayGradingRequest egr JOIN egr.quizAttempt qa JOIN qa.quizSubmission qs " +
            "WHERE egr.status = 'PENDING'")
    List<GradingRequestDTO> findPendingGradingRequests();

    boolean existsByQuizAttemptId(Long quizAttemptId);

    long countByStatus(GradingStatus status);
}
