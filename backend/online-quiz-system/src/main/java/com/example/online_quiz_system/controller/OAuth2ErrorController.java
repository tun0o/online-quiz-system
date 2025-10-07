package com.example.online_quiz_system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to handle OAuth2 error responses and provide detailed error information.
 */
@RestController
@RequestMapping("/api/oauth2")
public class OAuth2ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ErrorController.class);

    /**
     * Handles OAuth2 error responses from frontend.
     * 
     * @param error Error code from OAuth2 provider
     * @param message Error message
     * @param details Detailed error information
     * @return Error response with details
     */
    @GetMapping("/error")
    public ResponseEntity<Map<String, Object>> handleOAuth2Error(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String details) {
        
        logger.error("OAuth2 Error - Code: {}, Message: {}, Details: {}", error, message, details);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", error != null ? error : "UNKNOWN_ERROR");
        errorResponse.put("message", message != null ? message : "OAuth2 authentication failed");
        errorResponse.put("details", details != null ? details : "No additional details available");
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        // Add specific error handling
        if ("INVALID_REQUEST".equals(error)) {
            errorResponse.put("suggestion", "Please check your OAuth2 configuration and redirect URI");
        } else if ("INVALID_CLIENT".equals(error)) {
            errorResponse.put("suggestion", "Please verify your Client ID and Client Secret");
        } else if ("ACCESS_DENIED".equals(error)) {
            errorResponse.put("suggestion", "Please grant necessary permissions and try again");
        }
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Health check endpoint for OAuth2 configuration.
     * 
     * @return OAuth2 configuration status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getOAuth2Status() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "OAuth2 configuration is active");
        status.put("timestamp", System.currentTimeMillis());
        status.put("endpoints", Map.of(
            "authorization", "/oauth2/authorization/google",
            "callback", "/login/oauth2/code/google",
            "error", "/api/oauth2/error"
        ));
        
        return ResponseEntity.ok(status);
    }
}
