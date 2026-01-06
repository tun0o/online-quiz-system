package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.UserRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRankingRepository extends JpaRepository<UserRanking, Long> {

    Optional<UserRanking> findByUserId(Long userId);

    List<UserRanking> findTop10ByOrderByTotalPointsDescLastActivityDateAsc();

    List<UserRanking> findTop10ByOrderByDailyPointsDescLastActivityDateAsc();

    Page<UserRanking> findAllByOrderByTotalPointsDesc(Pageable pageable);

    List<UserRanking> findTop10ByOrderByWeeklyPointsDescLastActivityDateAsc();

    List<UserRanking> findTop10ByOrderByMonthlyPointsDescLastActivityDateAsc();

    @Query("SELECT COUNT(ur) + 1 FROM UserRanking ur WHERE ur.totalPoints > :totalPoints " +
            "OR ( ur.totalPoints = :totalPoints AND ur.updatedAt < :updatedAt)")
    Integer findUserRankByTotalPoints(@Param("totalPoints") Integer totalPoints, @Param("updatedAt")LocalDateTime updatedAt);

    @Query("SELECT COUNT(ur) + 1 FROM UserRanking ur WHERE ur.dailyPoints > :dailyPoints " +
            "OR ( ur.dailyPoints = :dailyPoints AND ur.updatedAt < :updatedAt)")
    Integer findUserRankByDailyPoints(@Param("dailyPoints") Integer dailyPoints, @Param("updatedAt")LocalDateTime updatedAt);

    @Query("SELECT COUNT(ur) + 1 FROM UserRanking ur WHERE ur.weeklyPoints > :weeklyPoints " +
            "OR ( ur.weeklyPoints = :weeklyPoints AND ur.updatedAt < :updatedAt)")
    Integer findUserRankByWeeklyPoints(@Param("weeklyPoints") Integer weeklyPoints, @Param("updatedAt")LocalDateTime updatedAt);

    @Query("SELECT COUNT(ur) + 1 FROM UserRanking ur WHERE ur.monthlyPoints > :monthlyPoints " +
            "OR ( ur.monthlyPoints = :monthlyPoints AND ur.updatedAt < :updatedAt)")
    Integer findUserRankByMonthlyPoints(@Param("monthlyPoints") Integer monthlyPoints, @Param("updatedAt")LocalDateTime updatedAt);

    @Query("SELECT ur FROM UserRanking ur WHERE ur.totalPoints > 0 " +
            "ORDER BY ur.totalPoints DESC")
    List<UserRanking> findAllActiveUserOrderByPoints();

    @Modifying
    @Query("UPDATE UserRanking ur SET ur.dailyPoints = 0")
    int resetDailyPoints();

    @Modifying
    @Query("UPDATE UserRanking ur SET ur.weeklyPoints = 0")
    int resetWeeklyPoints();

    @Modifying
    @Query("UPDATE UserRanking ur SET ur.monthlyPoints = 0")
    int resetMonthlyPoints();
}
