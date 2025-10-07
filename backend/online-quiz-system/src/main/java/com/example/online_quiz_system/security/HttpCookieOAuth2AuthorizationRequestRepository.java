package com.example.online_quiz_system.security;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Base64;
import java.util.Optional;

/**
 * Enhanced cookie-based repository to store OAuth2AuthorizationRequest.
 * Fixed Jackson deserialization issues with OAuth2AuthorizationResponseType using reflection.
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpCookieOAuth2AuthorizationRequestRepository.class);

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 300; // 5 phút

    private final ObjectMapper objectMapper;

    public HttpCookieOAuth2AuthorizationRequestRepository() {
        this.objectMapper = new ObjectMapper();
        
        // Cấu hình Jackson để xử lý OAuth2 objects
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        this.objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        // Đăng ký custom deserializer cho OAuth2AuthorizationResponseType
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OAuth2AuthorizationResponseType.class, new OAuth2AuthorizationResponseTypeDeserializer());
        this.objectMapper.registerModule(module);
        
        logger.info("HttpCookieOAuth2AuthorizationRequestRepository initialized with custom Jackson configuration");
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        logger.debug("Loading OAuth2 authorization request from cookie");
        
        Optional<OAuth2AuthorizationRequest> requestOpt = getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME)
                .flatMap(cookie -> {
                    logger.debug("Found OAuth2 cookie: {} with length: {}", cookie.getName(), cookie.getValue().length());
                    return deserialize(cookie.getValue());
                });
        
        if (requestOpt.isPresent()) {
            logger.debug("Successfully loaded OAuth2 authorization request");
            return requestOpt.get();
        } else {
            logger.warn("No OAuth2 authorization request found in cookies");
            return null;
        }
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Saving OAuth2 authorization request");
        
        if (authorizationRequest == null) {
            logger.debug("Authorization request is null, removing cookies");
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        
        try {
            String serialized = serialize(authorizationRequest);
            Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
            cookie.setSecure(false); // Set to true in production with HTTPS
            response.addCookie(cookie);
            
            logger.debug("OAuth2 authorization request saved to cookie successfully, length: {}", serialized.length());
        } catch (Exception e) {
            logger.error("Failed to save OAuth2 authorization request", e);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Removing OAuth2 authorization request");
        
        OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        
        if (req != null) {
            logger.debug("OAuth2 authorization request removed successfully");
        } else {
            logger.warn("No OAuth2 authorization request found to remove");
        }
        
        return req;
    }

    private void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        response.addCookie(cookie);
        
        logger.debug("OAuth2 authorization request cookies removed");
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            logger.debug("No cookies found in request");
            return Optional.empty();
        }
        
        for (Cookie c : request.getCookies()) {
            if (c.getName().equals(name)) {
                logger.debug("Found cookie: {} with value length: {}", c.getName(), c.getValue().length());
                return Optional.of(c);
            }
        }
        
        logger.debug("Cookie {} not found", name);
        return Optional.empty();
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            String json = objectMapper.writeValueAsString(authorizationRequest);
            String encoded = Base64.getUrlEncoder().encodeToString(json.getBytes());
            logger.debug("Serialized OAuth2 authorization request, JSON length: {}, encoded length: {}", 
                        json.length(), encoded.length());
            return encoded;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize OAuth2AuthorizationRequest", e);
            throw new RuntimeException("Failed to serialize OAuth2AuthorizationRequest", e);
        }
    }

    private Optional<OAuth2AuthorizationRequest> deserialize(String value) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(value);
            String json = new String(decoded);
            logger.debug("Deserializing OAuth2 authorization request, JSON: {}", json);
            
            OAuth2AuthorizationRequest req = objectMapper.readValue(json, OAuth2AuthorizationRequest.class);
            logger.debug("Deserialized OAuth2 authorization request successfully");
            return Optional.of(req);
        } catch (IOException e) {
            logger.error("Failed to deserialize OAuth2AuthorizationRequest", e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected error during deserialization", e);
            return Optional.empty();
        }
    }

    /**
     * Custom deserializer for OAuth2AuthorizationResponseType using reflection.
     * OAuth2AuthorizationResponseType is not a traditional enum, so we use reflection to access static fields.
     */
    private static class OAuth2AuthorizationResponseTypeDeserializer extends JsonDeserializer<OAuth2AuthorizationResponseType> {
        
        @Override
        public OAuth2AuthorizationResponseType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            logger.debug("Deserializing OAuth2AuthorizationResponseType from value: {}", value);
            
            try {
                // Lấy tất cả các fields static public của class
                Field[] fields = OAuth2AuthorizationResponseType.class.getDeclaredFields();
                
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers()) && 
                        field.getType().equals(OAuth2AuthorizationResponseType.class)) {
                        
                        OAuth2AuthorizationResponseType responseType = 
                            (OAuth2AuthorizationResponseType) field.get(null);
                        
                        if (responseType.getValue().equals(value)) {
                            logger.debug("Found matching OAuth2AuthorizationResponseType: {}", responseType);
                            return responseType;
                        }
                    }
                }
                
                logger.warn("No matching OAuth2AuthorizationResponseType found for value: {}", value);
                // Fallback to default value instead of throwing exception
                return OAuth2AuthorizationResponseType.CODE;
                
            } catch (IllegalAccessException e) {
                logger.error("Error accessing OAuth2AuthorizationResponseType fields", e);
                // Fallback to default value instead of throwing exception
                return OAuth2AuthorizationResponseType.CODE;
            } catch (Exception e) {
                logger.error("Unexpected error during OAuth2AuthorizationResponseType deserialization", e);
                // Fallback to default value instead of throwing exception
                return OAuth2AuthorizationResponseType.CODE;
            }
        }
    }
}