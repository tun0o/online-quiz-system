package com.example.online_quiz_system.service;

import java.util.Map;

/**
 * Google OAuth2 user information implementation.
 */
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    
    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        // Try multiple possible field names for ID
        String id = (String) attributes.get("id");
        if (id == null || id.isEmpty()) {
            id = (String) attributes.get("sub");
        }
        if (id == null || id.isEmpty()) {
            id = (String) attributes.get("user_id");
        }
        if (id == null || id.isEmpty()) {
            // Generate fallback ID from email
            String email = getEmail();
            if (email != null && !email.isEmpty()) {
                id = "google_" + email.hashCode();
            }
        }
        return id;
    }

    @Override
    public String getName() {
        String name = (String) attributes.get("name");
        if (name == null || name.isEmpty()) {
            // Try to construct name from given_name and family_name
            String givenName = (String) attributes.get("given_name");
            String familyName = (String) attributes.get("family_name");
            if (givenName != null || familyName != null) {
                name = (givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "");
                name = name.trim();
            }
        }
        return name;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }

    @Override
    public boolean isEmailVerified() {
        Boolean emailVerified = (Boolean) attributes.get("email_verified");
        return emailVerified != null && emailVerified;
    }

    @Override
    public String getPhone() {
        return (String) attributes.get("phone_number");
    }

    @Override
    public String getBirthday() {
        return (String) attributes.get("birthday");
    }

    @Override
    public String getGender() {
        return (String) attributes.get("gender");
    }

    @Override
    public String getLocale() {
        return (String) attributes.get("locale");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}