package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.AuditLog;
import com.example.online_quiz_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUser(User user);
}

