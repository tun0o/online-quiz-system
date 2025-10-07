package com.example.online_quiz_system.config;

import com.example.online_quiz_system.service.CustomUserDetailsService;
import com.example.online_quiz_system.service.JwtService;
import com.example.online_quiz_system.service.GoogleOAuth2UserService;
import com.example.online_quiz_system.service.MultiProviderOAuth2UserService;
import com.example.online_quiz_system.service.OAuth2AuthenticationSuccessHandler;
import com.example.online_quiz_system.service.OAuth2AuthenticationFailureHandler;
import com.example.online_quiz_system.security.InMemoryOAuth2AuthorizationRequestRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    // Note: we intentionally do NOT inject CustomOAuth2UserService / OAuth2AuthenticationSuccessHandler
    // via the constructor to avoid circular dependencies. They are injected into the filterChain method instead.
    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * FIXED: Remove AuthenticationProvider to fix Spring Security warning
     * Use UserDetailsService directly instead
     */
    // @Bean
    // public AuthenticationProvider authenticationProvider() {
    //     DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    //     provider.setUserDetailsService(userDetailsService);
    //     provider.setPasswordEncoder(passwordEncoder());
    //     return provider;
    // }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    // Đổi tên method để tránh xung đột với @Component
    @Bean
    public InMemoryOAuth2AuthorizationRequestRepository oauth2AuthorizationRequestRepository() {
        return new InMemoryOAuth2AuthorizationRequestRepository();
    }

    /**
     * Security filter chain. Inject OAuth2-related beans as method parameters to break circular refs.
     * FIXED: Remove authenticationProvider() call to fix Spring Security warning
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           MultiProviderOAuth2UserService multiProviderOAuth2UserService,
                                           OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler,
                                           OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                // FIXED: Removed problematic sessionManagement configuration
                // Session management is handled automatically by Spring Security 6
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/oauth2/**").permitAll() // OAuth2 error handling và debug endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasRole("USER")
                        .requestMatchers("/api/quiz-submissions/public").permitAll()
                        .requestMatchers("/api/challenges/leaderboard").permitAll() // Cho phép xem bảng xếp hạng công khai
                        .requestMatchers("/api/quiz-submissions/**", "/api/challenges/**", "/api/quizzes/**").authenticated() // Các API còn lại cần đăng nhập
                        .requestMatchers("/oauth2/**", "/login/**", "/oauth2/authorization/**", "/login/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                // FIXED: Remove authenticationProvider() call
                // .authenticationProvider(authenticationProvider())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authz -> authz
                                // Sử dụng đường dẫn mặc định của Spring Security để tránh xung đột
                                .baseUri("/oauth2/authorization")
                                .authorizationRequestRepository(oauth2AuthorizationRequestRepository())
                        )
                        .redirectionEndpoint(redirection -> redirection
                                // Đảm bảo redirect URI đúng format
                                .baseUri("/login/oauth2/code/*")
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(multiProviderOAuth2UserService)
                                // Disable OIDC user service to avoid null 'id' issues
                                // .oidcUserService(customOidcUserService)
                        )
                        .successHandler(oauth2AuthenticationSuccessHandler)
                        .failureHandler(oauth2AuthenticationFailureHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // When allowCredentials(true) is set, use specific allowed origins instead of "*". Adjust as needed.
        config.setAllowedOriginPatterns(List.of("http://localhost:3000", "http://localhost:5173")); // Đảm bảo port 3000 có ở đây
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}