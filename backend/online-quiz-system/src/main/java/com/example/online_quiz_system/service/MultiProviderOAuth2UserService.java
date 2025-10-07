package com.example.online_quiz_system.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Multi-Provider OAuth2 User Service that handles both Google and Facebook OAuth2 flows.
 * This service provides unified handling for different OAuth2 providers.
 */
@Service
public class MultiProviderOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiProviderOAuth2UserService.class);

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        logger.debug("MultiProviderOAuth2UserService: Loading OAuth2 user for provider: {}", registrationId);
        
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            logger.debug("MultiProviderOAuth2UserService: Raw {} attributes: {}", registrationId, oAuth2User.getAttributes());
            
            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
            
            // Process based on provider
            if ("google".equals(registrationId)) {
                return processGoogleUser(attributes, oAuth2User);
            } else if ("facebook".equals(registrationId)) {
                return processFacebookUser(attributes, oAuth2User);
            } else {
                // Generic fallback for other providers
                return processGenericUser(attributes, oAuth2User, registrationId);
            }
            
        } catch (Exception e) {
            logger.error("MultiProviderOAuth2UserService: Failed to load {} OAuth2 user", registrationId, e);
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_user_load_failed",
                "Failed to load " + registrationId + " OAuth2 user", null), e);
        }
    }
    
    /**
     * Process Google OAuth2 user attributes.
     */
    private OAuth2User processGoogleUser(Map<String, Object> attributes, OAuth2User originalUser) {
        logger.debug("Processing Google OAuth2 user");
        
        // Validate required Google attributes
        if (!validateGoogleAttributes(attributes)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("google_attributes_invalid",
                "Invalid Google OAuth2 attributes received", null));
        }
        
        String id = (String) attributes.get("id");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        Boolean emailVerified = (Boolean) attributes.getOrDefault("email_verified", false);
        String picture = (String) attributes.get("picture");
        
        // Ensure required fields
        if (id == null || id.isEmpty()) {
            logger.error("Google response missing 'id' field. Available attributes: {}", attributes.keySet());
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_id_required", 
                "ID is required but not provided by Google OAuth2", null));
        }
        
        if (email == null || email.isEmpty()) {
            logger.error("Google response missing 'email' field. Available attributes: {}", attributes.keySet());
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_email_required", 
                "Email is required but not provided by Google OAuth2", null));
        }
        
        // Set defaults for missing optional fields
        if (name == null || name.isEmpty()) {
            name = email.split("@")[0];
            attributes.put("name", name);
        }
        
        if (picture == null) {
            picture = "";
            attributes.put("picture", picture);
        }
        
        attributes.put("email_verified", emailVerified);
        attributes.put("oauth2_provider", "google");
        
        logger.info("Google OAuth2 user processed successfully - ID: {}, Name: {}, Email: {}", id, name, email);
        
        return new DefaultOAuth2User(originalUser.getAuthorities(), attributes, "id");
    }
    
    /**
     * Process Facebook OAuth2 user attributes.
     */
    private OAuth2User processFacebookUser(Map<String, Object> attributes, OAuth2User originalUser) {
        logger.debug("Processing Facebook OAuth2 user");
        
        // Validate required Facebook attributes
        if (!validateFacebookAttributes(attributes)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("facebook_attributes_invalid",
                "Invalid Facebook OAuth2 attributes received", null));
        }
        
        String id = (String) attributes.get("id");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = extractFacebookPicture(attributes);
        
        // Ensure required fields
        if (id == null || id.isEmpty()) {
            logger.error("Facebook response missing 'id' field. Available attributes: {}", attributes.keySet());
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_id_required", 
                "ID is required but not provided by Facebook OAuth2", null));
        }
        
        if (email == null || email.isEmpty()) {
            logger.error("Facebook response missing 'email' field. Available attributes: {}", attributes.keySet());
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_email_required", 
                "Email is required but not provided by Facebook OAuth2", null));
        }
        
        // Set defaults for missing optional fields
        if (name == null || name.isEmpty()) {
            name = email.split("@")[0];
            attributes.put("name", name);
        }
        
        if (picture == null) {
            picture = "";
            attributes.put("picture", picture);
        }
        
        attributes.put("email_verified", true); // Facebook emails are typically verified
        attributes.put("oauth2_provider", "facebook");
        
        logger.info("Facebook OAuth2 user processed successfully - ID: {}, Name: {}, Email: {}", id, name, email);
        
        return new DefaultOAuth2User(originalUser.getAuthorities(), attributes, "id");
    }
    
    /**
     * Process generic OAuth2 user attributes for other providers.
     */
    private OAuth2User processGenericUser(Map<String, Object> attributes, OAuth2User originalUser, String provider) {
        logger.debug("Processing generic OAuth2 user for provider: {}", provider);
        
        String id = (String) attributes.get("id");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        
        // Generate fallback values if missing
        if (id == null || id.isEmpty()) {
            id = "generated_" + System.currentTimeMillis();
            attributes.put("id", id);
        }
        
        if (email == null || email.isEmpty()) {
            email = "user_" + System.currentTimeMillis() + "@" + provider + ".com";
            attributes.put("email", email);
        }
        
        if (name == null || name.isEmpty()) {
            name = email.split("@")[0];
            attributes.put("name", name);
        }
        
        attributes.put("oauth2_provider", provider);
        attributes.put("email_verified", false);
        
        logger.info("Generic OAuth2 user processed for {} - ID: {}, Name: {}, Email: {}", provider, id, name, email);
        
        return new DefaultOAuth2User(originalUser.getAuthorities(), attributes, "id");
    }
    
    /**
     * Validate Google OAuth2 attributes.
     */
    private boolean validateGoogleAttributes(Map<String, Object> attributes) {
        return attributes.containsKey("id") && attributes.containsKey("email");
    }
    
    /**
     * Validate Facebook OAuth2 attributes.
     */
    private boolean validateFacebookAttributes(Map<String, Object> attributes) {
        return attributes.containsKey("id") && attributes.containsKey("email");
    }
    
    /**
     * Extract Facebook profile picture URL.
     */
    private String extractFacebookPicture(Map<String, Object> attributes) {
        Object pictureObj = attributes.get("picture");
        if (pictureObj instanceof Map) {
            Map<String, Object> pictureData = (Map<String, Object>) pictureObj;
            Object dataObj = pictureData.get("data");
            if (dataObj instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) dataObj;
                return (String) data.get("url");
            }
        }
        return null;
    }
}
