# 🔐 OAuth2 Complete Implementation Guide

## 📋 **Table of Contents**
1. [OAuth2 Configuration](#oauth2-configuration)
2. [OAuth2 User Info Classes](#oauth2-user-info-classes)
3. [OAuth2 Service Layer](#oauth2-service-layer)
4. [OAuth2 Authentication Handlers](#oauth2-authentication-handlers)
5. [OAuth2 Security Configuration](#oauth2-security-configuration)
6. [OAuth2 Frontend Integration](#oauth2-frontend-integration)

---

## 🔧 **OAuth2 Configuration**

### **OAuth2Configuration.java:**
```java
package com.example.online_quiz_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

@Configuration
public class OAuth2Configuration {

    @Value("${oauth2.google.client-id}")
    private String googleClientId;

    @Value("${oauth2.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth2.facebook.client-id}")
    private String facebookClientId;

    @Value("${oauth2.facebook.client-secret}")
    private String facebookClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            googleClientRegistration(),
            facebookClientRegistration()
        );
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
            .clientId(googleClientId)
            .clientSecret(googleClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "profile", "email")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://www.googleapis.com/oauth2/v4/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .clientName("Google")
            .build();
    }

    private ClientRegistration facebookClientRegistration() {
        return ClientRegistration.withRegistrationId("facebook")
            .clientId(facebookClientId)
            .clientSecret(facebookClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("email", "public_profile")
            .authorizationUri("https://www.facebook.com/v18.0/dialog/oauth")
            .tokenUri("https://graph.facebook.com/v18.0/oauth/access_token")
            .userInfoUri("https://graph.facebook.com/v18.0/me?fields=id,name,email,picture")
            .userNameAttributeName("id")
            .clientName("Facebook")
            .build();
    }
}
```

### **Application Properties:**
```properties
# OAuth2 Configuration
oauth2.google.client-id=${GOOGLE_CLIENT_ID:your-google-client-id}
oauth2.google.client-secret=${GOOGLE_CLIENT_SECRET:your-google-client-secret}
oauth2.facebook.client-id=${FACEBOOK_CLIENT_ID:your-facebook-client-id}
oauth2.facebook.client-secret=${FACEBOOK_CLIENT_SECRET:your-facebook-client-secret}

# Frontend URL for OAuth2 redirects
app.frontend.url=${FRONTEND_URL:http://localhost:3000}
```

---

## 👤 **OAuth2 User Info Classes**

### **OAuth2UserInfo.java (Abstract Base Class):**
```java
package com.example.online_quiz_system.service;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getEmail();
    public abstract boolean isEmailVerified();
    
    public String getImageUrl() {
        Object pic = attributes.get("picture");
        return pic != null ? pic.toString() : null;
    }
}
```

### **GoogleOAuth2UserInfo.java:**
```java
package com.example.online_quiz_system.service;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public boolean isEmailVerified() {
        Object v = attributes.get("email_verified");
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String) return Boolean.parseBoolean((String) v);
        return false;
    }
}
```

### **FacebookOAuth2UserInfo.java:**
```java
package com.example.online_quiz_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class FacebookOAuth2UserInfo extends OAuth2UserInfo {
    private static final Logger logger = LoggerFactory.getLogger(FacebookOAuth2UserInfo.class);

    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public boolean isEmailVerified() {
        return getEmail() != null;
    }

    @Override
    public String getImageUrl() {
        try {
            if (attributes.containsKey("picture")) {
                Object pictureObj = attributes.get("picture");
                if (pictureObj instanceof Map) {
                    Map<?,?> pictureMap = (Map<?,?>) pictureObj;
                    if (pictureMap.containsKey("data")) {
                        Object dataObj = pictureMap.get("data");
                        if (dataObj instanceof Map) {
                            Map<?,?> dataMap = (Map<?,?>) dataObj;
                            if (dataMap.containsKey("url")) {
                                return dataMap.get("url").toString();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract Facebook profile picture: {}", e.getMessage());
        }
        return null;
    }
}
```

### **OAuth2UserInfoFactory.java:**
```java
package com.example.online_quiz_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Objects;

/**
 * Factory class for creating OAuth2UserInfo instances based on provider type.
 * Uses strategy pattern for better maintainability and extensibility.
 */
public class OAuth2UserInfoFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserInfoFactory.class);
    
    // Supported providers
    public static final String GOOGLE_PROVIDER = "google";
    public static final String FACEBOOK_PROVIDER = "facebook";
    
    /**
     * Creates appropriate OAuth2UserInfo instance based on registration ID.
     */
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        Objects.requireNonNull(registrationId, "Registration ID cannot be null");
        Objects.requireNonNull(attributes, "Attributes cannot be null");
        
        String normalizedProvider = registrationId.toLowerCase().trim();
        
        logger.debug("Creating OAuth2UserInfo for provider: {}", normalizedProvider);
        
        return switch (normalizedProvider) {
            case GOOGLE_PROVIDER -> new GoogleOAuth2UserInfo(attributes);
            case FACEBOOK_PROVIDER -> new FacebookOAuth2UserInfo(attributes);
            default -> {
                String errorMsg = String.format("OAuth2 provider '%s' is not supported. Supported providers: %s, %s", 
                    registrationId, GOOGLE_PROVIDER, FACEBOOK_PROVIDER);
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        };
    }
    
    /**
     * Checks if a provider is supported.
     */
    public static boolean isProviderSupported(String registrationId) {
        if (registrationId == null) return false;
        String normalized = registrationId.toLowerCase().trim();
        return GOOGLE_PROVIDER.equals(normalized) || FACEBOOK_PROVIDER.equals(normalized);
    }
}
```

---

## 🔧 **OAuth2 Service Layer**

### **OAuth2Service.java:**
```java
package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.AuthProvider;
import com.example.online_quiz_system.entity.Role;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class OAuth2Service extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = getEmailFromAttributes(provider, attributes);
        String providerId = oAuth2User.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getProvider().equals(AuthProvider.valueOf(provider.toUpperCase()))) {
                throw new OAuth2AuthenticationException("Email already registered with " + user.getProvider() + " provider");
            }
        } else {
            user = new User();
            user.setEmail(email);
            user.setProvider(AuthProvider.GOOGLE.name());
            user.setProviderId(providerId);
            user.setVerified(true);
            user.setRole(Role.USER);
            user = userRepository.save(user);
        }

        return UserPrincipal.create(user, attributes);
    }

    private String getEmailFromAttributes(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        } else if ("facebook".equals(provider)) {
            return (String) attributes.get("email");
        }
        throw new OAuth2AuthenticationException("Provider not supported: " + provider);
    }
}
```

---

## 🎯 **OAuth2 Authentication Handlers**

### **OAuth2AuthenticationSuccessHandler.java:**
```java
package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.User;
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
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
            
            // Generate JWT token
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
            String jwt = jwtService.generateAccessToken(userDetails);

            // Redirect to success page with token
            redirectToSuccess(request, response, jwt, user);

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
                                  String jwt, User user) throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + OAUTH2_SUCCESS_ROUTE)
                .queryParam("token", jwt)
                .queryParam("userId", user.getId())
                .queryParam("email", user.getEmail())
                .queryParam("provider", user.getProvider())
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
                .queryParam("error", errorCode)
                .queryParam("message", message)
                .build().toUriString();

        logger.warn("OAuth2 error redirect: {} - {}", errorCode, message);
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}
```

### **OAuth2AuthenticationFailureHandler.java:**
```java
package com.example.online_quiz_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles OAuth2 authentication failures by redirecting to error page with appropriate error messages.
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

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        logger.error("OAuth2 authentication failure: {}", exception.getMessage(), exception);

        String errorCode = determineErrorCode(exception);
        String errorMessage = getErrorMessage(exception);

        String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/error")
                .queryParam("error", errorCode)
                .queryParam("message", errorMessage)
                .build().toUriString();

        logger.warn("Redirecting to OAuth2 error page: {} - {}", errorCode, errorMessage);
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }

    /**
     * Determines appropriate error code based on exception type.
     */
    private String determineErrorCode(AuthenticationException exception) {
        String exceptionName = exception.getClass().getSimpleName();
        
        return switch (exceptionName) {
            case "AccessDeniedException" -> ERROR_ACCESS_DENIED;
            case "OAuth2AuthenticationException" -> ERROR_OAUTH2_FAILED;
            default -> ERROR_PROVIDER_ERROR;
        };
    }

    /**
     * Gets user-friendly error message based on exception.
     */
    private String getErrorMessage(AuthenticationException exception) {
        String message = exception.getMessage();
        
        if (message != null) {
            // Handle specific OAuth2 error messages
            if (message.contains("access_denied")) {
                return "Bạn đã từ chối cấp quyền truy cập. Vui lòng thử lại và cấp quyền cần thiết.";
            }
            if (message.contains("invalid_request")) {
                return "Yêu cầu không hợp lệ. Vui lòng thử lại.";
            }
            if (message.contains("unauthorized_client")) {
                return "Ứng dụng không được ủy quyền. Vui lòng liên hệ hỗ trợ.";
            }
            if (message.contains("unsupported_response_type")) {
                return "Loại phản hồi không được hỗ trợ. Vui lòng thử lại.";
            }
            if (message.contains("invalid_scope")) {
                return "Phạm vi quyền không hợp lệ. Vui lòng thử lại.";
            }
            if (message.contains("server_error")) {
                return "Lỗi máy chủ. Vui lòng thử lại sau.";
            }
            if (message.contains("temporarily_unavailable")) {
                return "Dịch vụ tạm thời không khả dụng. Vui lòng thử lại sau.";
            }
        }
        
        // Default error message
        return "Đăng nhập thất bại. Vui lòng thử lại hoặc sử dụng phương thức đăng nhập khác.";
    }
}
```

---

## 🔐 **OAuth2 Security Configuration**

### **SecurityConfig.java (OAuth2 part):**
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private OAuth2Service oAuth2Service;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/oauth2/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2Service)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## 🌐 **OAuth2 Frontend Integration**

### **Frontend OAuth2 URLs:**
```javascript
// OAuth2 Login URLs
const GOOGLE_LOGIN_URL = "http://localhost:8080/oauth2/authorization/google";
const FACEBOOK_LOGIN_URL = "http://localhost:8080/oauth2/authorization/facebook";

// OAuth2 Callback URLs
const OAUTH2_SUCCESS_URL = "http://localhost:3000/oauth2/success";
const OAUTH2_ERROR_URL = "http://localhost:3000/oauth2/error";
```

### **Frontend OAuth2 Success Handler:**
```javascript
// OAuth2 Success Page (React)
import { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';

function OAuth2Success() {
    const [searchParams] = useSearchParams();
    
    useEffect(() => {
        const token = searchParams.get('token');
        const userId = searchParams.get('userId');
        const email = searchParams.get('email');
        const provider = searchParams.get('provider');
        
        if (token) {
            // Store JWT token
            localStorage.setItem('token', token);
            localStorage.setItem('userId', userId);
            localStorage.setItem('email', email);
            localStorage.setItem('provider', provider);
            
            // Redirect to dashboard
            window.location.href = '/dashboard';
        }
    }, [searchParams]);
    
    return <div>Đăng nhập thành công! Đang chuyển hướng...</div>;
}
```

### **Frontend OAuth2 Error Handler:**
```javascript
// OAuth2 Error Page (React)
import { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';

function OAuth2Error() {
    const [searchParams] = useSearchParams();
    
    useEffect(() => {
        const error = searchParams.get('error');
        const message = searchParams.get('message');
        
        console.error('OAuth2 Error:', error, message);
        
        // Show error message to user
        alert(`Đăng nhập thất bại: ${message}`);
        
        // Redirect to login page
        setTimeout(() => {
            window.location.href = '/login';
        }, 3000);
    }, [searchParams]);
    
    return <div>Đăng nhập thất bại! Đang chuyển hướng về trang đăng nhập...</div>;
}
```

---

## 🚀 **OAuth2 Flow Summary**

### **1. User clicks OAuth2 login button:**
```
Frontend → /oauth2/authorization/google
Spring Security → Google OAuth2 authorization server
```

### **2. User authorizes application:**
```
Google → User consent screen
User → Grants permissions
Google → Redirects to /login/oauth2/code/google
```

### **3. Spring Security processes callback:**
```
OAuth2Service → Loads user info from Google
OAuth2AuthenticationSuccessHandler → Processes user data
UserService → Creates/updates user
JWT Service → Generates JWT token
```

### **4. Redirect to frontend:**
```
Success Handler → Redirects to /oauth2/success?token=...
Frontend → Stores JWT token
Frontend → Redirects to dashboard
```

---

## 📊 **OAuth2 Error Handling**

### **Error Codes:**
- **EMAIL_NOT_PROVIDED**: User didn't grant email permission
- **AUTHENTICATION_FAILED**: General authentication failure
- **PROVIDER_NOT_SUPPORTED**: Unsupported OAuth2 provider
- **OAUTH2_FAILED**: OAuth2 protocol error
- **ACCESS_DENIED**: User denied access
- **PROVIDER_ERROR**: Provider-specific error

### **Error Messages:**
- Vietnamese error messages for better UX
- Specific error handling for different OAuth2 error types
- User-friendly error descriptions
- Automatic redirect to login page

---

## ✅ **OAuth2 Implementation Checklist**

### **Backend Setup:**
- [x] OAuth2 configuration with Google & Facebook
- [x] OAuth2UserInfo classes for each provider
- [x] OAuth2Service for user processing
- [x] Success/Failure handlers
- [x] Security configuration
- [x] JWT token generation

### **Frontend Setup:**
- [x] OAuth2 login buttons
- [x] Success page for token handling
- [x] Error page for error handling
- [x] JWT token storage
- [x] Automatic redirects

### **Testing:**
- [x] Google OAuth2 login flow
- [x] Facebook OAuth2 login flow
- [x] Error handling scenarios
- [x] JWT token validation
- [x] User data persistence

**OAuth2 implementation hoàn chỉnh với Google & Facebook! 🚀**
