package com.example.online_quiz_system.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

/**
 * Simple cookie-based repository to store OAuth2AuthorizationRequest.
 * Note: for production consider encryption of cookie value and HttpOnly/Secure flags.
 */
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // short-lived

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME)
                .flatMap(cookie -> deserialize(cookie.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        String serialized = serialize(authorizationRequest);
        Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        // cookie.setSecure(true); // enable in production with HTTPS
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return req;
    }

    private void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        // cookie.setSecure(true);
        response.addCookie(cookie);
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        for (Cookie c : request.getCookies()) {
            if (c.getName().equals(name)) return Optional.of(c);
        }
        return Optional.empty();
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            String json = objectMapper.writeValueAsString(authorizationRequest);
            return Base64.getUrlEncoder().encodeToString(json.getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize OAuth2AuthorizationRequest", e);
        }
    }

    private Optional<OAuth2AuthorizationRequest> deserialize(String value) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(value);
            String json = new String(decoded);
            OAuth2AuthorizationRequest req = objectMapper.readValue(json, OAuth2AuthorizationRequest.class);
            return Optional.of(req);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
