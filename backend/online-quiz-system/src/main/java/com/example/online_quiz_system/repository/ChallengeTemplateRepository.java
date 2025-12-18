package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.ChallengeTemplate;
import com.example.online_quiz_system.enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeTemplateRepository extends JpaRepository<ChallengeTemplate, Long> {

    List<ChallengeTemplate> findByIsActiveTrueOrderByDifficultyLevelAsc();

    @Query("SELECT ct FROM ChallengeTemplate ct WHERE ct.isActive = true AND ct.difficultyLevel = :level")
    List<ChallengeTemplate> findActiveByDifficultyLevel(DifficultyLevel level);

    @Query("SELECT ct FROM ChallengeTemplate ct WHERE ct.isActive = true " +
            "AND ct.id NOT IN (SELECT dc.template.id FROM DailyChallenge dc WHERE dc.challengeDate = CURRENT_DATE)")
    List<ChallengeTemplate> findAvailableForToday();
}
