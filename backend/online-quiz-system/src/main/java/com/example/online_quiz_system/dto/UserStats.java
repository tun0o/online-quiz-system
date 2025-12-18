package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.entity.QuizAttempt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class UserStats {

    /**
     * Điểm trung bình theo môn, ví dụ: "TOAN" -> 6.8
     */
    private Map<String, Double> averageScoreBySubject;

    /**
     * Số lần làm bài theo môn, ví dụ: "TOAN" -> 12
     */
    private Map<String, Long> attemptCountBySubject;

    /**
     * Các lần làm bài gần đây nhất (ví dụ top 20), dùng cho các rule nâng cao
     */
    private List<QuizAttempt> recentAttempts;

    /**
     * Tỉ lệ bài làm không hoàn thành (status != COMPLETED hoặc endTime null)
     */
    private double incompleteRate;
}
