package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.AdminDashboardStatsDTO;
import com.example.online_quiz_system.enums.SubmissionStatus;
import com.example.online_quiz_system.dto.CountByDate;
import com.example.online_quiz_system.dto.DailyModerationStatProjection;
import com.example.online_quiz_system.dto.ModeratorDashboardStatsDto;
import com.example.online_quiz_system.enums.GradingStatus;
import com.example.online_quiz_system.repository.EssayGradingRequestRepository;
import com.example.online_quiz_system.repository.QuizSubmissionRepository;
import com.example.online_quiz_system.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final EssayGradingRequestRepository essayGradingRequestRepository;

    public AdminService(UserRepository userRepository, QuizSubmissionRepository quizSubmissionRepository,
            EssayGradingRequestRepository essayGradingRequestRepository) {
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

        return new AdminDashboardStatsDTO(totalUsers, totalApprovedQuizzes, totalPendingSubmissions,
                totalRejectedSubmissions, totalPendingGradings, userRegistrations, quizSubmissions);
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
            chartData.add(
                    new AdminDashboardStatsDTO.ChartDataPoint(dateString, countsByDate.getOrDefault(dateString, 0L)));
        }
        return chartData;
    }

    public ModeratorDashboardStatsDto getModeratorDashboardStats() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 1. Lấy các số liệu tổng quan
        long totalPendingSubmissions = quizSubmissionRepository.countByStatus(SubmissionStatus.PENDING);
        long totalPendingGradings = essayGradingRequestRepository.countByStatus(GradingStatus.PENDING);

        // 2. Lấy dữ liệu hoạt động kiểm duyệt cho biểu đồ
        List<DailyModerationStatProjection> dailyStats = quizSubmissionRepository.getDailyModerationStats(sevenDaysAgo);
        Map<LocalDate, DailyModerationStatProjection> statsMap = dailyStats.stream()
                .collect(Collectors.toMap(DailyModerationStatProjection::getDate, stat -> stat));

        List<ModeratorDashboardStatsDto.DailyActivityDto> moderationActivity = IntStream.range(0, 7)
                .mapToObj(i -> LocalDate.now().minusDays(i))
                .map(date -> {
                    DailyModerationStatProjection stat = statsMap.get(date);
                    return ModeratorDashboardStatsDto.DailyActivityDto.builder()
                            .date(date)
                            .approvedCount(stat != null ? stat.getApprovedCount() : 0)
                            .rejectedCount(stat != null ? stat.getRejectedCount() : 0)
                            .build();
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());

        // 4. Xây dựng và trả về DTO cuối cùng
        return ModeratorDashboardStatsDto.builder()
                .totalPendingSubmissions(totalPendingSubmissions)
                .totalPendingGradings(totalPendingGradings)
                .moderationActivity(moderationActivity)
                .build();
    }
}