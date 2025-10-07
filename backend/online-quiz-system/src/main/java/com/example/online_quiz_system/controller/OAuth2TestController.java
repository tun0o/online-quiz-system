package com.example.online_quiz_system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for OAuth2 debugging and configuration verification.
 */
@RestController
@RequestMapping("/api/oauth2/test")
public class OAuth2TestController {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2TestController.class);

    @Value("${GOOGLE_CLIENT_ID:NOT_SET}")
    private String googleClientId;

    @Value("${GOOGLE_CLIENT_SECRET:NOT_SET}")
    private String googleClientSecret;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Test endpoint to verify OAuth2 configuration.
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getOAuth2Config() {
        Map<String, Object> config = new HashMap<>();
        
        // Check if environment variables are set
        boolean clientIdSet = !"NOT_SET".equals(googleClientId) && !googleClientId.contains("your-google-client-id");
        boolean clientSecretSet = !"NOT_SET".equals(googleClientSecret) && !googleClientSecret.contains("your-google-client-secret");
        
        config.put("googleClientId", clientIdSet ? "SET" : "NOT_SET");
        config.put("googleClientSecret", clientSecretSet ? "SET" : "NOT_SET");
        config.put("frontendUrl", frontendUrl);
        config.put("oauth2AuthorizationUrl", "/oauth2/authorization/google");
        config.put("oauth2CallbackUrl", "/login/oauth2/code/google");
        config.put("status", "OAuth2 configuration test");
        
        logger.info("OAuth2 Configuration Test - Client ID: {}, Secret: {}", 
                   clientIdSet ? "SET" : "NOT_SET", clientSecretSet ? "SET" : "NOT_SET");
        
        return ResponseEntity.ok(config);
    }

    /**
     * Test endpoint to verify OAuth2 URLs.
     */
    @GetMapping("/urls")
    public ResponseEntity<Map<String, Object>> getOAuth2Urls() {
        Map<String, Object> urls = new HashMap<>();
        
        urls.put("authorizationUrl", "/oauth2/authorization/google");
        urls.put("callbackUrl", "/login/oauth2/code/google");
        urls.put("errorUrl", "/api/oauth2/error");
        urls.put("statusUrl", "/api/oauth2/status");
        urls.put("frontendUrl", frontendUrl);
        
        // Expected Google OAuth2 URLs
        urls.put("expectedGoogleAuthUrl", "https://accounts.google.com/o/oauth2/v2/auth");
        urls.put("expectedCallbackUrl", "http://localhost:8080/login/oauth2/code/google");
        
        return ResponseEntity.ok(urls);
    }

    /**
     * Test endpoint to check OAuth2 health.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getOAuth2Health() {
        Map<String, Object> health = new HashMap<>();
        
        boolean clientIdValid = !"NOT_SET".equals(googleClientId) && !googleClientId.contains("your-google-client-id");
        boolean clientSecretValid = !"NOT_SET".equals(googleClientSecret) && !googleClientSecret.contains("your-google-client-secret");
        
        health.put("status", (clientIdValid && clientSecretValid) ? "HEALTHY" : "UNHEALTHY");
        health.put("clientIdValid", clientIdValid);
        health.put("clientSecretValid", clientSecretValid);
        health.put("timestamp", System.currentTimeMillis());
        
        if (!clientIdValid) {
            health.put("error", "Google Client ID is not properly configured");
        }
        if (!clientSecretValid) {
            health.put("error", "Google Client Secret is not properly configured");
        }
        
        return ResponseEntity.ok(health);
    }
}