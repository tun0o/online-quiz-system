package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.UserChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserChallengeProgressRepository extends JpaRepository<UserChallengeProgress, Long> {

    Optional<UserChallengeProgress> findByUserIdAndDailyChallengeId(Long userId, Long dailyChallengeId);

    @Query("SELECT ucp FROM UserChallengeProgress ucp " +
            "JOIN FETCH ucp.dailyChallenge dc " +
            "JOIN FETCH dc.template " +
            "WHERE ucp.userId = :userId AND dc.challengeDate = :date")
    List<UserChallengeProgress> findUserProgressForDate(@Param("userId") Long userId,
                                                        @Param("date") LocalDate date);

    @Query("SELECT COUNT(ucp) FROM UserChallengeProgress ucp " +
            "JOIN ucp.dailyChallenge dc " +
            "WHERE ucp.userId = userId and ucp.isCompleted = true AND dc. challengeDate = :date")
    Long countCompletedChallengesForUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT SUM(ucp.pointsEarned) FROM UserChallengeProgress ucp " +
            "JOIN ucp.dailyChallenge dc " +
            "WHERE ucp.userId = :userId AND dc.challengeDate = :date")
    Integer sumPointsEarnedForUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
