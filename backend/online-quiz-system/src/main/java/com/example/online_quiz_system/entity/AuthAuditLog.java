package com.example.online_quiz_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // nullable for anonymous events

    @Column(nullable = false, length = 50)
    private String action; // REGISTER, LOGIN_SUCCESS, LOGIN_FAIL, REFRESH_SUCCESS, REFRESH_FAIL, VERIFY_SUCCESS, VERIFY_FAIL, RESET_SUCCESS, RESET_FAIL

    @Column(length = 64)
    private String deviceFingerprint;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 200)
    private String userAgent;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 255)
    private String reason; // optional extra context

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}



