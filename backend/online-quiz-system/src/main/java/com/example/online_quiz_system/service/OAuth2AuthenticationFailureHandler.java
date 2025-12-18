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

