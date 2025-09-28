package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.DailyPointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyPointHistoryRepository extends JpaRepository<DailyPointHistory, Long> {

    List<DailyPointHistory> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);

    @Query("SELECT SUM(dph.pointsEarned) FROM DailyPointHistory dph " +
            "WHERE dph.userId = :userId AND dph.activityDate BETWEEN :startDate AND :endDate")
    Integer sumPointsForUserBetweenDates(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT dph FROM DailyPointHistory dph " +
            "WHERE dph.userId = :userId ORDER BY dph.activityDate DESC")
    List<DailyPointHistory> findByUserIdOrderByDateDesc(@Param("userId") Long userId);
}
