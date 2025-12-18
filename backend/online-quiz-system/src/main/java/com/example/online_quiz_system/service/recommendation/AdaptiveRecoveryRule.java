package com.example.online_quiz_system.service.recommendation;

import com.example.online_quiz_system.dto.QuizDifficultyStats;
import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.service.QuizDifficultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdaptiveRecoveryRule implements RecommendationRule {

    private final QuizDifficultyService quizDifficultyService;

    @Override
    public String getName() {
        return "AdaptiveRecoveryRule";
    }

    @Override
    public double calculateScore(User user, QuizSubmission quiz, UserStats stats) {
        Map<String, Double> averages = stats.getAverageScoreBySubject();
        if (averages == null || averages.isEmpty()) {
            return 0.5;
        }

        String subject = quiz.getSubject();
        if (subject == null) {
            return 0.5;
        }

        Double userAvg = averages.get(subject);
        if (userAvg == null) {
            return 0.5;
        }

        QuizDifficultyStats diffStats = quizDifficultyService.getDifficultyStats(quiz.getId());
        double quizAvg = diffStats.getAverageScore();

        // gap > 0: user mạnh hơn đề, gap < 0: đề khó hơn user
        double gap = userAvg - quizAvg;

        if (gap < -2.0) {
            // Đề khó hơn năng lực hiện tại > 2 điểm -> tránh
            return 0.2;
        }

        if (gap < 0.0) {
            // Đề hơi khó hơn (0 - 2 điểm) -> vẫn chấp nhận, có thử thách
            return 0.6;
        }

        if (gap <= 1.5) {
            // Đề ngang hoặc dễ hơn một chút so với năng lực hiện tại -> rất phù hợp
            return 0.9;
        }

        // Đề quá dễ so với năng lực hiện tại
        return 0.4;
    }
}
