package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.AuthAuditLog;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.repository.AuthAuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuthAuditLogRepository repository;

    public AuditLogService(AuthAuditLogRepository repository) {
        this.repository = repository;
    }

    @Async
    public void log(User user, String action, boolean success, String reason, String ip, String deviceFingerprint, String userAgent) {
        AuthAuditLog log = AuthAuditLog.builder()
                .user(user)
                .action(action)
                .success(success)
                .reason(reason)
                .ipAddress(ip)
                .deviceFingerprint(deviceFingerprint)
                .userAgent(userAgent)
                .build();
        repository.save(log);
    }
}



