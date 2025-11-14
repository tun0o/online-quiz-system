package com.example.online_quiz_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.online_quiz_system.repository.UserRankingRepository;

import jakarta.transaction.Transactional;

@Service
public class RankingResetService {

    private static final Logger logger = LoggerFactory.getLogger(RankingResetService.class);

    @Autowired
    private UserRankingRepository userRankingRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetDailyPoints() {
        logger.info("Starting daily points reset...");
        int updatedRows = userRankingRepository.resetDailyPoints();
        logger.info("Daily points reset completed. Total records updated: {}", updatedRows);
    }

    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void resetWeeklyPoints() {
        logger.info("Starting weekly points reset...");
        int updatedRows = userRankingRepository.resetWeeklyPoints();
        logger.info("Weekly points reset completed. Total records updated: {}", updatedRows);
    }

    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void resetMonthlyPoints() {
        logger.info("Starting monthly points reset...");
        int updatedRows = userRankingRepository.resetMonthlyPoints();
        logger.info("Monthly points reset completed. Total records updated: {}", updatedRows);
    }
}