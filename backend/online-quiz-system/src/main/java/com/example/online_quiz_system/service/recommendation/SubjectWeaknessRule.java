package com.example.online_quiz_system.service.recommendation;

import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SubjectWeaknessRule implements RecommendationRule {

    @Override
    public String getName() {
        return "SubjectWeaknessRule";
    }

    @Override
    public double calculateScore(User user, QuizSubmission quiz, UserStats stats) {
        Map<String, Double> averages = stats.getAverageScoreBySubject();
        String subject = quiz.getSubject();

        if (averages == null || averages.isEmpty()) {
            // Chưa có dữ liệu gì -> trung lập
            return 0.5;
        }

        Double avg = averages.get(subject);

        if (avg == null) {
            // Chưa từng làm môn này -> ưu tiên rất cao để khuyến khích khám phá
            return 1.0;
        }

        if (avg < 5.0) {
            // Rất yếu -> ưu tiên tối đa
            return 1.0;
        } else if (avg < 7.0) {
            // Trung bình/yếu -> ưu tiên khá cao
            return 0.8;
        } else {
            // Đã khá/giỏi -> ưu tiên rất thấp nhưng không phải 0, vẫn có thể xuất hiện
            return 0.1;
        }
    }
}
