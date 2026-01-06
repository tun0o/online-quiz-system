package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.UserRanking;
import com.example.online_quiz_system.enums.NotificationType;
import com.example.online_quiz_system.repository.UserRankingRepository;
import com.example.online_quiz_system.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class RankingRewardService {

    private static final Logger logger = LoggerFactory.getLogger(RankingRewardService.class);

    // Định nghĩa các mức điểm thưởng (Top 1, 2, 3)
    private static final List<Integer> DAILY_REWARDS = Arrays.asList(50, 30, 15);
    private static final List<Integer> WEEKLY_REWARDS = Arrays.asList(250, 150, 75);
    private static final List<Integer> MONTHLY_REWARDS = Arrays.asList(1000, 500, 250);

    @Autowired
    private UserRankingRepository userRankingRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Chạy vào 23:59:00 mỗi ngày để trao thưởng BXH ngày.
     */
    @Scheduled(cron = "0 59 23 * * *")
    @Transactional
    public void distributeDailyRewards() {
        logger.info("Bắt đầu trao thưởng bảng xếp hạng NGÀY...");
        List<UserRanking> topUsers = userRankingRepository.findTop10ByOrderByDailyPointsDescLastActivityDateAsc().stream().limit(3).toList();
        distributeRewards(topUsers, "ngày", DAILY_REWARDS);
        logger.info("Hoàn thành trao thưởng bảng xếp hạng NGÀY.");
    }

    /**
     * Chạy vào 23:59:00 mỗi Chủ Nhật để trao thưởng BXH tuần.
     */
    @Scheduled(cron = "0 59 23 * * SUN")
    @Transactional
    public void distributeWeeklyRewards() {
        logger.info("Bắt đầu trao thưởng bảng xếp hạng TUẦN...");
        List<UserRanking> topUsers = userRankingRepository.findTop10ByOrderByWeeklyPointsDescLastActivityDateAsc().stream().limit(3).toList();
        distributeRewards(topUsers, "tuần", WEEKLY_REWARDS);
        logger.info("Hoàn thành trao thưởng bảng xếp hạng TUẦN.");
    }

    /**
     * Chạy vào 23:59:00 ngày cuối cùng của tháng để trao thưởng BXH tháng.
     */
    @Scheduled(cron = "0 59 23 L * *")
    @Transactional
    public void distributeMonthlyRewards() {
        logger.info("Bắt đầu trao thưởng bảng xếp hạng THÁNG...");
        List<UserRanking> topUsers = userRankingRepository.findTop10ByOrderByMonthlyPointsDescLastActivityDateAsc().stream().limit(3).toList();
        distributeRewards(topUsers, "tháng", MONTHLY_REWARDS);
        logger.info("Hoàn thành trao thưởng bảng xếp hạng THÁNG.");
    }

    /**
     * Logic chung để trao thưởng và gửi thông báo.
     * @param topUsers Danh sách người dùng trong top.
     * @param periodName Tên chu kỳ ("ngày", "tuần", "tháng").
     * @param rewards Danh sách điểm thưởng.
     */
    private void distributeRewards(List<UserRanking> topUsers, String periodName, List<Integer> rewards) {
        for (int i = 0; i < topUsers.size(); i++) {
            if (i >= rewards.size()) break;

            UserRanking ranking = topUsers.get(i);
            int rank = i + 1;
            int rewardPoints = rewards.get(i);

            // Chỉ trao thưởng nếu người dùng có điểm > 0
            if (ranking.getDailyPoints() > 0 || ranking.getWeeklyPoints() > 0 || ranking.getMonthlyPoints() > 0) {
                // Cộng điểm thưởng vào điểm tiêu dùng
                ranking.setConsumptionPoints(ranking.getConsumptionPoints() + rewardPoints);
                userRankingRepository.save(ranking);

                User user = userRepository.findById(ranking.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + ranking.getUserId()));

                // Tạo thông báo cho người dùng
                String message = String.format(
                        "Chúc mừng! Bạn đã đạt hạng %d bảng xếp hạng %s và nhận được %d điểm thưởng.",
                        rank, periodName, rewardPoints
                );
                notificationService.createNotification(user, message, "/user/profile", NotificationType.RANKING_REWARD);

                logger.info("Đã trao {} điểm cho User ID {} (Hạng {}) cho BXH {}.",
                        rewardPoints, ranking.getUserId(), rank, periodName);
            }
        }
    }
}