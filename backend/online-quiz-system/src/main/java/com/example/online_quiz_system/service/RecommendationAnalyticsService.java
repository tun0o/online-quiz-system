package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.RecommendationAnalyticsDTO;
import com.example.online_quiz_system.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecommendationAnalyticsService {

    private final QuizAttemptRepository quizAttemptRepository;

    public RecommendationAnalyticsDTO getUserAnalytics(Long userId, LocalDate sinceDate) {
        long totalAttempts = quizAttemptRepository.countByUserId(userId);
        long completedAttempts = quizAttemptRepository.countByUserIdAndStatusIgnoreCase(userId, "COMPLETED");

        double completionRate = totalAttempts > 0
                ? (double) completedAttempts / (double) totalAttempts
                : 0.0;

        LocalDateTime sinceDateTime = sinceDate != null ? sinceDate.atStartOfDay() : null;

        long totalAttemptsSince = 0;
        long completedAttemptsSince = 0;
        double completionRateSince = 0.0;

        if (sinceDateTime != null) {
            totalAttemptsSince = quizAttemptRepository.countByUserIdAndCreatedAtAfter(userId, sinceDateTime);
            completedAttemptsSince = quizAttemptRepository
                    .countByUserIdAndStatusIgnoreCaseAndCreatedAtAfter(userId, "COMPLETED", sinceDateTime);

            completionRateSince = totalAttemptsSince > 0
                    ? (double) completedAttemptsSince / (double) totalAttemptsSince
                    : 0.0;
        }

        Double averageScore = quizAttemptRepository.findAverageScoreByUserId(userId);
        Double averageScoreSince = sinceDateTime != null
                ? quizAttemptRepository.findAverageScoreByUserIdSince(userId, sinceDateTime)
                : null;

        return RecommendationAnalyticsDTO.builder()
                .totalAttempts(totalAttempts)
                .completedAttempts(completedAttempts)
                .completionRate(completionRate)
                .totalAttemptsSince(totalAttemptsSince)
                .completedAttemptsSince(completedAttemptsSince)
                .completionRateSince(completionRateSince)
                .averageScore(averageScore)
                .averageScoreSince(averageScoreSince)
                .build();
    }
}
