package com.example.online_quiz_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * OAuth2Account Entity - Quản lý OAuth2 authentication data
 * Hỗ trợ multiple providers per user (Google, Facebook, GitHub, etc.)
 */
@Entity
@Table(name = "oauth2_accounts", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "provider"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User không được để trống")
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    @NotBlank(message = "Provider không được để trống")
    private String provider; // "google", "facebook", "github", etc.

    @Column(name = "provider_id", nullable = false)
    @NotBlank(message = "Provider ID không được để trống")
    private String providerId; // ID từ OAuth2 provider

    @Column(name = "provider_name")
    private String providerName; // Tên từ OAuth2 provider

    @Column(name = "provider_picture")
    private String providerPicture; // Avatar từ OAuth2 provider

    @Column(name = "provider_email")
    private String providerEmail; // Email từ OAuth2 provider

    // Extended OAuth2 fields
    @Column(name = "provider_phone")
    private String providerPhone; // Phone từ OAuth2 provider

    @Column(name = "provider_birthday")
    private String providerBirthday; // Birthday từ OAuth2 provider

    @Column(name = "provider_gender")
    private String providerGender; // Gender từ OAuth2 provider

    @Column(name = "provider_locale")
    private String providerLocale; // Locale từ OAuth2 provider

    // Metadata fields
    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false; // Account chính để hiển thị

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt; // Lần cuối sử dụng

    @CreationTimestamp
    @Column(name = "linked_at", updatable = false)
    private LocalDateTime linkedAt; // Thời gian liên kết

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isGoogle() {
        return "google".equalsIgnoreCase(provider);
    }

    public boolean isFacebook() {
        return "facebook".equalsIgnoreCase(provider);
    }

    public boolean isGitHub() {
        return "github".equalsIgnoreCase(provider);
    }

    public String getDisplayName() {
        return providerName != null ? providerName : "User";
    }

    public String getDisplayPicture() {
        return providerPicture;
    }

    public String getDisplayEmail() {
        return providerEmail;
    }

    public String getDisplayPhone() {
        return providerPhone;
    }

    public String getDisplayBirthday() {
        return providerBirthday;
    }

    public String getDisplayGender() {
        return providerGender;
    }

    public String getDisplayLocale() {
        return providerLocale;
    }

    /**
     * Update last used timestamp
     */
    public void markAsUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Set as primary account
     */
    public void setAsPrimary() {
        this.isPrimary = true;
    }

    /**
     * Check if this is the primary account
     */
    public boolean isPrimaryAccount() {
        return Boolean.TRUE.equals(isPrimary);
    }
}
