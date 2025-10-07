package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.UserProfile;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    /**
     * Tìm UserProfile theo User ID (updated for new structure)
     * Note: Cache moved to Service level to avoid serialization conflicts
     */
    @Query("SELECT up FROM UserProfile up WHERE up.userId = :userId")
    Optional<UserProfile> findByUserId(@Param("userId") Long userId);
    
    /**
     * Kiểm tra UserProfile có tồn tại theo User ID (updated for new structure)
     */
    @Query("SELECT COUNT(up) > 0 FROM UserProfile up WHERE up.userId = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
    
    /**
     * Xóa UserProfile theo User ID
     */
    @Modifying
    @Query("DELETE FROM UserProfile up WHERE up.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    /**
     * Tìm UserProfile theo email
     */
    Optional<UserProfile> findByEmail(String email);
    
    /**
     * Tìm UserProfile theo grade
     */
    @Query("SELECT up FROM UserProfile up WHERE up.grade = :grade")
    Optional<UserProfile> findByGrade(@Param("grade") String grade);
    
    /**
     * FIXED: Get user IDs with profiles efficiently
     */
    @Query("SELECT up.userId FROM UserProfile up")
    List<Long> findUserIdsWithProfiles();
    
    /**
     * 🔥 UPSERT: Tối ưu tạo profile với Postgres ON CONFLICT DO UPDATE
     * Sử dụng native query để tận dụng database-level upsert
     */
    @Modifying
    @Query(value = """
        INSERT INTO user_profiles (user_id, email, email_verified, grade, goal, full_name, created_at, updated_at)
        VALUES (:userId, :email, :emailVerified, :grade, :goal, :fullName, NOW(), NOW())
        ON CONFLICT (user_id) 
        DO UPDATE SET 
            email = EXCLUDED.email,
            email_verified = EXCLUDED.email_verified,
            grade = EXCLUDED.grade,
            goal = EXCLUDED.goal,
            updated_at = NOW()
        """, nativeQuery = true)
    int upsertUserProfile(
        @Param("userId") Long userId,
        @Param("email") String email,
        @Param("emailVerified") Boolean emailVerified,
        @Param("grade") String grade,
        @Param("goal") String goal,
        @Param("fullName") String fullName
    );
}