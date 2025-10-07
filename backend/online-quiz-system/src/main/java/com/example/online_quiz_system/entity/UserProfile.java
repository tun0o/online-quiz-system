package com.example.online_quiz_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    // Map user_id (DB) to userId (entity) and use it as primary key
    @Id
    @Column(name = "user_id")
    private Long userId;

    // Personal info - Made nullable for OAuth2 users
    @Column(name = "full_name")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String fullName;

    @Column(name = "date_of_birth")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String province;
    private String school;
    private String grade;
    private String goal;

    // Contact info - Validation fields
    @Column(name = "emergency_phone")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại Việt Nam không hợp lệ")
    private String emergencyPhone;

    @Column(name = "avatar_url")
    @Pattern(regexp = "^https?://.*", message = "URL không hợp lệ")
    private String avatarUrl;

    @Column(length = 1000)
    @Size(max = 1000, message = "Bio không được vượt quá 1000 ký tự")
    private String bio;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    // OAuth2 extended info - can be null (synced from OAuth2Account)
    @Column(name = "oauth2_phone")
    private String oauth2Phone; // Phone from OAuth2 provider
    
    @Column(name = "oauth2_birthday")
    private String oauth2Birthday; // Birthday from OAuth2 provider
    
    @Column(name = "oauth2_gender")
    private String oauth2Gender; // Gender from OAuth2 provider
    
    @Column(name = "oauth2_locale")
    private String oauth2Locale; // Locale from OAuth2 provider
    
    @Column(name = "oauth2_provider")
    private String oauth2Provider; // Provider name (google, facebook, etc.)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public UserProfile(com.example.online_quiz_system.entity.User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.emailVerified = user.getIsVerified();
        this.grade = user.getGrade();
        this.goal = user.getGoal();
        
        // Use OAuth2 data from User helper methods (now uses OAuth2Account)
        this.fullName = user.getDisplayName(); // Use helper method
        this.avatarUrl = user.getDisplayPicture(); // Use helper method
        
        // Get OAuth2 data from primary OAuth2Account
        OAuth2Account primaryAccount = user.getPrimaryOAuth2Account();
        if (primaryAccount != null) {
            this.oauth2Provider = primaryAccount.getProvider();
            this.oauth2Phone = primaryAccount.getDisplayPhone();
            this.oauth2Birthday = primaryAccount.getDisplayBirthday();
            this.oauth2Gender = primaryAccount.getDisplayGender();
            this.oauth2Locale = primaryAccount.getDisplayLocale();
        }
    }
    
    /**
     * Constructor with OAuth2Account for explicit sync
     */
    public UserProfile(com.example.online_quiz_system.entity.User user, OAuth2Account oauth2Account) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.emailVerified = user.getIsVerified();
        this.grade = user.getGrade();
        this.goal = user.getGoal();
        
        // Use OAuth2 data from specific OAuth2Account
        this.fullName = oauth2Account.getDisplayName();
        this.avatarUrl = oauth2Account.getDisplayPicture();
        this.oauth2Provider = oauth2Account.getProvider();
        this.oauth2Phone = oauth2Account.getDisplayPhone();
        this.oauth2Birthday = oauth2Account.getDisplayBirthday();
        this.oauth2Gender = oauth2Account.getDisplayGender();
        this.oauth2Locale = oauth2Account.getDisplayLocale();
    }

    // Constructor cho việc tạo mới với userId
    public UserProfile(Long userId, String email, Boolean emailVerified, String grade, String goal) {
        this.userId = userId;
        this.email = email;
        this.emailVerified = emailVerified;
        this.grade = grade;
        this.goal = goal;
        this.fullName = "Chưa cập nhật";
    }
}