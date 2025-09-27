package com.example.online_quiz_system.config;

import com.example.online_quiz_system.service.CustomUserDetailsService;
import com.example.online_quiz_system.service.JwtService;
import com.example.online_quiz_system.service.RedisService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisService redisService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService, RedisService redisService) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtService.validateToken(jwt)) {
                // Do not authenticate using refresh tokens
                try {
                    // Use JwtService helper to extract 'typ' claim safely
                    String typ = jwtService.extractClaim(jwt, claims -> claims.get("typ", String.class));
                    if ("refresh".equalsIgnoreCase(typ)) {
                        // Skip authenticating refresh tokens
                        filterChain.doFilter(request, response);
                        return;
                    }
                } catch (Exception ignore) {
                    // If extraction fails, continue (token validity already checked above)
                }
                // Check blacklist by JWT signature part
                try {
                    String[] parts = jwt.split("\\.");
                    if (parts.length == 3) {
                        String sig = parts[2];
                        if (redisService.isBlacklisted(sig)) {
                            filterChain.doFilter(request, response);
                            return;
                        }
                    }
                } catch (Exception ignored) {}
                String username = jwtService.getUsernameFromToken(jwt);
                if (username != null && !username.isBlank()) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
