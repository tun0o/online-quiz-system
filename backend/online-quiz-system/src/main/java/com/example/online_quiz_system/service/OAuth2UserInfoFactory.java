package com.example.online_quiz_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Objects;

/**
 * Factory class for creating OAuth2UserInfo instances based on provider type.
 * Uses strategy pattern for better maintainability and extensibility.
 */
public class OAuth2UserInfoFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserInfoFactory.class);
    
    // Supported providers
    public static final String GOOGLE_PROVIDER = "google";
    public static final String FACEBOOK_PROVIDER = "facebook";
    
    /**
     * Creates appropriate OAuth2UserInfo instance based on registration ID.
     * 
     * @param registrationId The OAuth2 provider registration ID
     * @param attributes User attributes from OAuth2 provider
     * @return OAuth2UserInfo instance for the specific provider
     * @throws IllegalArgumentException if provider is not supported
     */
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        Objects.requireNonNull(registrationId, "Registration ID cannot be null");
        Objects.requireNonNull(attributes, "Attributes cannot be null");
        
        String normalizedProvider = registrationId.toLowerCase().trim();
        
        logger.debug("Creating OAuth2UserInfo for provider: {}", normalizedProvider);
        
        return switch (normalizedProvider) {
            case GOOGLE_PROVIDER -> new GoogleOAuth2UserInfo(attributes);
            case FACEBOOK_PROVIDER -> new FacebookOAuth2UserInfo(attributes);
            default -> {
                String errorMsg = String.format("OAuth2 provider '%s' is not supported. Supported providers: %s, %s", 
                    registrationId, GOOGLE_PROVIDER, FACEBOOK_PROVIDER);
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        };
    }
    
    /**
     * Checks if a provider is supported.
     * 
     * @param registrationId The provider registration ID
     * @return true if provider is supported, false otherwise
     */
    public static boolean isProviderSupported(String registrationId) {
        if (registrationId == null) return false;
        String normalized = registrationId.toLowerCase().trim();
        return GOOGLE_PROVIDER.equals(normalized) || FACEBOOK_PROVIDER.equals(normalized);
    }
}
