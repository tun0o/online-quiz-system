package com.example.online_quiz_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @Email
    @NotBlank
    @Size(max = 254)
    @Column(nullable=false, unique=true, length = 254)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable=false, length = 255)
    private String passwordHash;

    @Builder.Default
    @Column(nullable=false)
    private boolean isVerified = false;

    @Size(max = 100)
    private String grade;
    @Size(max = 200)
    private String goal;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private AuthProvider provider; // LOCAL
    private String providerId; // id từ provider

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- mapping 1:N tới VerificationToken ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    @Size(max = 150)
    private String name;

    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }

    @Version
    private Long version;

    @PrePersist
    @PreUpdate
    void normalize() {
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
    }
}
