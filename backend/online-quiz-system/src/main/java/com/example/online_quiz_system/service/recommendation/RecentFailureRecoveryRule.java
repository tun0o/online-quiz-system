package com.example.online_quiz_system.service.recommendation;

import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizAttempt;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class RecentFailureRecoveryRule implements RecommendationRule {

    @Override
    public String getName() {
        return "RecentFailureRecoveryRule";
    }

    @Override
    public double calculateScore(User user, QuizSubmission quiz, UserStats stats) {
        List<QuizAttempt> recentAttempts = stats.getRecentAttempts();
        if (recentAttempts == null || recentAttempts.isEmpty()) {
            return 0.5;
        }

        String subject = quiz.getSubject();
        if (subject == null) {
            return 0.5;
        }
        String normalizedSubject = subject.toUpperCase(Locale.ROOT);

        // Lọc các attempt cùng môn với quiz
        List<QuizAttempt> sameSubjectAttempts = recentAttempts.stream()
                .filter(a -> a.getQuizSubmission() != null
                        && a.getQuizSubmission().getSubject() != null
                        && normalizedSubject.equalsIgnoreCase(a.getQuizSubmission().getSubject()))
                .limit(3)
                .collect(Collectors.toList());

        if (sameSubjectAttempts.size() < 2) {
            // Chưa đủ dữ liệu gần đây cho môn này -> trung lập
            return 0.5;
        }

        long failCount = sameSubjectAttempts.stream()
                .filter(a -> a.getScore() != null && a.getScore().doubleValue() < 5.0)
                .count();

        if (failCount < 2) {
            // Không có chuỗi fail rõ ràng
            return 0.5;
        }

        // Có ít nhất 2 lần điểm < 5 trong ~3 attempt gần nhất của môn này
        // -> user đang gặp khó khăn với môn này, ưu tiên đề dễ hơn một chút
        // Ở Phase 2, ta chỉ boost nhẹ, không đụng vào độ khó thực tế phức tạp
        return 0.8;
    }
}
