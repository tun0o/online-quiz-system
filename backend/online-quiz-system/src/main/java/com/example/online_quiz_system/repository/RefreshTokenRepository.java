package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteByUser_Id(Long userId);

    void deleteByTokenHash(String tokenHash);

    java.util.List<RefreshToken> findAllByUser_Id(Long userId);
}


