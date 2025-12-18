package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenHash(String tokenHash);

    List<VerificationToken> findByUser(User user);

    List<VerificationToken> findByUserAndUsedFalse(User user);

    void deleteByUser(User user);

    void deleteByUser_Id(Long userId);

    List<VerificationToken> findAllByExpiresAtBefore(LocalDateTime time);

    List<VerificationToken> findAllByUser_Id(Long userId);

    // Lấy token gần nhất theo createdAt cho 1 user
    Optional<VerificationToken> findTopByUserOrderByCreatedAtDesc(User user);
}
