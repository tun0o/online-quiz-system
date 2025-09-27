package com.example.online_quiz_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable=false)
    private boolean isVerified = false;

    private String grade;
    private String goal;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String provider; // google, facebook
    private String providerId; // id từ provider

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- mapping 1:N tới VerificationToken ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    private String name;

    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }
}
