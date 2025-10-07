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
 * Custom Google OAuth2 User Service that handles Google-specific OAuth2 flow.
 * This service bypasses the default Spring Security OAuth2 user loading
 * and directly handles Google OAuth2 responses.
 */
@Service
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuth2UserService.class);

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        logger.debug("GoogleOAuth2UserService: Loading OAuth2 user for provider: {}", registrationId);
        
        // ADVANCED FIX: Enhanced provider detection
        if (!isGoogleProvider(registrationId)) {
            logger.debug("GoogleOAuth2UserService: Not Google provider ({}), delegating to parent", registrationId);
            return super.loadUser(userRequest);
        }
        
        try {
            // Delegate to default implementation to get raw attributes
            OAuth2User oAuth2User = super.loadUser(userRequest);
            
            logger.debug("GoogleOAuth2UserService: Raw Google attributes: {}", oAuth2User.getAttributes());
            logger.debug("GoogleOAuth2UserService: Google authorities: {}", oAuth2User.getAuthorities());
            logger.debug("GoogleOAuth2UserService: Google name: {}", oAuth2User.getName());
            
            // Process Google-specific attributes
            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
            
            // ADVANCED FIX: Validate attributes before processing
            if (!validateGoogleAttributes(attributes)) {
                throw new OAuth2AuthenticationException(new OAuth2Error("google_attributes_invalid", 
                    "Invalid Google OAuth2 attributes received", null));
            }
            
            // Ensure 'id' field is present (Google always provides this)
            String id = (String) attributes.get("id");
            if (id == null || id.isEmpty()) {
                logger.error("GoogleOAuth2UserService: Google response missing 'id' field. Available attributes: {}", attributes.keySet());
                throw new OAuth2AuthenticationException(new OAuth2Error("google_id_required", 
                    "Google OAuth2 response missing required 'id' field", null));
            }
            logger.debug("GoogleOAuth2UserService: Google ID: {}", id);
            
            // Ensure 'email' field is present
            String email = (String) attributes.get("email");
            if (email == null || email.isEmpty()) {
                logger.error("GoogleOAuth2UserService: Google response missing 'email' field. Available attributes: {}", attributes.keySet());
                throw new OAuth2AuthenticationException(new OAuth2Error("google_email_required", 
                    "Google OAuth2 response missing required 'email' field", null));
            }
            logger.debug("GoogleOAuth2UserService: Google email: {}", email);
            
            // Ensure 'name' field is present
            String name = (String) attributes.get("name");
            if (name == null || name.isEmpty()) {
                // Try to construct name from given_name and family_name
                String givenName = (String) attributes.get("given_name");
                String familyName = (String) attributes.get("family_name");
                if (givenName != null || familyName != null) {
                    name = (givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "");
                    name = name.trim();
                } else {
                    // Fallback to email prefix
                    name = email.split("@")[0];
                }
                attributes.put("name", name);
            }
            logger.debug("GoogleOAuth2UserService: Google name: {}", name);
            
            // Ensure 'email_verified' field is present
            Boolean emailVerified = (Boolean) attributes.get("email_verified");
            if (emailVerified == null) {
                // For Google OAuth2, assume email is verified if it exists
                emailVerified = true;
                attributes.put("email_verified", emailVerified);
            }
            logger.debug("GoogleOAuth2UserService: Google email_verified: {}", emailVerified);
            
            // Ensure 'picture' field is present
            String picture = (String) attributes.get("picture");
            if (picture == null) {
                picture = ""; // Empty string instead of null
                attributes.put("picture", picture);
            }
            logger.debug("GoogleOAuth2UserService: Google picture: {}", picture);
            
            // ADVANCED FIX: Get accurate provider name
            String providerName = getProviderName(userRequest);
            logger.info("GoogleOAuth2UserService: Google OAuth2 user processed successfully - Provider: {}, ID: {}, Name: {}, Email: {}", 
                       providerName, id, name, email);
            
            // Create OAuth2User with 'id' as the name attribute
            return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "id");
            
        } catch (Exception e) {
            logger.error("GoogleOAuth2UserService: Failed to load Google OAuth2 user", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("google_oauth2_user_load_failed", 
                "Failed to load Google OAuth2 user", null), e);
        }
    }
    
    /**
     * ADVANCED FIX: Enhanced provider detection for Google.
     */
    private boolean isGoogleProvider(String registrationId) {
        if (registrationId == null) {
            return false;
        }
        
        // Check for various Google provider identifiers
        String lowerId = registrationId.toLowerCase();
        return lowerId.equals("google") || 
               lowerId.equals("google-oauth2") || 
               lowerId.contains("google") ||
               lowerId.startsWith("google");
    }
    
    /**
     * ADVANCED FIX: Enhanced provider name extraction.
     */
    private String getProviderName(OAuth2UserRequest userRequest) {
        try {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            String providerName = userRequest.getClientRegistration().getProviderDetails().getIssuerUri();
            
            if (providerName != null && providerName.contains("google")) {
                return "google";
            }
            
            return registrationId != null ? registrationId.toLowerCase() : "google";
        } catch (Exception e) {
            logger.warn("Failed to extract provider name", e);
            return "google";
        }
    }
    
    /**
     * ADVANCED FIX: Enhanced attribute validation.
     */
    private boolean validateGoogleAttributes(Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            logger.error("GoogleOAuth2UserService: No attributes received from Google");
            return false;
        }
        
        // Check for required Google attributes
        boolean hasId = attributes.containsKey("id") && attributes.get("id") != null;
        boolean hasEmail = attributes.containsKey("email") && attributes.get("email") != null;
        
        if (!hasId) {
            logger.error("GoogleOAuth2UserService: Missing 'id' attribute in Google response");
        }
        
        if (!hasEmail) {
            logger.error("GoogleOAuth2UserService: Missing 'email' attribute in Google response");
        }
        
        return hasId && hasEmail;
    }
}
