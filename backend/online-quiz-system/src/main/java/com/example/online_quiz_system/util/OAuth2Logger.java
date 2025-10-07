package com.example.online_quiz_system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Utility class for structured OAuth2 logging.
 * Provides consistent logging format and context for OAuth2 operations.
 */
public class OAuth2Logger {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Logger.class);
    
    // MDC keys for structured logging
    private static final String MDC_PROVIDER = "oauth2.provider";
    private static final String MDC_USER_EMAIL = "oauth2.user.email";
    private static final String MDC_OPERATION = "oauth2.operation";
    private static final String MDC_ERROR_CODE = "oauth2.error.code";

    /**
     * Logs OAuth2 authentication start.
     */
    public static void logAuthStart(String provider, String userEmail) {
        setContext(provider, userEmail, "AUTH_START");
        logger.info("OAuth2 authentication started for provider: {}, user: {}", provider, userEmail);
        clearContext();
    }

    /**
     * Logs OAuth2 authentication success.
     */
    public static void logAuthSuccess(String provider, String userEmail, String userId) {
        setContext(provider, userEmail, "AUTH_SUCCESS");
        logger.info("OAuth2 authentication successful for provider: {}, user: {}, userId: {}", 
                   provider, userEmail, userId);
        clearContext();
    }

    /**
     * Logs OAuth2 authentication failure.
     */
    public static void logAuthFailure(String provider, String userEmail, String errorCode, String errorMessage) {
        setContext(provider, userEmail, "AUTH_FAILURE");
        MDC.put(MDC_ERROR_CODE, errorCode);
        logger.error("OAuth2 authentication failed for provider: {}, user: {}, error: {} - {}", 
                    provider, userEmail, errorCode, errorMessage);
        clearContext();
    }

    /**
     * Logs OAuth2 user creation.
     */
    public static void logUserCreation(String provider, String userEmail, String userId) {
        setContext(provider, userEmail, "USER_CREATION");
        logger.info("OAuth2 user created for provider: {}, user: {}, userId: {}", 
                   provider, userEmail, userId);
        clearContext();
    }

    /**
     * Logs OAuth2 user linking.
     */
    public static void logUserLinking(String provider, String userEmail, String userId) {
        setContext(provider, userEmail, "USER_LINKING");
        logger.info("OAuth2 account linked for provider: {}, user: {}, userId: {}", 
                   provider, userEmail, userId);
        clearContext();
    }

    /**
     * Logs OAuth2 user update.
     */
    public static void logUserUpdate(String provider, String userEmail, String userId) {
        setContext(provider, userEmail, "USER_UPDATE");
        logger.info("OAuth2 user updated for provider: {}, user: {}, userId: {}", 
                   provider, userEmail, userId);
        clearContext();
    }

    /**
     * Logs OAuth2 provider errors.
     */
    public static void logProviderError(String provider, String errorCode, String errorMessage, Exception exception) {
        setContext(provider, null, "PROVIDER_ERROR");
        MDC.put(MDC_ERROR_CODE, errorCode);
        logger.error("OAuth2 provider error for provider: {}, error: {} - {}", 
                    provider, errorCode, errorMessage, exception);
        clearContext();
    }

    /**
     * Logs OAuth2 configuration issues.
     */
    public static void logConfigurationError(String provider, String errorMessage) {
        setContext(provider, null, "CONFIG_ERROR");
        logger.error("OAuth2 configuration error for provider: {} - {}", provider, errorMessage);
        clearContext();
    }

    /**
     * Logs OAuth2 redirect operations.
     */
    public static void logRedirect(String provider, String userEmail, String redirectType, String url) {
        setContext(provider, userEmail, "REDIRECT");
        logger.info("OAuth2 redirect for provider: {}, user: {}, type: {}, url: {}", 
                   provider, userEmail, redirectType, url);
        clearContext();
    }

    /**
     * Logs OAuth2 token operations.
     */
    public static void logTokenOperation(String provider, String userEmail, String operation, boolean success) {
        setContext(provider, userEmail, "TOKEN_OPERATION");
        if (success) {
            logger.info("OAuth2 token operation successful for provider: {}, user: {}, operation: {}", 
                       provider, userEmail, operation);
        } else {
            logger.warn("OAuth2 token operation failed for provider: {}, user: {}, operation: {}", 
                       provider, userEmail, operation);
        }
        clearContext();
    }

    /**
     * Sets MDC context for structured logging.
     */
    private static void setContext(String provider, String userEmail, String operation) {
        if (provider != null) {
            MDC.put(MDC_PROVIDER, provider);
        }
        if (userEmail != null) {
            MDC.put(MDC_USER_EMAIL, userEmail);
        }
        if (operation != null) {
            MDC.put(MDC_OPERATION, operation);
        }
    }

    /**
     * Clears MDC context.
     */
    private static void clearContext() {
        MDC.remove(MDC_PROVIDER);
        MDC.remove(MDC_USER_EMAIL);
        MDC.remove(MDC_OPERATION);
        MDC.remove(MDC_ERROR_CODE);
    }

    /**
     * Logs OAuth2 metrics for monitoring.
     */
    public static void logMetrics(String provider, String metric, Object value) {
        setContext(provider, null, "METRICS");
        logger.info("OAuth2 metrics - provider: {}, metric: {}, value: {}", provider, metric, value);
        clearContext();
    }
}