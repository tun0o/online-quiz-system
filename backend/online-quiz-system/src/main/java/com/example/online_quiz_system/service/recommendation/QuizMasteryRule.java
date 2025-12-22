package com.example.online_quiz_system.service.recommendation;

import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizAttempt;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuizMasteryRule implements RecommendationRule {

    @Override
    public String getName() {
        return "QuizMasteryRule";
    }

    @Override
    public double calculateScore(User user, QuizSubmission quiz, UserStats stats) {
        List<QuizAttempt> attempts = stats.getRecentAttempts();

        if (attempts == null || attempts.isEmpty()) {
            // Không có dữ liệu -> trung lập
            return 0.5;
        }

        Long quizId = quiz.getId();
        if (quizId == null) {
            return 0.5;
        }

        // Tìm điểm cao nhất mà user đã từng đạt cho chính quiz này
        Double bestScore = attempts.stream()
                .filter(a -> a.getQuizSubmission() != null
                        && a.getQuizSubmission().getId() != null
                        && a.getQuizSubmission().getId().equals(quizId)
                        && a.getScore() != null)
                .map(a -> a.getScore().doubleValue())
                .max(Double::compareTo)
                .orElse(null);

        if (bestScore == null) {
            // Chưa từng làm đề này -> trung lập, để các rule khác quyết định
            return 0.5;
        }

        if (bestScore < 5.0) {
            // Đã làm nhưng điểm thấp -> nên ưu tiên luyện lại
            return 1.0;
        } else if (bestScore < 8.0) {
            // Điểm trung bình -> ưu tiên vừa phải
            return 0.7;
        } else {
            // Đã làm rất tốt (>= 8) -> gần như không cần gợi ý lại
            return 0.1;
        }
    }
}
