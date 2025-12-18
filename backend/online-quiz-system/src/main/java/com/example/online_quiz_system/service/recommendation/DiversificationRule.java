package com.example.online_quiz_system.service.recommendation;

import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizAttempt;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DiversificationRule implements RecommendationRule {

    @Override
    public String getName() {
        return "DiversificationRule";
    }

    @Override
    public double calculateScore(User user, QuizSubmission quiz, UserStats stats) {
        List<QuizAttempt> recentAttempts = stats.getRecentAttempts();
        String subject = quiz.getSubject();

        if (recentAttempts == null || recentAttempts.isEmpty()) {
            return 0.5;
        }

        Map<String, Long> countBySubject = recentAttempts.stream()
                .collect(Collectors.groupingBy(a -> a.getQuizSubmission().getSubject(), Collectors.counting()));

        long total = recentAttempts.size();
        long subjectCount = countBySubject.getOrDefault(subject, 0L);

        double share = (double) subjectCount / (double) total;

        if (subjectCount == 0) {
            return 0.9;
        } else if (share < 0.3) {
            return 0.8;
        } else if (share < 0.6) {
            return 0.5;
        } else {
            return 0.2;
        }
    }
}
