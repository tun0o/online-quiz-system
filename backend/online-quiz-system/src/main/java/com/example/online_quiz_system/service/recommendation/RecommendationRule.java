package com.example.online_quiz_system.service.recommendation;

import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.User;

public interface RecommendationRule {

    String getName();

    /**
     * Tính điểm phù hợp trong khoảng 0.0 - 1.0 cho cặp (user, quiz)
     */
    double calculateScore(User user, QuizSubmission quiz, UserStats stats);
}
