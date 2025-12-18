package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.QuizDifficultyStats;
import com.example.online_quiz_system.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizDifficultyService {

    private final QuizAttemptRepository quizAttemptRepository;

    public QuizDifficultyStats getDifficultyStats(Long quizId) {
        Double avg = quizAttemptRepository.findAverageScoreByQuizId(quizId);
        long count = quizAttemptRepository.countByQuizSubmission_Id(quizId);

        if (avg == null) {
            avg = 5.0; // mặc định trung bình nếu chưa có dữ liệu
        }

        return new QuizDifficultyStats(avg, count);
    }
}
