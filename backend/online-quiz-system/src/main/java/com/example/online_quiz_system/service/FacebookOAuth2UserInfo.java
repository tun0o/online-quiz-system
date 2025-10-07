package com.example.online_quiz_system.service;

import java.util.Map;

/**
 * Facebook OAuth2 user information implementation.
 */
public class FacebookOAuth2UserInfo implements OAuth2UserInfo {
    
    private final Map<String, Object> attributes;

    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        String id = (String) attributes.get("id");
        if (id == null || id.isEmpty()) {
            // Generate fallback ID from email
            String email = getEmail();
            if (email != null && !email.isEmpty()) {
                id = "facebook_" + email.hashCode();
            }
        }
        return id;
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> picture = (Map<String, Object>) attributes.get("picture");
        if (picture != null) {
            Map<String, Object> data = (Map<String, Object>) picture.get("data");
            if (data != null) {
                return (String) data.get("url");
            }
        }
        return null;
    }

    @Override
    public boolean isEmailVerified() {
        // Facebook doesn't provide email_verified field, assume verified if email exists
        return getEmail() != null && !getEmail().isEmpty();
    }

    @Override
    public String getPhone() {
        return (String) attributes.get("phone");
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