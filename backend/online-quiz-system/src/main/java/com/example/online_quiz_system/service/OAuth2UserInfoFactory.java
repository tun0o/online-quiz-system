package com.example.online_quiz_system.service;

import java.util.Map;

/**
 * Factory class to create OAuth2UserInfo instances for different providers.
 */
public class OAuth2UserInfoFactory {
    
    /**
     * Check if the provider is supported.
     */
    public static boolean isProviderSupported(String registrationId) {
        return "google".equalsIgnoreCase(registrationId) || 
               "facebook".equalsIgnoreCase(registrationId);
    }
    
    /**
     * Create OAuth2UserInfo instance based on provider.
     */
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if ("facebook".equalsIgnoreCase(registrationId)) {
            return new FacebookOAuth2UserInfo(attributes);
        } else {
            throw new IllegalArgumentException("Provider not supported: " + registrationId);
        }
    }
}