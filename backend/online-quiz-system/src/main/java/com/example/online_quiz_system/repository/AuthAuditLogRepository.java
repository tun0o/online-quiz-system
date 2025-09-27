package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.AuthAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, Long> {
}



