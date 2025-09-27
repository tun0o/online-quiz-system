package com.example.online_quiz_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Size(max = 512)
    @Column(name = "user_agent")
    private String userAgent;

    @Size(max = 100)
    @Column(name = "location")
    private String location;

    @Builder.Default
    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime = LocalDateTime.now();

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "flagged_reasons", length = 1000)
    // Lưu danh sách lý do dưới dạng CSV hoặc JSON
    private String flaggedReasons;

    @Version
    private Long version;
}
