package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.PasswordResetToken;
import com.example.online_quiz_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    
    List<PasswordResetToken> findByUser(User user);
    
    void deleteByUser(User user);
    
    void deleteByUser_Id(Long userId);
    
    List<PasswordResetToken> findAllByExpiresAtBefore(LocalDateTime now);
    
    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(User user);
}

