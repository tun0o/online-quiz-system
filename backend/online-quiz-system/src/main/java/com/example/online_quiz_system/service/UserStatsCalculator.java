package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizAttempt;
import com.example.online_quiz_system.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatsCalculator {

    private final QuizAttemptRepository quizAttemptRepository;

    public UserStats calculateStats(Long userId) {
        // Lấy tối đa 100 bài làm gần nhất đã hoàn thành
        Page<QuizAttempt> page = quizAttemptRepository
                .findByUserIdAndEndTimeIsNotNullOrderByEndTimeDesc(userId, PageRequest.of(0, 100));

        List<QuizAttempt> attempts = page.getContent();

        Map<String, Double> avgBySubject = attempts.stream()
                .filter(a -> a.getScore() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getQuizSubmission().getSubject(),
                        Collectors.averagingDouble(a -> a.getScore().doubleValue())
                ));

        Map<String, Long> countBySubject = attempts.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getQuizSubmission().getSubject(),
                        Collectors.counting()
                ));

        long totalAttempts = attempts.size();
        long incompleteAttempts = attempts.stream()
                .filter(a -> a.getEndTime() == null || a.getStatus() == null || !"COMPLETED".equalsIgnoreCase(a.getStatus()))
                .count();

        double incompleteRate = totalAttempts > 0
                ? (double) incompleteAttempts / (double) totalAttempts
                : 0.0;

        return UserStats.builder()
                .averageScoreBySubject(avgBySubject)
                .attemptCountBySubject(countBySubject)
                .recentAttempts(attempts)
                .incompleteRate(incompleteRate)
                .build();
    }
}
