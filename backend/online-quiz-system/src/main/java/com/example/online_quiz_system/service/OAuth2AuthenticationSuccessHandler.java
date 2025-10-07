package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.UserProfile;
import com.example.online_quiz_system.repository.UserProfileRepository;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.util.OAuth2Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.Map;

/**
 * Handles successful OAuth2 authentication by processing user data,
 * generating JWT tokens, and redirecting to frontend with authentication data.
 */
@Component
@Transactional
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
    private final UserProfileRepository userProfileRepository;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(UserService userService,
                                              JwtService jwtService,
                                              CustomUserDetailsService customUserDetailsService,
                                              UserProfileRepository userProfileRepository) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional
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
            String providerId = oAuth2User.getName(); // Get provider ID from OAuth2User
            User user = userService.processOAuthPostLogin(registrationId, providerId, userInfo);
            
            // Generate JWT token
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
            String jwt = jwtService.generateAccessToken(userDetails);

            // Redirect to success page with token
            redirectToSuccess(request, response, jwt, user, authentication);

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
     * ADVANCED FIX: Enhanced UTF-8 encoding and fallback mechanisms for Vietnamese characters.
     */
    @Transactional
    private void redirectToSuccess(HttpServletRequest request, HttpServletResponse response, 
                                  String jwt, User user, Authentication authentication) throws IOException {
        try {
            // Lấy name từ UserProfile với fallback
            String userName = getUserNameFromProfile(user.getId());
            
            // ADVANCED FIX: Enhanced Unicode handling with multiple fallback strategies
            // FIXED: Don't pre-encode here, let buildSuccessUrl handle encoding
            String safeUserName = sanitizeUnicodeString(userName);
            String safeEmail = sanitizeUnicodeString(user.getEmail());
            
            // ADVANCED FIX: Extract provider from authentication first, then fallback to user
            String provider = extractProvider(authentication);
            if ("google".equals(provider)) {
                // Try to get more specific provider from user if available
                String userProvider = getProviderName(user);
                if (!"google".equals(userProvider)) {
                    provider = userProvider;
                }
            }
            
            // Set response encoding to UTF-8
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");
            
            // Build URL with proper encoding
            String targetUrl = buildSuccessUrl(jwt, user.getId(), safeEmail, safeUserName, provider);
            
            OAuth2Logger.logAuthSuccess(provider, user.getEmail(), user.getId().toString());
            OAuth2Logger.logRedirect(provider, user.getEmail(), "SUCCESS", targetUrl);

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            logger.error("Failed to redirect to success page for user: {}", user.getEmail(), e);
            // Fallback to error page
            handleError(request, response, ERROR_AUTHENTICATION_FAILED, 
                "Lỗi chuyển hướng. Vui lòng thử lại.");
        }
    }
    
    /**
     * Sanitizes Unicode string to prevent encoding issues.
     * FINAL FIX: Complete Unicode handling for Vietnamese characters.
     */
    private String sanitizeUnicodeString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        
        try {
            // FINAL FIX: Convert Vietnamese characters to ASCII-safe equivalents
            String sanitized = input
                // Vietnamese specific character replacements
                .replace("à", "a").replace("á", "a").replace("ạ", "a").replace("ả", "a").replace("ã", "a")
                .replace("â", "a").replace("ầ", "a").replace("ấ", "a").replace("ậ", "a").replace("ẩ", "a").replace("ẫ", "a")
                .replace("ă", "a").replace("ằ", "a").replace("ắ", "a").replace("ặ", "a").replace("ẳ", "a").replace("ẵ", "a")
                .replace("è", "e").replace("é", "e").replace("ẹ", "e").replace("ẻ", "e").replace("ẽ", "e")
                .replace("ê", "e").replace("ề", "e").replace("ế", "e").replace("ệ", "e").replace("ể", "e").replace("ễ", "e")
                .replace("ì", "i").replace("í", "i").replace("ị", "i").replace("ỉ", "i").replace("ĩ", "i")
                .replace("ò", "o").replace("ó", "o").replace("ọ", "o").replace("ỏ", "o").replace("õ", "o")
                .replace("ô", "o").replace("ồ", "o").replace("ố", "o").replace("ộ", "o").replace("ổ", "o").replace("ỗ", "o")
                .replace("ơ", "o").replace("ờ", "o").replace("ớ", "o").replace("ợ", "o").replace("ở", "o").replace("ỡ", "o")
                .replace("ù", "u").replace("ú", "u").replace("ụ", "u").replace("ủ", "u").replace("ũ", "u")
                .replace("ư", "u").replace("ừ", "u").replace("ứ", "u").replace("ự", "u").replace("ử", "u").replace("ữ", "u")
                .replace("ỳ", "y").replace("ý", "y").replace("ỵ", "y").replace("ỷ", "y").replace("ỹ", "y")
                .replace("đ", "d")
                // Uppercase versions
                .replace("À", "A").replace("Á", "A").replace("Ạ", "A").replace("Ả", "A").replace("Ã", "A")
                .replace("Â", "A").replace("Ầ", "A").replace("Ấ", "A").replace("Ậ", "A").replace("Ẩ", "A").replace("Ẫ", "A")
                .replace("Ă", "A").replace("Ằ", "A").replace("Ắ", "A").replace("Ặ", "A").replace("Ẳ", "A").replace("Ẵ", "A")
                .replace("È", "E").replace("É", "E").replace("Ẹ", "E").replace("Ẻ", "E").replace("Ẽ", "E")
                .replace("Ê", "E").replace("Ề", "E").replace("Ế", "E").replace("Ệ", "E").replace("Ể", "E").replace("Ễ", "E")
                .replace("Ì", "I").replace("Í", "I").replace("Ị", "I").replace("Ỉ", "I").replace("Ĩ", "I")
                .replace("Ò", "O").replace("Ó", "O").replace("Ọ", "O").replace("Ỏ", "O").replace("Õ", "O")
                .replace("Ô", "O").replace("Ồ", "O").replace("Ố", "O").replace("Ộ", "O").replace("Ổ", "O").replace("Ỗ", "O")
                .replace("Ơ", "O").replace("Ờ", "O").replace("Ớ", "O").replace("Ợ", "O").replace("Ở", "O").replace("Ỡ", "O")
                .replace("Ù", "U").replace("Ú", "U").replace("Ụ", "U").replace("Ủ", "U").replace("Ũ", "U")
                .replace("Ư", "U").replace("Ừ", "U").replace("Ứ", "U").replace("Ự", "U").replace("Ử", "U").replace("Ữ", "U")
                .replace("Ỳ", "Y").replace("Ý", "Y").replace("Ỵ", "Y").replace("Ỷ", "Y").replace("Ỹ", "Y")
                .replace("Đ", "D")
                // Remove any remaining non-ASCII characters
                .replaceAll("[^\\p{ASCII}]", "")
                .trim();
            
            // If string becomes empty after sanitization, return safe default
            if (sanitized.isEmpty()) {
                return "User";
            }
            
            return sanitized;
        } catch (Exception e) {
            logger.warn("Failed to sanitize string: {}", input, e);
            // Return safe default
            return "User";
        }
    }
    
    /**
     * Gets provider name with fallback.
     * FIXED: Avoid LazyInitializationException by using safe access.
     */
    private String getProviderName(User user) {
        try {
            // FIXED: Safe access to avoid LazyInitializationException
            // Use try-catch to handle lazy loading gracefully
            if (user.getOauth2Accounts() != null) {
                try {
                    // Try to access the collection safely
                    if (!user.getOauth2Accounts().isEmpty()) {
                        return user.getOauth2Accounts().iterator().next().getProvider();
                    }
                } catch (Exception lazyException) {
                    // If lazy loading fails, use default provider
                    logger.debug("Lazy loading failed for user: {}, using default provider", user.getEmail());
                }
            }
            return "google"; // Default fallback
        } catch (Exception e) {
            logger.warn("Failed to get provider name for user: {}, using default", user.getEmail(), e);
            return "google"; // Return google instead of unknown
        }
    }
    
    /**
     * ADVANCED FIX: Extract provider from Authentication object.
     * This method provides multiple strategies to determine the OAuth2 provider.
     */
    private String extractProvider(Authentication authentication) {
        try {
            // Cách 1: Từ OAuth2AuthenticationToken
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                String provider = oauthToken.getAuthorizedClientRegistrationId();
                logger.info("Extracted provider from OAuth2AuthenticationToken: {}", provider);
                return provider;
            }
            
            // Cách 2: Từ OAuth2User attributes
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                String provider = oauthUser.getAttribute("oauth2_provider");
                if (provider != null) {
                    logger.info("Extracted provider from OAuth2User attributes: {}", provider);
                    return provider;
                }
            }
            
            logger.warn("Could not determine OAuth2 provider, using 'google' as default");
            return "google"; // Default fallback
        } catch (Exception e) {
            logger.warn("Error extracting provider from authentication", e);
            return "google";
        }
    }
    
    /**
     * Builds success URL with proper encoding.
     * FIXED: Avoid double-encoding by not pre-encoding parameters.
     */
    private String buildSuccessUrl(String jwt, Long userId, String email, String name, String provider) {
        try {
            // FIXED: Don't pre-encode parameters, let UriComponentsBuilder handle encoding
            return UriComponentsBuilder.fromUriString(frontendUrl + OAUTH2_SUCCESS_ROUTE)
                    .queryParam("token", jwt)
                    .queryParam("userId", userId)
                    .queryParam("email", email)
                    .queryParam("name", name)
                    .queryParam("provider", provider)
                    .build()
                    .toUriString();
        } catch (Exception e) {
            logger.error("Failed to build success URL", e);
            // Fallback to simple URL with manual encoding
            try {
                String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
                String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
                return frontendUrl + OAUTH2_SUCCESS_ROUTE + 
                       "?token=" + jwt + 
                       "&userId=" + userId + 
                       "&email=" + encodedEmail + 
                       "&name=" + encodedName + 
                       "&provider=" + provider;
            } catch (Exception ex) {
                logger.error("Failed to build fallback URL", ex);
                return frontendUrl + OAUTH2_SUCCESS_ROUTE + "?error=url_build_failed";
            }
        }
    }

    /**
     * Gets user name from UserProfile.
     */
    @Transactional
    private String getUserNameFromProfile(Long userId) {
        try {
            return userProfileRepository.findByUserId(userId)
                    .map(UserProfile::getFullName)
                    .orElse("Chưa cập nhật");
        } catch (Exception e) {
            logger.warn("Failed to get user name from profile for user ID: {}", userId, e);
            return "Chưa cập nhật";
        }
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