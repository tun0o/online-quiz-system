package com.example.online_quiz_system.service;

import java.util.Map;

/**
 * Interface for OAuth2 user information from different providers.
 */
public interface OAuth2UserInfo {
    String getId();
    String getName();
    String getEmail();
    String getImageUrl();
    boolean isEmailVerified();
    
    // Extended info methods - can return null
    String getPhone();
    String getBirthday();
    String getGender();
    String getLocale();
    
    Map<String, Object> getAttributes();
}