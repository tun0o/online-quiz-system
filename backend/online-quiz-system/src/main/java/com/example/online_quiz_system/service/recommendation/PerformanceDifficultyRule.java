package com.example.online_quiz_system.service.recommendation;

import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.enums.DifficultyLevel;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PerformanceDifficultyRule implements RecommendationRule {

    @Override
    public String getName() {
        return "PerformanceDifficultyRule";
    }

    @Override
    public double calculateScore(User user, QuizSubmission quiz, UserStats stats) {
        Map<String, Double> averages = stats.getAverageScoreBySubject();
        String subject = quiz.getSubject();
        DifficultyLevel difficulty = quiz.getDifficultyLevel();

        if (difficulty == null) {
            return 0.5;
        }

        Double avg = averages != null ? averages.get(subject) : null;

        if (avg == null) {
            // Chưa có dữ liệu -> ưu tiên MEDIUM một chút
            return switch (difficulty) {
                case MEDIUM -> 0.7;
                case EASY, HARD -> 0.5;
            };
        }

        // Buffer zone tránh nhảy độ khó quá gắt
        if (avg < 4.0) {
            // Rất yếu -> chỉ nên ưu tiên EASY
            return switch (difficulty) {
                case EASY -> 0.95;
                case MEDIUM -> 0.4;
                case HARD -> 0.1;
            };
        } else if (avg < 6.0) {
            // Yếu -> chủ yếu EASY, một ít MEDIUM
            return switch (difficulty) {
                case EASY -> 0.9;
                case MEDIUM -> 0.6;
                case HARD -> 0.1;
            };
        } else if (avg < 8.0) {
            // Trung bình khá -> MEDIUM là tối ưu
            return switch (difficulty) {
                case MEDIUM -> 0.9;
                case EASY -> 0.6;
                case HARD -> 0.4;
            };
        } else {
            // Giỏi -> nên thử thách với HARD
            return switch (difficulty) {
                case HARD -> 0.9;
                case MEDIUM -> 0.7;
                case EASY -> 0.3;
            };
        }
    }
}
