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
 * Flexible OAuth2 User Service that handles missing fields gracefully.
 * This service provides fallback values and flexible field handling for OAuth2 providers.
 */
@Service
public class FlexibleOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlexibleOAuth2UserService.class);

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        logger.debug("Loading OAuth2 user for provider: {}", registrationId);
        logger.debug("OAuth2UserRequest details: {}", userRequest);
        logger.debug("ClientRegistration details: {}", userRequest.getClientRegistration());
        logger.debug("Access token: {}", userRequest.getAccessToken());
        logger.debug("Access token value: {}", userRequest.getAccessToken().getTokenValue());
        logger.debug("Access token scopes: {}", userRequest.getAccessToken().getScopes());
        logger.debug("Access token token type: {}", userRequest.getAccessToken().getTokenType());
        logger.debug("Access token issued at: {}", userRequest.getAccessToken().getIssuedAt());
        
        try {
            // Delegate to default implementation
            OAuth2User oAuth2User = super.loadUser(userRequest);
            
            // DEBUG: Log all raw attributes from provider
            logger.debug("Raw OAuth2 attributes from {}: {}", registrationId, oAuth2User.getAttributes());
            logger.debug("OAuth2User authorities: {}", oAuth2User.getAuthorities());
            logger.debug("OAuth2User name: {}", oAuth2User.getName());
            
            // Apply flexible field handling
            OAuth2User flexibleUser = applyFlexibleFieldHandling(oAuth2User, userRequest);
            
            logger.info("OAuth2 user loaded successfully for provider: {}", registrationId);
            return flexibleUser;
            
        } catch (Exception e) {
            logger.error("Failed to load OAuth2 user for provider: {}", registrationId, e);
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_user_load_failed", "Failed to load OAuth2 user", null), e);
        }
    }

    /**
     * Apply flexible field handling to OAuth2 user attributes.
     * This method ensures all required fields have fallback values.
     */
    private OAuth2User applyFlexibleFieldHandling(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        
        logger.debug("Applying flexible field handling for provider: {}", registrationId);
        logger.debug("Original attributes: {}", attributes.keySet());
        logger.debug("Attribute values: {}", attributes);
        logger.debug("OAuth2User authorities: {}", oAuth2User.getAuthorities());
        logger.debug("OAuth2UserRequest details: {}", userRequest);
        logger.debug("ClientRegistration details: {}", userRequest.getClientRegistration());
        logger.debug("Access token: {}", userRequest.getAccessToken());
        logger.debug("Access token value: {}", userRequest.getAccessToken().getTokenValue());
        logger.debug("Access token scopes: {}", userRequest.getAccessToken().getScopes());
        logger.debug("OAuth2User name: {}", oAuth2User.getName());
        logger.debug("Access token token type: {}", userRequest.getAccessToken().getTokenType());
        logger.debug("OAuth2User authorities: {}", oAuth2User.getAuthorities());
        
        // Handle different OAuth2 providers
        switch (registrationId.toLowerCase()) {
            case "google":
                return handleGoogleOAuth2User(attributes, oAuth2User);
            case "facebook":
                return handleFacebookOAuth2User(attributes, oAuth2User);
            default:
                return handleGenericOAuth2User(attributes, oAuth2User, registrationId);
        }
    }

    /**
     * Handle Google OAuth2 user with flexible field requirements.
     */
    private OAuth2User handleGoogleOAuth2User(Map<String, Object> attributes, OAuth2User originalUser) {
        // DEBUG: Log all raw attributes from Google
        logger.debug("Raw Google OAuth2 attributes: {}", attributes);
        logger.debug("Google OAuth2 attribute keys: {}", attributes.keySet());
        logger.debug("Google OAuth2 attribute values: {}", attributes);
        logger.debug("Google OAuth2 original user authorities: {}", originalUser.getAuthorities());
        logger.debug("Google OAuth2 original user name: {}", originalUser.getName());
        logger.debug("Google OAuth2 original user attributes: {}", originalUser.getAttributes());
        logger.debug("Google OAuth2 original user authorities: {}", originalUser.getAuthorities());
        logger.debug("Google OAuth2 original user name: {}", originalUser.getName());
        
        // Ensure 'id' field is present
        String id = getFlexibleId(attributes, "google");
        attributes.put("id", id);
        logger.debug("Google OAuth2 ID set to: {}", id);
        
        // Ensure 'sub' field is present for OIDC compatibility
        if (!attributes.containsKey("sub")) {
            attributes.put("sub", id);
            logger.debug("Google OAuth2 'sub' field set to: {}", id);
        }
        
        // Ensure 'name' field is present
        String name = getFlexibleName(attributes);
        attributes.put("name", name);
        logger.debug("Google OAuth2 name set to: {}", name);
        
        // CRITICAL: Ensure 'email' field is present
        String email = getFlexibleEmail(attributes);
        if (email == null || email.isEmpty()) {
            logger.error("Google OAuth2 response missing email field. Available attributes: {}", attributes.keySet());
            logger.error("Google OAuth2 attribute values: {}", attributes);
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_email_required", 
                "Email is required but not provided by Google OAuth2", null));
        }
        logger.debug("Google OAuth2 email found: {}", email);
        
        // Ensure 'email_verified' field is present
        Boolean emailVerified = getFlexibleEmailVerified(attributes);
        attributes.put("email_verified", emailVerified);
        logger.debug("Google OAuth2 email_verified set to: {}", emailVerified);
        
        // Ensure 'picture' field is present
        String picture = getFlexiblePicture(attributes);
        attributes.put("picture", picture);
        logger.debug("Google OAuth2 picture set to: {}", picture);
        
        logger.info("Google OAuth2 user processed successfully - ID: {}, Name: {}, Email: {}", id, name, email);
        return new DefaultOAuth2User(originalUser.getAuthorities(), attributes, "id");
    }

    /**
     * Handle Facebook OAuth2 user with flexible field requirements.
     */
    private OAuth2User handleFacebookOAuth2User(Map<String, Object> attributes, OAuth2User originalUser) {
        logger.debug("Raw Facebook OAuth2 attributes: {}", attributes);
        logger.debug("Facebook OAuth2 attribute keys: {}", attributes.keySet());
        logger.debug("Facebook OAuth2 attribute values: {}", attributes);
        logger.debug("Facebook OAuth2 original user authorities: {}", originalUser.getAuthorities());
        logger.debug("Facebook OAuth2 original user name: {}", originalUser.getName());
        logger.debug("Facebook OAuth2 original user attributes: {}", originalUser.getAttributes());
        logger.debug("Facebook OAuth2 original user authorities: {}", originalUser.getAuthorities());
        logger.debug("Facebook OAuth2 original user name: {}", originalUser.getName());
        
        // Ensure 'id' field is present
        String id = getFlexibleId(attributes, "facebook");
        attributes.put("id", id);
        logger.debug("Facebook OAuth2 ID set to: {}", id);
        
        // Ensure 'sub' field is present for OIDC compatibility
        if (!attributes.containsKey("sub")) {
            attributes.put("sub", id);
            logger.debug("Facebook OAuth2 'sub' field set to: {}", id);
        }
        
        // Ensure 'name' field is present
        String name = getFlexibleName(attributes);
        attributes.put("name", name);
        logger.debug("Facebook OAuth2 name set to: {}", name);
        
        // Ensure 'email' field is present
        String email = getFlexibleEmail(attributes);
        if (email == null || email.isEmpty()) {
            logger.error("Facebook OAuth2 response missing email field. Available attributes: {}", attributes.keySet());
            logger.error("Facebook OAuth2 attribute values: {}", attributes);
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_email_required", "Email is required for authentication", null));
        }
        logger.debug("Facebook OAuth2 email found: {}", email);
        
        // Ensure 'email_verified' field is present
        Boolean emailVerified = getFlexibleEmailVerified(attributes);
        attributes.put("email_verified", emailVerified);
        logger.debug("Facebook OAuth2 email_verified set to: {}", emailVerified);
        
        // Ensure 'picture' field is present
        String picture = getFlexiblePicture(attributes);
        attributes.put("picture", picture);
        logger.debug("Facebook OAuth2 picture set to: {}", picture);
        
        logger.info("Facebook OAuth2 user processed successfully - ID: {}, Name: {}, Email: {}", id, name, email);
        return new DefaultOAuth2User(originalUser.getAuthorities(), attributes, "id");
    }

    /**
     * Handle generic OAuth2 user with flexible field requirements.
     */
    private OAuth2User handleGenericOAuth2User(Map<String, Object> attributes, OAuth2User originalUser, String provider) {
        logger.debug("Raw {} OAuth2 attributes: {}", provider, attributes);
        logger.debug("{} OAuth2 attribute keys: {}", provider, attributes.keySet());
        logger.debug("{} OAuth2 attribute values: {}", provider, attributes);
        logger.debug("{} OAuth2 original user authorities: {}", provider, originalUser.getAuthorities());
        logger.debug("{} OAuth2 original user name: {}", provider, originalUser.getName());
        logger.debug("{} OAuth2 original user attributes: {}", provider, originalUser.getAttributes());
        logger.debug("{} OAuth2 original user authorities: {}", provider, originalUser.getAuthorities());
        logger.debug("{} OAuth2 original user name: {}", provider, originalUser.getName());
        
        // Ensure 'id' field is present
        String id = getFlexibleId(attributes, provider);
        attributes.put("id", id);
        logger.debug("{} OAuth2 ID set to: {}", provider, id);
        
        // Ensure 'sub' field is present for OIDC compatibility
        if (!attributes.containsKey("sub")) {
            attributes.put("sub", id);
            logger.debug("{} OAuth2 'sub' field set to: {}", provider, id);
        }
        
        // Ensure 'name' field is present
        String name = getFlexibleName(attributes);
        attributes.put("name", name);
        logger.debug("{} OAuth2 name set to: {}", provider, name);
        
        // Ensure 'email' field is present
        String email = getFlexibleEmail(attributes);
        if (email == null || email.isEmpty()) {
            logger.error("{} OAuth2 response missing email field. Available attributes: {}", provider, attributes.keySet());
            logger.error("{} OAuth2 attribute values: {}", provider, attributes);
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_email_required", "Email is required for authentication", null));
        }
        logger.debug("{} OAuth2 email found: {}", provider, email);
        
        // Ensure 'email_verified' field is present
        Boolean emailVerified = getFlexibleEmailVerified(attributes);
        attributes.put("email_verified", emailVerified);
        logger.debug("{} OAuth2 email_verified set to: {}", provider, emailVerified);
        
        // Ensure 'picture' field is present
        String picture = getFlexiblePicture(attributes);
        attributes.put("picture", picture);
        logger.debug("{} OAuth2 picture set to: {}", provider, picture);
        
        logger.info("{} OAuth2 user processed successfully - ID: {}, Name: {}, Email: {}", provider, id, name, email);
        return new DefaultOAuth2User(originalUser.getAuthorities(), attributes, "id");
    }

    /**
     * Get flexible ID with fallback logic.
     */
    private String getFlexibleId(Map<String, Object> attributes, String provider) {
        logger.debug("Getting flexible ID for provider: {} with attributes: {}", provider, attributes.keySet());
        logger.debug("Attribute values for ID lookup: {}", attributes);
        
        // Try multiple possible field names for ID
        String[] possibleIdFields = {"id", "sub", "user_id", "uid", "userid"};
        
        for (String field : possibleIdFields) {
            String id = (String) attributes.get(field);
            if (id != null && !id.isEmpty()) {
                logger.debug("Found ID in field '{}': {}", field, id);
                return id;
            }
        }
        
        // Generate fallback ID from email
        String email = (String) attributes.get("email");
        if (email != null && !email.isEmpty()) {
            String fallbackId = provider + "_" + email.hashCode();
            logger.warn("Generated fallback ID for {} user from email: {}", provider, fallbackId);
            return fallbackId;
        }
        
        // Generate fallback ID from timestamp
        String timestampId = provider + "_" + System.currentTimeMillis();
        logger.warn("Generated timestamp fallback ID for {} user: {}", provider, timestampId);
        return timestampId;
    }

    /**
     * Get flexible name with fallback logic.
     */
    private String getFlexibleName(Map<String, Object> attributes) {
        logger.debug("Getting flexible name from attributes: {}", attributes.keySet());
        logger.debug("Attribute values for name lookup: {}", attributes);
        
        // Try multiple possible field names for name
        String[] possibleNameFields = {"name", "display_name", "full_name", "given_name", "first_name"};
        
        for (String field : possibleNameFields) {
            String name = (String) attributes.get(field);
            if (name != null && !name.isEmpty()) {
                logger.debug("Found name in '{}' field: {}", field, name);
                return name;
            }
        }
        
        // Try to construct name from given_name and family_name
        String givenName = (String) attributes.get("given_name");
        String familyName = (String) attributes.get("family_name");
        if (givenName != null || familyName != null) {
            String constructedName = (givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "");
            logger.debug("Constructed name from given_name and family_name: {}", constructedName.trim());
            return constructedName.trim();
        }
        
        // Fallback to email prefix
        String email = (String) attributes.get("email");
        if (email != null && !email.isEmpty()) {
            String emailPrefix = email.split("@")[0];
            logger.debug("Using email prefix as name: {}", emailPrefix);
            return emailPrefix;
        }
        
        // Final fallback
        logger.debug("Using final fallback name: User");
        return "User";
    }

    /**
     * Get flexible email with fallback logic.
     */
    private String getFlexibleEmail(Map<String, Object> attributes) {
        logger.debug("Getting flexible email from attributes: {}", attributes.keySet());
        logger.debug("Attribute values for email lookup: {}", attributes);
        
        String email = (String) attributes.get("email");
        if (email != null && !email.isEmpty()) {
            logger.debug("Found email in 'email' field: {}", email);
            return email;
        }
        
        // Try alternative email fields
        String[] possibleEmailFields = {"email_address", "mail", "user_email"};
        for (String field : possibleEmailFields) {
            email = (String) attributes.get(field);
            if (email != null && !email.isEmpty()) {
                logger.debug("Found email in '{}' field: {}", field, email);
                return email;
            }
        }
        
        logger.error("No email found in any field. Available attributes: {}", attributes.keySet());
        logger.error("Attribute values: {}", attributes);
        return null; // Email is required, so return null to trigger error
    }

    /**
     * Get flexible email verified status with fallback logic.
     */
    private Boolean getFlexibleEmailVerified(Map<String, Object> attributes) {
        logger.debug("Getting flexible email_verified from attributes: {}", attributes.keySet());
        logger.debug("Attribute values for email_verified lookup: {}", attributes);
        
        Boolean emailVerified = (Boolean) attributes.get("email_verified");
        if (emailVerified != null) {
            logger.debug("Found email_verified field: {}", emailVerified);
            return emailVerified;
        }
        
        // For OAuth2 providers, assume email is verified if it exists
        String email = (String) attributes.get("email");
        boolean isVerified = email != null && !email.isEmpty();
        logger.debug("Assuming email is verified based on existence: {}", isVerified);
        return isVerified;
    }

    /**
     * Get flexible picture URL with fallback logic.
     */
    private String getFlexiblePicture(Map<String, Object> attributes) {
        logger.debug("Getting flexible picture from attributes: {}", attributes.keySet());
        logger.debug("Attribute values for picture lookup: {}", attributes);
        
        // Try multiple possible field names for picture
        String[] possiblePictureFields = {"picture", "avatar_url", "profile_picture", "photo", "image"};
        
        for (String field : possiblePictureFields) {
            String picture = (String) attributes.get(field);
            if (picture != null && !picture.isEmpty()) {
                logger.debug("Found picture in '{}' field: {}", field, picture);
                return picture;
            }
        }
        
        // Try nested picture object (Facebook style)
        Object pictureObj = attributes.get("picture");
        if (pictureObj instanceof Map) {
            Map<String, Object> pictureMap = (Map<String, Object>) pictureObj;
            Object data = pictureMap.get("data");
            if (data instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) data;
                String url = (String) dataMap.get("url");
                if (url != null && !url.isEmpty()) {
                    logger.debug("Found picture in nested object: {}", url);
                    return url;
                }
            }
        }
        
        // Return null if no picture found
        logger.debug("No picture found in any field");
        return null;
    }
}



