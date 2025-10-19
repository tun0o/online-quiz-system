package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.util.OAuth2Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.online_quiz_system.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handles successful OAuth2 authentication by processing user data,
 * generating JWT tokens, and redirecting to frontend with authentication data.
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    
    // Error codes for frontend handling
    public static final String ERROR_EMAIL_NOT_PROVIDED = "EMAIL_NOT_PROVIDED";
    public static final String ERROR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    public static final String ERROR_PROVIDER_NOT_SUPPORTED = "PROVIDER_NOT_SUPPORTED";
    
    // Frontend routes
    private static final String OAUTH2_SUCCESS_ROUTE = "/oauth2/success";
    private static final String OAUTH2_ERROR_ROUTE = "/oauth2/error";

    private final UserService userService;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(UserService userService,
                                              JwtService jwtService,
                                              CustomUserDetailsService customUserDetailsService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String registrationId = null;
        OAuth2UserInfo userInfo = null;
        
        try {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            registrationId = token.getAuthorizedClientRegistrationId();
            OAuth2User oAuth2User = token.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            // Validate provider support
            if (!OAuth2UserInfoFactory.isProviderSupported(registrationId)) {
                handleError(request, response, ERROR_PROVIDER_NOT_SUPPORTED, 
                    "Provider không được hỗ trợ: " + registrationId);
                return;
            }

            // Extract and validate user info
            userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
            OAuth2Logger.logAuthStart(registrationId, userInfo.getEmail());
            
            if (!isValidUserInfo(userInfo)) {
                OAuth2Logger.logAuthFailure(registrationId, null, ERROR_EMAIL_NOT_PROVIDED, 
                    "Email không được cung cấp từ nhà cung cấp");
                handleError(request, response, ERROR_EMAIL_NOT_PROVIDED, 
                    "Email không được cung cấp từ nhà cung cấp. Vui lòng cấp quyền email.");
                return;
            }

            // Process user login/registration
            User user = userService.processOAuthPostLogin(registrationId, userInfo);
            
            // Generate JWT tokens
            UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(user.getEmail());
            String accessToken = jwtService.generateAccessToken(userPrincipal);
            String refreshToken = jwtService.generateRefreshToken(userPrincipal);

            // Redirect to success page with tokens and user info
            redirectToSuccess(request, response, accessToken, refreshToken, user, userPrincipal);

        } catch (BusinessException e) {
            OAuth2Logger.logAuthFailure(registrationId, userInfo != null ? userInfo.getEmail() : null, 
                ERROR_AUTHENTICATION_FAILED, e.getMessage());
            handleError(request, response, ERROR_AUTHENTICATION_FAILED, e.getMessage());
        } catch (Exception e) {
            OAuth2Logger.logAuthFailure(registrationId, userInfo != null ? userInfo.getEmail() : null, 
                ERROR_AUTHENTICATION_FAILED, e.getMessage());
            handleError(request, response, ERROR_AUTHENTICATION_FAILED, 
                "Xác thực thất bại. Vui lòng thử lại.");
        }
    }

    /**
     * Validates OAuth2UserInfo to ensure required fields are present.
     */
    private boolean isValidUserInfo(OAuth2UserInfo userInfo) {
        return userInfo != null 
            && userInfo.getEmail() != null 
            && !userInfo.getEmail().trim().isEmpty();
    }

    /**
     * Redirects to OAuth2 success page with authentication data.
     */
    private void redirectToSuccess(HttpServletRequest request, HttpServletResponse response, 
                                  String accessToken, String refreshToken, User user, UserPrincipal userPrincipal) throws IOException {
        
        // Serialize roles to JSON string
        String rolesJson = "[]";
        try {
            rolesJson = new ObjectMapper().writeValueAsString(userPrincipal.getAuthoritiesAsString());
        } catch (Exception e) {
            logger.error("Failed to serialize user roles to JSON", e);
        }

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + OAUTH2_SUCCESS_ROUTE)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("userId", user.getId())
                .queryParam("email", user.getEmail())
                .queryParam("name", URLEncoder.encode(user.getName(), StandardCharsets.UTF_8))
                .queryParam("provider", user.getProvider())
                .queryParam("roles", URLEncoder.encode(rolesJson, StandardCharsets.UTF_8))
                .queryParam("verified", String.valueOf(user.isVerified()))
                .build().toUriString();

        OAuth2Logger.logAuthSuccess(user.getProvider(), user.getEmail(), user.getId().toString());
        OAuth2Logger.logRedirect(user.getProvider(), user.getEmail(), "SUCCESS", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Handles OAuth2 errors by redirecting to error page with error details.
     */
    private void handleError(HttpServletRequest request, HttpServletResponse response, 
                           String errorCode, String message) throws IOException {
        String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + OAUTH2_ERROR_ROUTE)
                .queryParam("error", URLEncoder.encode(errorCode, StandardCharsets.UTF_8))
                .queryParam("message", URLEncoder.encode(message, StandardCharsets.UTF_8))
                .build().toUriString();

        logger.warn("OAuth2 error redirect: {} - {}", errorCode, message);
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}