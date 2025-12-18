package com.example.online_quiz_system.service.recommendation;

import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.enums.SubmissionStatus;
import com.example.online_quiz_system.repository.QuizSubmissionRepository;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.service.UserStatsCalculator;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimpleRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(SimpleRecommendationService.class);

    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserRepository userRepository;
    private final UserStatsCalculator userStatsCalculator;
    private final List<RecommendationRule> rules;

    public List<QuizSubmission> getPersonalizedQuizzes(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Lấy tất cả quiz đã duyệt (APPROVED)
        Page<QuizSubmission> page = quizSubmissionRepository
                .findByStatus(SubmissionStatus.APPROVED, Pageable.unpaged());
        List<QuizSubmission> quizzes = page.getContent();

        if (quizzes.isEmpty()) {
            return quizzes;
        }

        // Tính thống kê cho user (điểm trung bình theo môn, v.v.)
        UserStats stats = userStatsCalculator.calculateStats(userId);

        return quizzes.stream()
                .map(quiz -> new ScoredQuiz(quiz, calculateTotalScore(user, quiz, stats)))
                .sorted(Comparator.comparingDouble(ScoredQuiz::getScore).reversed())
                .limit(limit)
                .map(ScoredQuiz::getQuiz)
                .toList();
    }

    private double calculateTotalScore(User user, QuizSubmission quiz, UserStats stats) {
        double total = 0.0;

        for (RecommendationRule rule : rules) {
            double weight = getWeight(rule.getName());
            double score = rule.calculateScore(user, quiz, stats);
            total += weight * score;

            if (log.isDebugEnabled()) {
                log.debug("Rule {} for quiz {}: score={}, weight={}, contribution={}",
                        rule.getName(), quiz.getId(), score, weight, weight * score);
            }
        }

        return total;
    }

    private double getWeight(String ruleName) {
        return switch (ruleName) {
            case "SubjectWeaknessRule" -> 3.0;
            case "PerformanceDifficultyRule" -> 2.5;
            case "RecentFailureRecoveryRule" -> 2.0;
            case "AdaptiveRecoveryRule" -> 2.0;
            case "TimeManagementRule" -> 1.2;
            case "DiversificationRule" -> 1.5;
            case "QuizMasteryRule" -> 2.0;
            case "ExamPhaseRule" -> 1.8;
            default -> 1.0;
        };
    }

    @Data
    @AllArgsConstructor
    private static class ScoredQuiz {
        private QuizSubmission quiz;
        private double score;
    }
}
