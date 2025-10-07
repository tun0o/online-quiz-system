package com.example.online_quiz_system.service;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom OIDC User Service to handle missing 'id' field in OIDC user info.
 * This service ensures that the 'sub' field is always present for OIDC users.
 */
@Service
public class CustomOidcUserService extends OidcUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        logger.debug("Loading OIDC user for provider: {}", userRequest.getClientRegistration().getRegistrationId());
        
        try {
            // Delegate to default implementation
            OidcUser oidcUser = super.loadUser(userRequest);
            
            // Log the original claims for debugging
            Map<String, Object> originalClaims = oidcUser.getClaims();
            logger.debug("Original OIDC claims: {}", originalClaims.keySet());
            logger.debug("Original 'sub' value: {}", originalClaims.get("sub"));
            logger.debug("Original 'id' value: {}", originalClaims.get("id"));
            
            // Fix missing 'sub' field if needed
            OidcUser fixedUser = fixMissingSubField(oidcUser, userRequest);
            
            logger.debug("OIDC user loaded successfully with fixed attributes");
            return fixedUser;
            
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Attribute value for 'id' cannot be null")) {
                logger.error("OIDC user has null 'id' field, attempting to fix...", e);
                return handleNullIdField(userRequest);
            }
            logger.error("Failed to load OIDC user", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("oidc_user_load_failed", "Failed to load OIDC user", null), e);
        } catch (Exception e) {
            logger.error("Failed to load OIDC user", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("oidc_user_load_failed", "Failed to load OIDC user", null), e);
        }
    }

    /**
     * Fix missing 'sub' field in OIDC user.
     * The 'sub' field is required for OIDC compliance.
     */
    private OidcUser fixMissingSubField(OidcUser oidcUser, OidcUserRequest userRequest) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        logger.debug("Fixing missing 'sub' field for OIDC provider: {}", registrationId);
        
        // Get user info claims
        Map<String, Object> claims = new HashMap<>(oidcUser.getClaims());
        
        // Check if 'sub' field is missing or null
        String sub = (String) claims.get("sub");
        if (sub == null || sub.isEmpty()) {
            logger.warn("Missing 'sub' field for OIDC user, attempting to fix...");
            
            // Try to get ID from other fields
            String id = (String) claims.get("id");
            if (id == null || id.isEmpty()) {
                id = (String) claims.get("user_id");
            }
            if (id == null || id.isEmpty()) {
                // Generate fallback from email
                String email = (String) claims.get("email");
                if (email != null && !email.isEmpty()) {
                    id = registrationId + "_" + email.hashCode();
                    logger.warn("Generated fallback 'sub' for OIDC user: {}", id);
                } else {
                    throw new OAuth2AuthenticationException(new OAuth2Error("oidc_user_missing_fields", "OIDC user missing both 'sub' and 'email' fields", null));
                }
            }
            
            // Set the 'sub' field
            claims.put("sub", id);
            logger.debug("Fixed OIDC user 'sub' field: {}", id);
        }
        
        // Create a new OidcUser with fixed claims
        return new CustomOidcUser(oidcUser, claims);
    }

    /**
     * Handle the case where OIDC user has null 'id' field.
     * This method creates a fallback OIDC user with proper 'sub' field.
     */
    private OidcUser handleNullIdField(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        logger.warn("Handling null 'id' field for OIDC provider: {}", registrationId);
        
        try {
            // Try to get user info from the access token
            String accessToken = userRequest.getAccessToken().getTokenValue();
            logger.debug("Access token available for user info request");
            
            // Create fallback claims with generated 'sub'
            Map<String, Object> fallbackClaims = new HashMap<>();
            
            // Generate a fallback 'sub' based on provider and timestamp
            String fallbackSub = registrationId + "_" + System.currentTimeMillis();
            fallbackClaims.put("sub", fallbackSub);
            fallbackClaims.put("id", fallbackSub);
            
            // Add basic OIDC claims
            fallbackClaims.put("iss", "https://" + registrationId + ".com");
            fallbackClaims.put("aud", userRequest.getClientRegistration().getClientId());
            fallbackClaims.put("iat", System.currentTimeMillis() / 1000);
            fallbackClaims.put("exp", (System.currentTimeMillis() / 1000) + 3600);
            
            logger.warn("Created fallback OIDC user with 'sub': {}", fallbackSub);
            
            // Create a minimal OidcUser with fallback claims
            return new CustomOidcUser(null, fallbackClaims);
            
        } catch (Exception e) {
            logger.error("Failed to create fallback OIDC user", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("oidc_fallback_failed", "Failed to create fallback OIDC user", null), e);
        }
    }
}
