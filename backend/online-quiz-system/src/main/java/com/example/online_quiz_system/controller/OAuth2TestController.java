package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.config.OAuth2Configuration;
import com.example.online_quiz_system.util.OAuth2Validation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for OAuth2 functionality.
 * Provides endpoints for testing OAuth2 configuration and validation.
 * Should be disabled in production.
 */
@RestController
@RequestMapping("/api/oauth2/test")
public class OAuth2TestController {

    @Autowired
    private OAuth2Configuration oauth2Config;

    /**
     * Tests OAuth2 configuration validity.
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> testConfiguration() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean isValid = oauth2Config.isConfigurationValid();
            result.put("valid", isValid);
            result.put("frontendUrl", oauth2Config.getFrontendUrl());
            result.put("googleClientId", oauth2Config.getGoogleClientId() != null ? "***" : null);
            result.put("facebookClientId", oauth2Config.getFacebookClientId() != null ? "***" : null);
            
            if (isValid) {
                result.put("message", "OAuth2 configuration is valid");
            } else {
                result.put("message", "OAuth2 configuration is invalid");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Tests OAuth2 validation utilities.
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> testValidation(@RequestBody Map<String, String> testData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String provider = testData.get("provider");
            String providerId = testData.get("providerId");
            String email = testData.get("email");
            String name = testData.get("name");
            
            OAuth2Validation.validateOAuth2UserData(provider, providerId, email, name);
            
            result.put("valid", true);
            result.put("message", "Validation passed");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Tests OAuth2 provider support.
     */
    @GetMapping("/providers/{provider}")
    public ResponseEntity<Map<String, Object>> testProviderSupport(@PathVariable String provider) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            OAuth2Validation.validateProvider(provider);
            result.put("supported", true);
            result.put("message", "Provider is supported: " + provider);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("supported", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Tests OAuth2 URL generation.
     */
    @GetMapping("/urls")
    public ResponseEntity<Map<String, Object>> testUrls(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Dynamically construct the base URL from the request
            String backendBaseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

            result.put("backendBaseUrl", backendBaseUrl);
            result.put("googleAuthUrl", oauth2Config.getAuthorizationUrl(backendBaseUrl, "google"));
            result.put("facebookAuthUrl", oauth2Config.getAuthorizationUrl(backendBaseUrl, "facebook"));
            result.put("successUrl", oauth2Config.getSuccessUrl());
            result.put("errorUrl", oauth2Config.getErrorUrl());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Tests email validation.
     */
    @PostMapping("/validate-email")
    public ResponseEntity<Map<String, Object>> testEmailValidation(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String email = request.get("email");
            OAuth2Validation.validateEmail(email);
            
            result.put("valid", true);
            result.put("message", "Email is valid: " + email);
            result.put("trustedDomain", OAuth2Validation.isTrustedEmailDomain(email));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Tests name validation.
     */
    @PostMapping("/validate-name")
    public ResponseEntity<Map<String, Object>> testNameValidation(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String name = request.get("name");
            OAuth2Validation.validateName(name);
            
            result.put("valid", true);
            result.put("message", "Name is valid: " + name);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Tests input sanitization.
     */
    @PostMapping("/sanitize")
    public ResponseEntity<Map<String, Object>> testSanitization(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String input = request.get("input");
            String sanitized = OAuth2Validation.sanitizeInput(input);
            
            result.put("original", input);
            result.put("sanitized", sanitized);
            result.put("changed", !input.equals(sanitized));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
