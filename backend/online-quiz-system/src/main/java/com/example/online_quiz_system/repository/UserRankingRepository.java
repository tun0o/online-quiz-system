package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.UserRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRankingRepository extends JpaRepository<UserRanking, Long> {

    Optional<UserRanking> findByUserId(Long userId);

    List<UserRanking> findTop10ByOrderByTotalPointsDesc();

    Page<UserRanking> findAllByOrderByTotalPointsDesc(Pageable pageable);

    List<UserRanking> findTop10ByOrderByWeeklyPointsDesc();

    @Query("SELECT COUNT(ur) + 1 FROM UserRanking ur WHERE ur.totalPoints > " +
            "(SELECT ur2.totalPoints FROM UserRanking ur2 WHERE ur2.userId = :userId)")
    Integer findUserRankByUserId(@Param("userId") Long userId);

    @Query("SELECT ur FROM UserRanking ur WHERE ur.totalPoints > 0 " +
            "ORDER BY ur.totalPoints DESC")
    List<UserRanking> findAllActiveUserOrderByPoints();
}
