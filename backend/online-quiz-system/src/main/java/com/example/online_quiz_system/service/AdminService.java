package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.AdminDashboardStatsDTO;
import com.example.online_quiz_system.enums.SubmissionStatus;
import com.example.online_quiz_system.dto.CountByDate;
import com.example.online_quiz_system.enums.GradingStatus;
import com.example.online_quiz_system.repository.EssayGradingRequestRepository;
import com.example.online_quiz_system.repository.QuizSubmissionRepository;
import com.example.online_quiz_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final EssayGradingRequestRepository essayGradingRequestRepository;

    public AdminService(UserRepository userRepository, QuizSubmissionRepository quizSubmissionRepository, EssayGradingRequestRepository essayGradingRequestRepository) {
        this.userRepository = userRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.essayGradingRequestRepository = essayGradingRequestRepository;
    }

    public AdminDashboardStatsDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalApprovedQuizzes = quizSubmissionRepository.countByStatus(SubmissionStatus.APPROVED);
        long totalPendingSubmissions = quizSubmissionRepository.countByStatus(SubmissionStatus.PENDING);
        long totalRejectedSubmissions = quizSubmissionRepository.countByStatus(SubmissionStatus.REJECTED);
        long totalPendingGradings = essayGradingRequestRepository.countByStatus(GradingStatus.PENDING);

        // Lấy dữ liệu cho biểu đồ
        List<CountByDate> userCounts = userRepository.countNewUsersLast7Days();
        List<CountByDate> submissionCounts = quizSubmissionRepository.countNewSubmissionsLast7Days();

        // Chuyển đổi và điền dữ liệu còn thiếu cho 7 ngày
        List<AdminDashboardStatsDTO.ChartDataPoint> userRegistrations = formatChartData(userCounts);
        List<AdminDashboardStatsDTO.ChartDataPoint> quizSubmissions = formatChartData(submissionCounts);

        return new AdminDashboardStatsDTO(totalUsers, totalApprovedQuizzes, totalPendingSubmissions, totalRejectedSubmissions, totalPendingGradings, userRegistrations, quizSubmissions);
    }

    private List<AdminDashboardStatsDTO.ChartDataPoint> formatChartData(List<CountByDate> counts) {
        Map<String, Long> countsByDate = counts.stream()
                .collect(Collectors.toMap(CountByDate::getDate, CountByDate::getCount));

        List<AdminDashboardStatsDTO.ChartDataPoint> chartData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateString = date.format(formatter);
            chartData.add(new AdminDashboardStatsDTO.ChartDataPoint(dateString, countsByDate.getOrDefault(dateString, 0L)));
        }
        return chartData;
    }
}