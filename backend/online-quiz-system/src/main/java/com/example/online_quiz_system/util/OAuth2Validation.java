package com.example.online_quiz_system.util;

import com.example.online_quiz_system.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Utility class for OAuth2 data validation.
 * Provides comprehensive validation for OAuth2 user data and configuration.
 */
public class OAuth2Validation {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Name validation pattern (allows international characters)
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[\\p{L}\\p{M}\\p{Zs}\\p{Pd}']{1,50}$"
    );

    // Provider ID validation pattern
    private static final Pattern PROVIDER_ID_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]{1,100}$"
    );

    // Supported providers
    private static final String[] SUPPORTED_PROVIDERS = {"google", "facebook"};

    /**
     * Validates OAuth2 provider.
     * 
     * @param provider The provider name
     * @throws BusinessException if provider is invalid
     */
    public static void validateProvider(String provider) {
        if (!StringUtils.hasText(provider)) {
            throw new BusinessException("Provider không được để trống");
        }

        String normalizedProvider = provider.toLowerCase().trim();
        for (String supportedProvider : SUPPORTED_PROVIDERS) {
            if (supportedProvider.equals(normalizedProvider)) {
                return;
            }
        }

        throw new BusinessException("Provider không được hỗ trợ: " + provider);
    }

    /**
     * Validates email address.
     * 
     * @param email The email address
     * @throws BusinessException if email is invalid
     */
    public static void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException("Email không được để trống");
        }

        String trimmedEmail = email.trim();
        if (trimmedEmail.length() > 254) {
            throw new BusinessException("Email quá dài (tối đa 254 ký tự)");
        }

        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new BusinessException("Email không hợp lệ: " + email);
        }
    }

    /**
     * Validates user name.
     * 
     * @param name The user name
     * @throws BusinessException if name is invalid
     */
    public static void validateName(String name) {
        if (name == null) {
            return; // Name is optional
        }

        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            return; // Empty name is allowed
        }

        if (trimmedName.length() > 50) {
            throw new BusinessException("Tên quá dài (tối đa 50 ký tự)");
        }

        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            throw new BusinessException("Tên chứa ký tự không hợp lệ: " + name);
        }
    }

    /**
     * Validates provider ID.
     * 
     * @param providerId The provider ID
     * @throws BusinessException if provider ID is invalid
     */
    public static void validateProviderId(String providerId) {
        if (!StringUtils.hasText(providerId)) {
            throw new BusinessException("Provider ID không được để trống");
        }

        String trimmedId = providerId.trim();
        if (trimmedId.length() > 100) {
            throw new BusinessException("Provider ID quá dài (tối đa 100 ký tự)");
        }

        if (!PROVIDER_ID_PATTERN.matcher(trimmedId).matches()) {
            throw new BusinessException("Provider ID chứa ký tự không hợp lệ: " + providerId);
        }
    }

    /**
     * Validates OAuth2 user data comprehensively.
     * 
     * @param provider The OAuth2 provider
     * @param providerId The provider ID
     * @param email The user email
     * @param name The user name
     * @throws BusinessException if any validation fails
     */
    public static void validateOAuth2UserData(String provider, String providerId, String email, String name) {
        validateProvider(provider);
        validateProviderId(providerId);
        
        if (StringUtils.hasText(email)) {
            validateEmail(email);
        }
        
        if (StringUtils.hasText(name)) {
            validateName(name);
        }
    }

    /**
     * Validates OAuth2 configuration.
     * 
     * @param clientId The OAuth2 client ID
     * @param clientSecret The OAuth2 client secret
     * @param redirectUri The OAuth2 redirect URI
     * @throws BusinessException if configuration is invalid
     */
    public static void validateOAuth2Configuration(String clientId, String clientSecret, String redirectUri) {
        if (!StringUtils.hasText(clientId)) {
            throw new BusinessException("OAuth2 Client ID không được để trống");
        }

        if (!StringUtils.hasText(clientSecret)) {
            throw new BusinessException("OAuth2 Client Secret không được để trống");
        }

        if (!StringUtils.hasText(redirectUri)) {
            throw new BusinessException("OAuth2 Redirect URI không được để trống");
        }

        if (clientId.length() < 10 || clientId.length() > 200) {
            throw new BusinessException("OAuth2 Client ID có độ dài không hợp lệ");
        }

        if (clientSecret.length() < 10 || clientSecret.length() > 200) {
            throw new BusinessException("OAuth2 Client Secret có độ dài không hợp lệ");
        }

        if (!redirectUri.startsWith("http://") && !redirectUri.startsWith("https://")) {
            throw new BusinessException("OAuth2 Redirect URI phải bắt đầu với http:// hoặc https://");
        }
    }

    /**
     * Sanitizes user input for security.
     * 
     * @param input The input string
     * @return Sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        return input.trim()
                .replaceAll("[<>\"'&]", "") // Remove potentially dangerous characters
                .replaceAll("\\s+", " "); // Normalize whitespace
    }

    /**
     * Validates OAuth2 scopes.
     * 
     * @param scopes The OAuth2 scopes
     * @throws BusinessException if scopes are invalid
     */
    public static void validateScopes(String scopes) {
        if (!StringUtils.hasText(scopes)) {
            throw new BusinessException("OAuth2 scopes không được để trống");
        }

        String[] scopeArray = scopes.split(",");
        for (String scope : scopeArray) {
            String trimmedScope = scope.trim();
            if (trimmedScope.isEmpty()) {
                throw new BusinessException("OAuth2 scope không được để trống");
            }
            if (trimmedScope.length() > 50) {
                throw new BusinessException("OAuth2 scope quá dài: " + trimmedScope);
            }
        }
    }

    /**
     * Checks if email is from a trusted domain.
     * 
     * @param email The email address
     * @return true if email is from trusted domain
     */
    public static boolean isTrustedEmailDomain(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        String[] trustedDomains = {
            "gmail.com", "googlemail.com", "facebook.com", 
            "outlook.com", "hotmail.com", "yahoo.com"
        };

        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        for (String trustedDomain : trustedDomains) {
            if (domain.equals(trustedDomain)) {
                return true;
            }
        }

        return false;
    }
}

