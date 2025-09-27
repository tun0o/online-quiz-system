package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.AuthProvider;
import com.example.online_quiz_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    boolean existsByEmail(String email);

    @Modifying
    @Query("DELETE FROM User u WHERE u.isVerified = false AND u.createdAt < :threshold")
    int deleteUnverifiedUsersCreatedBefore(LocalDateTime threshold);
}