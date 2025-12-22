package com.example.online_quiz_system.service.recommendation;

import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TimeManagementRule implements RecommendationRule {

    @Override
    public String getName() {
        return "TimeManagementRule";
    }

    @Override
    public double calculateScore(User user, QuizSubmission quiz, UserStats stats) {
        double incompleteRate = stats.getIncompleteRate();
        Integer duration = quiz.getDurationMinutes();

        if (duration == null) {
            return 0.5;
        }

        // Nếu user hầu như luôn hoàn thành bài, rule gần như trung lập
        if (incompleteRate < 0.1) {
            return 0.5;
        }

        // User hay bỏ dở (>=10% attempt không hoàn thành)
        // Ưu tiên đề ngắn hơn để tăng khả năng hoàn thành
        if (duration <= 20) {
            return 0.9;
        } else if (duration <= 40) {
            return 0.7;
        } else {
            return 0.4;
        }
    }
}
