package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.dto.CountByDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByEmail(String email);

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM-DD') as date, COUNT(*) as count " +
                   "FROM users " +
                   "WHERE created_at >= CURRENT_DATE - INTERVAL '6 days' " +
                   "GROUP BY TO_CHAR(created_at, 'YYYY-MM-DD') " +
                   "ORDER BY date ASC", nativeQuery = true)
    List<CountByDate> countNewUsersLast7Days();
}