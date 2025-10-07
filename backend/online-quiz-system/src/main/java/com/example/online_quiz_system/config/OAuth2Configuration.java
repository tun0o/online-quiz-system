package com.example.online_quiz_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for OAuth2 settings.
 * Centralizes OAuth2-related configuration and provides validation.
 */
@Configuration
@PropertySource("classpath:application.properties")
public class OAuth2Configuration {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${GOOGLE_CLIENT_ID:your-google-client-id.apps.googleusercontent.com}")
    private String googleClientId;

    @Value("${GOOGLE_CLIENT_SECRET:your-google-client-secret}")
    private String googleClientSecret;

    @Value("${FACEBOOK_CLIENT_ID:your-facebook-client-id}")
    private String facebookClientId;

    @Value("${FACEBOOK_CLIENT_SECRET:your-facebook-client-secret}")
    private String facebookClientSecret;

    // OAuth2 Redirect URI Properties
    @Value("${app.oauth2.google.auth.url:http://localhost:8080/oauth2/authorization/google}")
    private String googleAuthUrl;

    @Value("${app.oauth2.facebook.auth.url:http://localhost:8080/oauth2/authorization/facebook}")
    private String facebookAuthUrl;

    @Value("${app.oauth2.google.callback.url:http://localhost:8080/login/oauth2/code/google}")
    private String googleCallbackUrl;

    @Value("${app.oauth2.facebook.callback.url:http://localhost:8080/login/oauth2/code/facebook}")
    private String facebookCallbackUrl;

    @Value("${app.oauth2.frontend.success.url:http://localhost:3000/oauth2/success}")
    private String frontendSuccessUrl;

    @Value("${app.oauth2.frontend.error.url:http://localhost:3000/oauth2/error}")
    private String frontendErrorUrl;

    // OAuth2 URLs
    public static final String GOOGLE_AUTHORIZATION_URL = "/oauth2/authorization/google";
    public static final String FACEBOOK_AUTHORIZATION_URL = "/oauth2/authorization/facebook";
    public static final String OAUTH2_SUCCESS_URL = "/oauth2/success";
    public static final String OAUTH2_ERROR_URL = "/oauth2/error";

    // OAuth2 Scopes
    public static final String GOOGLE_SCOPES = "openid,profile,email";
    public static final String FACEBOOK_SCOPES = "email,public_profile";

    // Error codes
    public static final String ERROR_EMAIL_NOT_PROVIDED = "EMAIL_NOT_PROVIDED";
    public static final String ERROR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    public static final String ERROR_PROVIDER_NOT_SUPPORTED = "PROVIDER_NOT_SUPPORTED";

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public String getGoogleClientSecret() {
        return googleClientSecret;
    }

    public String getFacebookClientId() {
        return facebookClientId;
    }

    public String getFacebookClientSecret() {
        return facebookClientSecret;
    }

    public String getGoogleAuthUrl() {
        return googleAuthUrl;
    }

    public String getFacebookAuthUrl() {
        return facebookAuthUrl;
    }

    public String getGoogleCallbackUrl() {
        return googleCallbackUrl;
    }

    public String getFacebookCallbackUrl() {
        return facebookCallbackUrl;
    }

    public String getFrontendSuccessUrl() {
        return frontendSuccessUrl;
    }

    public String getFrontendErrorUrl() {
        return frontendErrorUrl;
    }

    /**
     * Validates OAuth2 configuration.
     * 
     * @return true if configuration is valid, false otherwise
     */
    public boolean isConfigurationValid() {
        return googleClientId != null && !googleClientId.trim().isEmpty()
            && googleClientSecret != null && !googleClientSecret.trim().isEmpty()
            && facebookClientId != null && !facebookClientId.trim().isEmpty()
            && facebookClientSecret != null && !facebookClientSecret.trim().isEmpty()
            && frontendUrl != null && !frontendUrl.trim().isEmpty();
    }

    /**
     * Gets the full OAuth2 authorization URL for a provider.
     * 
     * @param backendBaseUrl The base URL of the backend server (e.g., "http://localhost:8080")
     * @param provider The OAuth2 provider (google, facebook)
     * @return Full authorization URL
     */
    public String getAuthorizationUrl(String backendBaseUrl, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> backendBaseUrl + GOOGLE_AUTHORIZATION_URL;
            case "facebook" -> backendBaseUrl + FACEBOOK_AUTHORIZATION_URL;
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }

    /**
     * Gets the full OAuth2 success URL.
     * 
     * @return Full success URL
     */
    public String getSuccessUrl() {
        return frontendUrl + OAUTH2_SUCCESS_URL;
    }

    /**
     * Gets the full OAuth2 error URL.
     * 
     * @return Full error URL
     */
    public String getErrorUrl() {
        return frontendUrl + OAUTH2_ERROR_URL;
    }
}