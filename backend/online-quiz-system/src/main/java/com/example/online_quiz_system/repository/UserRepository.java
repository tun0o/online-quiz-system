package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Note: Cache moved to Service level to avoid serialization conflicts
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    /**
     * Optimized query to fetch User with verification tokens
     * Note: UserProfile relationship is now managed via userId field
     */
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.verificationTokens " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithVerificationTokens(@Param("id") Long id);
}