package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.User;
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

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

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
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String registrationId = token.getAuthorizedClientRegistrationId(); // google | facebook
        OAuth2User oAuth2User = token.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Chuẩn hóa thông tin user từ provider
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // Kiểm tra email có tồn tại không
        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/error")
                    .queryParam("error", "EMAIL_NOT_PROVIDED")
                    .queryParam("message", "Email không được cung cấp từ nhà cung cấp. Vui lòng cấp quyền email.")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
            return;
        }

        try {
            // Lưu hoặc cập nhật user trong DB
            User user = userService.processOAuthPostLogin(registrationId, userInfo);

            // Load UserDetails để sinh JWT
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
            String jwt = jwtService.generateAccessToken(userDetails);

            // Redirect về frontend kèm token
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/success")
                    .queryParam("token", jwt)
                    .queryParam("userId", user.getId())
                    .queryParam("email", user.getEmail())
                    .build().toUriString();

            logger.info("OAuth2 login success for {} (provider {}) -> redirect {}",
                    user.getEmail(), registrationId, targetUrl);

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            logger.error("OAuth2 authentication error: {}", e.getMessage(), e);
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/error")
                    .queryParam("error", "AUTHENTICATION_FAILED")
                    .queryParam("message", "Xác thực thất bại. Vui lòng thử lại.")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}