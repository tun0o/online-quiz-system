package com.example.online_quiz_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "password_hash")
    // Password hash can be null for OAuth2 users
    private String passwordHash;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role không được để trống")
    private Role role;

    @Pattern(regexp = "^(10|11|12)$", message = "Lớp học chỉ được là 10, 11, hoặc 12")
    private String grade;

    @Size(max = 500, message = "Mục tiêu không được vượt quá 500 ký tự")
    private String goal;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- mapping 1:N tới VerificationToken ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore // Ignore lazy collection to prevent LazyInitializationException during Redis serialization
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    // --- mapping 1:N tới OAuth2Account ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<OAuth2Account> oauth2Accounts = new ArrayList<>();

    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }

    public Boolean getIsVerified() {
        return this.isVerified;
    }

    // Add isVerified() method for compatibility
    public boolean isVerified() {
        return this.isVerified != null && this.isVerified;
    }
    
    // OAuth2 helper methods - Updated for OAuth2Account
    public boolean isOAuth2User() {
        return !oauth2Accounts.isEmpty();
    }
    
    public boolean hasProvider(String provider) {
        return oauth2Accounts.stream()
            .anyMatch(account -> provider.equalsIgnoreCase(account.getProvider()));
    }
    
    public OAuth2Account getOAuth2Account(String provider) {
        return oauth2Accounts.stream()
            .filter(account -> provider.equalsIgnoreCase(account.getProvider()))
            .findFirst()
            .orElse(null);
    }
    
    public OAuth2Account getPrimaryOAuth2Account() {
        return oauth2Accounts.stream()
            .filter(OAuth2Account::isPrimaryAccount)
            .findFirst()
            .orElse(oauth2Accounts.stream().findFirst().orElse(null));
    }
    
    public String getDisplayName() {
        OAuth2Account primaryAccount = getPrimaryOAuth2Account();
        if (primaryAccount != null) {
            return primaryAccount.getDisplayName();
        }
        return email; // fallback
    }
    
    public String getDisplayPicture() {
        OAuth2Account primaryAccount = getPrimaryOAuth2Account();
        if (primaryAccount != null) {
            return primaryAccount.getDisplayPicture();
        }
        return null;
    }
    
    public String getDisplayEmail() {
        OAuth2Account primaryAccount = getPrimaryOAuth2Account();
        if (primaryAccount != null && primaryAccount.getDisplayEmail() != null) {
            return primaryAccount.getDisplayEmail();
        }
        return email; // fallback to user email
    }
    
    public String getDisplayPhone() {
        OAuth2Account primaryAccount = getPrimaryOAuth2Account();
        if (primaryAccount != null) {
            return primaryAccount.getDisplayPhone();
        }
        return null;
    }
    
    public String getDisplayBirthday() {
        OAuth2Account primaryAccount = getPrimaryOAuth2Account();
        if (primaryAccount != null) {
            return primaryAccount.getDisplayBirthday();
        }
        return null;
    }
    
    public String getDisplayGender() {
        OAuth2Account primaryAccount = getPrimaryOAuth2Account();
        if (primaryAccount != null) {
            return primaryAccount.getDisplayGender();
        }
        return null;
    }
    
    public String getDisplayLocale() {
        OAuth2Account primaryAccount = getPrimaryOAuth2Account();
        if (primaryAccount != null) {
            return primaryAccount.getDisplayLocale();
        }
        return null;
    }
}