package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.*;
import com.example.online_quiz_system.entity.EssayGradingRequest;
import com.example.online_quiz_system.entity.QuizAttempt;
import com.example.online_quiz_system.entity.UserAnswer;
import com.example.online_quiz_system.enums.GradingStatus;
import com.example.online_quiz_system.repository.EssayGradingRequestRepository;
import com.example.online_quiz_system.repository.QuizAttemptRepository;
import com.example.online_quiz_system.repository.UserAnswerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GradingService {

    @Autowired
    private EssayGradingRequestRepository gradingRequestRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    public List<GradingRequestDTO> getPendingGradingRequests() {
        return gradingRequestRepository.findPendingGradingRequests();
    }

    public GradingDetailDTO getAttemptDetailsForGrading(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("QuizAttempt not found with id: " + attemptId));

        List<EssayQuestionToGradeDTO> essayQuestionToGrade = userAnswerRepository.findByQuizAttemptId(attemptId)
                .stream()
                .filter(ua -> "ESSAY".equals(ua.getQuestion().getQuestionType().name()))
                .map(ua -> new EssayQuestionToGradeDTO(
                        ua.getQuestion().getId(),
                        ua.getId(),
                        ua.getQuestion().getQuestionText(),
                        ua.getAnswerText(),
                        ua.getQuestion().getEssayGuidelines(),
                        ua.getQuestion().getMaxScore()
                ))
                .collect(Collectors.toList());

        return new GradingDetailDTO(
                attempt.getId(),
                attempt.getQuizSubmission().getId(),
                attempt.getUserId(),
                essayQuestionToGrade
        );
    }

    @Transactional
    public void submitGrades(GradeSubmissionDTO submission){
        QuizAttempt attempt = quizAttemptRepository.findById(submission.getAttemptId())
                .orElseThrow(() -> new EntityNotFoundException("QuizAttempt not found with id: " + submission.getAttemptId()));

        BigDecimal totalEssayScore = BigDecimal.ZERO;

        for(GradeDTO grade : submission.getGrades()){
            UserAnswer userAnswer = userAnswerRepository.findById(grade.getUserAnswerId())
                    .orElseThrow(() -> new EntityNotFoundException("UserAnswer not found with id: " + grade.getUserAnswerId()));

            BigDecimal maxScore = userAnswer.getQuestion().getMaxScore();
            if(grade.getScore().compareTo(BigDecimal.ZERO) < 0 || grade.getScore().compareTo(maxScore) > 0){
                throw new IllegalArgumentException("Score for question " + userAnswer.getQuestion().getId() + " must between 0 and " + maxScore);
            }

            userAnswer.setScore(grade.getScore());
            userAnswer.setAdminFeedback(grade.getFeedback());
            userAnswer.setIsCorrect(grade.getScore().compareTo(BigDecimal.ZERO) > 0);
            userAnswerRepository.save(userAnswer);

            totalEssayScore = totalEssayScore.add(grade.getScore());
        }

        BigDecimal currentScoreFromAutoGraded = attempt.getScore() != null ? attempt.getScore() : BigDecimal.ZERO;
        BigDecimal newTotalScore = currentScoreFromAutoGraded.add(totalEssayScore);
        attempt.setScore(newTotalScore);
        quizAttemptRepository.save(attempt);

        EssayGradingRequest request = gradingRequestRepository.findByQuizAttemptId(attempt.getId())
                .orElseThrow(() -> new EntityNotFoundException(("GradingRequest not found for attemptId: " + attempt.getId())));

        request.setStatus(GradingStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());
//        request.setAssignedTo();
        gradingRequestRepository.save(request);
    }
}
