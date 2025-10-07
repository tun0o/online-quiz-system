package com.example.online_quiz_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles OAuth2 authentication failures by redirecting to error page with appropriate error messages.
 * Improved to handle invalid_request and other OAuth2 errors properly.
 */
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // Error codes
    public static final String ERROR_OAUTH2_FAILED = "OAUTH2_FAILED";
    public static final String ERROR_ACCESS_DENIED = "ACCESS_DENIED";
    public static final String ERROR_PROVIDER_ERROR = "PROVIDER_ERROR";
    public static final String ERROR_INVALID_REQUEST = "INVALID_REQUEST";
    public static final String ERROR_INVALID_CLIENT = "INVALID_CLIENT";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        logger.error("OAuth2 authentication failure: {}", exception.getMessage(), exception);

        String errorCode = determineErrorCode(exception);
        String errorMessage = getErrorMessage(exception);
        String detailedError = getDetailedError(exception);

        // URL encode the error message to handle special characters properly
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());
        String encodedDetailedError = URLEncoder.encode(detailedError, StandardCharsets.UTF_8.toString());

        String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/error")
                .queryParam("error", errorCode)
                .queryParam("message", encodedMessage)
                .queryParam("details", encodedDetailedError)
                .build().toUriString();

        logger.warn("Redirecting to OAuth2 error page: {} - {} - {}", errorCode, errorMessage, detailedError);
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }

    /**
     * Determines appropriate error code based on exception type and OAuth2 error.
     */
    private String determineErrorCode(AuthenticationException exception) {
        String exceptionName = exception.getClass().getSimpleName();
        
        // Handle OAuth2 specific errors
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            OAuth2Error error = oauth2Exception.getError();
            String errorCode = error.getErrorCode();
            
            return switch (errorCode) {
                case "invalid_request" -> ERROR_INVALID_REQUEST;
                case "invalid_client" -> ERROR_INVALID_CLIENT;
                case "access_denied" -> ERROR_ACCESS_DENIED;
                default -> ERROR_OAUTH2_FAILED;
            };
        }
        
        return switch (exceptionName) {
            case "AccessDeniedException" -> ERROR_ACCESS_DENIED;
            case "OAuth2AuthenticationException" -> ERROR_OAUTH2_FAILED;
            default -> ERROR_PROVIDER_ERROR;
        };
    }

    /**
     * Gets user-friendly error message based on exception.
     * Returns English messages to avoid encoding issues.
     */
    private String getErrorMessage(AuthenticationException exception) {
        String message = exception.getMessage();
        
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            OAuth2Error error = oauth2Exception.getError();
            String errorCode = error.getErrorCode();
            
            return switch (errorCode) {
                case "invalid_request" -> "Invalid OAuth2 request. Please check your configuration.";
                case "invalid_client" -> "Invalid OAuth2 client. Please check your Client ID and Secret.";
                case "access_denied" -> "Access denied. Please grant necessary permissions.";
                case "unauthorized_client" -> "Application not authorized. Please contact support.";
                case "unsupported_response_type" -> "Unsupported response type. Please try again.";
                case "invalid_scope" -> "Invalid scope. Please try again.";
                case "server_error" -> "Server error. Please try again later.";
                case "temporarily_unavailable" -> "Service temporarily unavailable. Please try again later.";
                default -> "OAuth2 authentication failed. Please try again.";
            };
        }
        
        if (message != null) {
            // Handle specific OAuth2 error messages - using English to avoid encoding issues
            if (message.contains("access_denied")) {
                return "Access denied. Please try again and grant necessary permissions.";
            }
            if (message.contains("invalid_request")) {
                return "Invalid request. Please check your OAuth2 configuration.";
            }
            if (message.contains("unauthorized_client")) {
                return "Application not authorized. Please contact support.";
            }
            if (message.contains("unsupported_response_type")) {
                return "Unsupported response type. Please try again.";
            }
            if (message.contains("invalid_scope")) {
                return "Invalid scope. Please try again.";
            }
            if (message.contains("server_error")) {
                return "Server error. Please try again later.";
            }
            if (message.contains("temporarily_unavailable")) {
                return "Service temporarily unavailable. Please try again later.";
            }
        }
        
        // Default error message
        return "Login failed. Please try again or use another login method.";
    }

    /**
     * Gets detailed error information for debugging.
     */
    private String getDetailedError(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            OAuth2Error error = oauth2Exception.getError();
            
            StringBuilder details = new StringBuilder();
            details.append("Error Code: ").append(error.getErrorCode());
            details.append(", Description: ").append(error.getDescription());
            if (error.getUri() != null) {
                details.append(", URI: ").append(error.getUri());
            }
            
            return details.toString();
        }
        
        return "Exception: " + exception.getClass().getSimpleName() + 
               ", Message: " + exception.getMessage();
    }
}