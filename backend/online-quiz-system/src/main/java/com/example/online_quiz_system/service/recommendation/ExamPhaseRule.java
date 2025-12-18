package com.example.online_quiz_system.service.recommendation;

import com.example.online_quiz_system.dto.UserStats;
import com.example.online_quiz_system.entity.QuizAttempt;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Locale;

/**
 * Ưu tiên đề giữa học kì khi người dùng còn đang luyện giữa kì,
 * và dần chuyển sang đề cuối kì khi đã làm khá nhiều đề giữa kì.
 */
@Component
public class ExamPhaseRule implements RecommendationRule {

    private enum Phase {
        MIDTERM, // Giữa học kì I / II
        FINAL,   // Cuối học kì I / II
        OTHER
    }

    // Ngưỡng số lần làm đề giữa kì để coi là đã luyện đủ và nên chuyển sang cuối kì
    private static final int MIDTERM_ATTEMPT_THRESHOLD = 3;

    @Override
    public String getName() {
        return "ExamPhaseRule";
    }

    @Override
    public double calculateScore(User user, QuizSubmission quiz, UserStats stats) {
        String title = quiz.getTitle();
        if (title == null || stats.getRecentAttempts() == null) {
            return 0.5; // trung lập
        }

        Phase quizPhase = detectPhase(title);
        if (quizPhase == Phase.OTHER) {
            // Đề không rõ giữa / cuối kì -> không tác động nhiều
            return 0.5;
        }

        // Xác định kì học hiện tại từ thời gian hệ thống
        Semester currentSemester = detectCurrentSemester();
        if (currentSemester == Semester.NONE) {
            // Không nằm trong khoảng tháng học chính (ví dụ nghỉ hè) -> không can thiệp nhiều
            return 0.5;
        }

        List<QuizAttempt> attempts = stats.getRecentAttempts();
        String subject = quiz.getSubject();
        if (subject == null) {
            return 0.5;
        }

        String normalizedSubject = subject.toUpperCase(Locale.ROOT);

        int midtermAttemptsForSubject = 0;
        int finalAttemptsForSubject = 0;

        for (QuizAttempt attempt : attempts) {
            if (attempt.getQuizSubmission() == null
                    || attempt.getQuizSubmission().getSubject() == null
                    || attempt.getQuizSubmission().getTitle() == null) {
                continue;
            }

            String attSubject = attempt.getQuizSubmission().getSubject().toUpperCase(Locale.ROOT);
            if (!normalizedSubject.equals(attSubject)) {
                continue; // chỉ xét cùng môn
            }

            Phase attPhase = detectPhase(attempt.getQuizSubmission().getTitle());
            if (attPhase == Phase.MIDTERM) {
                midtermAttemptsForSubject++;
            } else if (attPhase == Phase.FINAL) {
                finalAttemptsForSubject++;
            }
        }

        // Logic cho điểm tổng hợp:
        // 1) Nếu quiz không thuộc kì học hiện tại -> ưu tiên rất thấp.
        // 2) Nếu thuộc kì hiện tại:
        //    - User chưa làm nhiều giữa kì -> ưu tiên đề giữa kì.
        //    - Khi đã làm khá nhiều giữa kì (>= threshold) -> ưu tiên đề cuối kì.

        boolean isCurrentSemesterMidterm =
                (currentSemester == Semester.HK1 && isMidtermPhase(title)) ||
                (currentSemester == Semester.HK2 && isMidtermPhase(title));

        boolean isCurrentSemesterFinal =
                (currentSemester == Semester.HK1 && isFinalPhase(title)) ||
                (currentSemester == Semester.HK2 && isFinalPhase(title));

        if (!isCurrentSemesterMidterm && !isCurrentSemesterFinal) {
            // Đề thuộc kì khác với kì hiện tại -> giảm mạnh ưu tiên
            return 0.15;
        }

        if (quizPhase == Phase.MIDTERM) {
            if (midtermAttemptsForSubject < MIDTERM_ATTEMPT_THRESHOLD) {
                // Vẫn đang giai đoạn luyện giữa kì -> ưu tiên cao cho đề giữa kì
                return 0.9;
            } else {
                // Đã làm khá nhiều giữa kì -> giảm ưu tiên để nhường chỗ cho cuối kì
                return 0.4;
            }
        } else if (quizPhase == Phase.FINAL) {
            if (midtermAttemptsForSubject >= MIDTERM_ATTEMPT_THRESHOLD) {
                // Đã luyện đủ giữa kì -> nên chuyển sang luyện cuối kì
                return 0.9;
            } else {
                // Chưa luyện nhiều giữa kì -> giữ ưu tiên vừa phải cho cuối kì
                return 0.5;
            }
        }

        return 0.5;
    }

    private Phase detectPhase(String rawTitle) {
        if (rawTitle == null) return Phase.OTHER;
        String title = rawTitle.toUpperCase(Locale.ROOT);

        boolean isMidterm = isMidtermPhase(title);

        boolean isFinal = isFinalPhase(title);

        if (isMidterm && !isFinal) {
            return Phase.MIDTERM;
        }
        if (isFinal && !isMidterm) {
            return Phase.FINAL;
        }
        return Phase.OTHER;
    }

    private boolean isMidtermPhase(String normalizedTitle) {
        String title = normalizedTitle.toUpperCase(Locale.ROOT);
        return title.contains("GIỮA HỌC KÌ I") || title.contains("GIỮA HỌC KÌ II")
                || title.contains("GIỮA HOC KI I") || title.contains("GIUA HOC KI I")
                || title.contains("GIUA HOC KI II");
    }

    private boolean isFinalPhase(String normalizedTitle) {
        String title = normalizedTitle.toUpperCase(Locale.ROOT);
        return title.contains("CUỐI HỌC KÌ I") || title.contains("CUỐI HỌC KÌ II")
                || title.contains("CUOI HOC KI I") || title.contains("CUOI HOC KI II");
    }

    private enum Semester {
        HK1, HK2, NONE
    }

    private Semester detectCurrentSemester() {
        Month month = LocalDate.now().getMonth();

        // Nới rộng dải tháng cho từng học kì:
        // - Học kì I: từ tháng 8–12
        // - Học kì II: từ tháng 1–6
        if (month.getValue() >= Month.AUGUST.getValue() && month.getValue() <= Month.DECEMBER.getValue()) {
            return Semester.HK1;
        }
        if (month.getValue() >= Month.JANUARY.getValue() && month.getValue() <= Month.JUNE.getValue()) {
            return Semester.HK2;
        }
        return Semester.NONE;
    }
}
