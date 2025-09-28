package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.DailyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, Long> {

    List<DailyChallenge> findByChallengeDate(LocalDate date);

    @Query("SELECT dc FROM DailyChallenge dc " +
            "JOIN FETCH dc.template " +
            "WHERE dc.challengeDate = :date AND dc.isActive = true " +
            "ORDER BY dc.template.difficultyLevel ASC")
    List<DailyChallenge> findTodayChallengeWithTemplate(@Param("date") LocalDate date);

    boolean existsByTemplateIdAndChallengeDate(Long templateId, LocalDate challengeDate);
}
