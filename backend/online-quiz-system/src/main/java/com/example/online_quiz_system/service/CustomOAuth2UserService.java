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
 * Custom OAuth2UserService to handle missing 'id' field from Google OAuth2.
 * Fixes the issue where Google OAuth2 returns user info without 'id' (sub) field.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.debug("Loading OAuth2 user for provider: {}", userRequest.getClientRegistration().getRegistrationId());
        
        try {
            // Delegate to default implementation (calls provider's userinfo endpoint)
            OAuth2User oAuth2User = super.loadUser(userRequest);
            
            // Fix missing 'id' field issue
            OAuth2User fixedUser = fixMissingIdField(oAuth2User, userRequest);
            
            logger.debug("OAuth2 user loaded successfully with fixed attributes");
            return fixedUser;
            
        } catch (Exception e) {
            logger.error("Failed to load OAuth2 user", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_user_load_failed", "Failed to load OAuth2 user", null), e);
        }
    }

    /**
     * Fix missing 'id' field in OAuth2User attributes.
     * Google OAuth2 sometimes returns user info without 'id' field, causing Spring Security to fail.
     */
    private OAuth2User fixMissingIdField(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        logger.debug("Fixing missing 'id' field for provider: {}", registrationId);
        logger.debug("Original attributes: {}", attributes.keySet());
        
        // Handle different OAuth2 providers
        switch (registrationId.toLowerCase()) {
            case "google":
                return fixGoogleOAuth2User(attributes, oAuth2User);
            case "facebook":
                return fixFacebookOAuth2User(attributes, oAuth2User);
            default:
                return fixGenericOAuth2User(attributes, oAuth2User, registrationId);
        }
    }

    /**
     * Fix Google OAuth2 user attributes.
     */
    private OAuth2User fixGoogleOAuth2User(Map<String, Object> attributes, OAuth2User originalUser) {
        // Google OAuth2 attributes mapping
        String id = (String) attributes.get("id");
        if (id == null || id.isEmpty()) {
            // Try alternative field names
            id = (String) attributes.get("sub");
            if (id == null || id.isEmpty()) {
                id = (String) attributes.get("user_id");
            }
            if (id == null || id.isEmpty()) {
                // Generate a fallback ID based on email
                String email = (String) attributes.get("email");
                if (email != null && !email.isEmpty()) {
                    id = "google_" + email.hashCode();
                    logger.warn("Generated fallback ID for Google user: {}", id);
                } else {
                    throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_user_missing_fields", "Google OAuth2 user missing both 'id' and 'email' fields", null));
                }
            }
            attributes.put("id", id);
        }
        
        // Ensure required fields are present
        if (!attributes.containsKey("name")) {
            String givenName = (String) attributes.get("given_name");
            String familyName = (String) attributes.get("family_name");
            if (givenName != null || familyName != null) {
                attributes.put("name", (givenName != null ? givenName : "") + " " + (familyName != null ? familyName : ""));
            }
        }
        
        // Ensure 'sub' field is present for OIDC compatibility
        if (!attributes.containsKey("sub")) {
            attributes.put("sub", id);
        }
        
        logger.debug("Fixed Google OAuth2 user attributes, ID: {}, SUB: {}", id, attributes.get("sub"));
        return new DefaultOAuth2User(originalUser.getAuthorities(), attributes, "sub");
    }

    /**
     * Fix Facebook OAuth2 user attributes.
     */
    private OAuth2User fixFacebookOAuth2User(Map<String, Object> attributes, OAuth2User originalUser) {
        String id = (String) attributes.get("id");
        if (id == null || id.isEmpty()) {
            String email = (String) attributes.get("email");
            if (email != null && !email.isEmpty()) {
                id = "facebook_" + email.hashCode();
                attributes.put("id", id);
                logger.warn("Generated fallback ID for Facebook user: {}", id);
            } else {
                throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_user_missing_fields", "Facebook OAuth2 user missing both 'id' and 'email' fields", null));
            }
        }
        
        // Ensure 'sub' field is present for OIDC compatibility
        if (!attributes.containsKey("sub")) {
            attributes.put("sub", id);
        }
        
        logger.debug("Fixed Facebook OAuth2 user attributes, ID: {}, SUB: {}", id, attributes.get("sub"));
        return new DefaultOAuth2User(originalUser.getAuthorities(), attributes, "sub");
    }

    /**
     * Fix generic OAuth2 user attributes.
     */
    private OAuth2User fixGenericOAuth2User(Map<String, Object> attributes, OAuth2User originalUser, String provider) {
        String id = (String) attributes.get("id");
        if (id == null || id.isEmpty()) {
            // Try common field names
            String[] possibleIdFields = {"sub", "user_id", "uid", "userid"};
            for (String field : possibleIdFields) {
                id = (String) attributes.get(field);
                if (id != null && !id.isEmpty()) {
                    attributes.put("id", id);
                    break;
                }
            }
            
            if (id == null || id.isEmpty()) {
                String email = (String) attributes.get("email");
                if (email != null && !email.isEmpty()) {
                    id = provider + "_" + email.hashCode();
                    attributes.put("id", id);
                    logger.warn("Generated fallback ID for {} user: {}", provider, id);
                } else {
                    throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_user_missing_fields", provider + " OAuth2 user missing 'id' field and no email for fallback", null));
                }
            }
        }
        
        // Ensure 'sub' field is present for OIDC compatibility
        if (!attributes.containsKey("sub")) {
            attributes.put("sub", id);
        }
        
        logger.debug("Fixed {} OAuth2 user attributes, ID: {}, SUB: {}", provider, id, attributes.get("sub"));
        return new DefaultOAuth2User(originalUser.getAuthorities(), attributes, "sub");
    }
}