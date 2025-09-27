package com.example.online_quiz_system.controller;
import com.example.online_quiz_system.entity.RefreshToken;
import com.example.online_quiz_system.entity.Role;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.repository.LoginSessionRepository;
import com.example.online_quiz_system.repository.RefreshTokenRepository;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock private UserService userService;
    @Mock private VerificationService verificationService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private CustomUserDetailsService customUserDetailsService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private LoginSessionRepository loginSessionRepository;
    @Mock private RedisService redisService;
    @Mock
    private PasswordResetService passwordResetService;

    @Test
    @DisplayName("register: returns 400 with validation errors when fields invalid")
    void register_validationErrors() throws Exception {
        String body = "{}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("register: success returns message and calls service")
    void register_success() throws Exception {
        Mockito.doNothing().when(userService).registerUser(anyString(), anyString(), any(), any());
        String body = "{\"email\":\"u@example.com\",\"password\":\"Aa1!aaaa\"}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
        Mockito.verify(userService).registerUser(eq("u@example.com"), eq("Aa1!aaaa"), isNull(), isNull());
    }

    @Test
    @DisplayName("login: unverified user is rejected with 401")
    void login_unverified() throws Exception {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("u@example.com").password("x").authorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "x", principal.getAuthorities());
        Mockito.when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        User user = User.builder().id(1L).email("u@example.com").passwordHash("hash").role(Role.USER).isVerified(false).build();
        Mockito.when(userRepository.findByEmail("u@example.com")).thenReturn(Optional.of(user));

        String body = "{\"email\":\"u@example.com\",\"password\":\"secret\"}";
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(containsString("chưa được xác thực")));
    }

    @Test
    @DisplayName("login: success returns access and refresh tokens")
    void login_success() throws Exception {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("v@example.com").password("x").authorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "x", principal.getAuthorities());
        Mockito.when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        User user = User.builder().id(2L).email("v@example.com").passwordHash("hash").role(Role.USER).isVerified(true).build();
        Mockito.when(userRepository.findByEmail("v@example.com")).thenReturn(Optional.of(user));
        Mockito.when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("access.token");
        Mockito.when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("refresh.token");

        String body = "{\"email\":\"v@example.com\",\"password\":\"secret\"}";
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.token"))
                .andExpect(jsonPath("$.email").value("v@example.com"));
    }

    @Test
    @DisplayName("refresh: rotates refresh token and returns new pair")
    void refresh_rotation_success() throws Exception {
        String oldRefresh = "old.refresh";
        Mockito.when(jwtService.validateToken(oldRefresh)).thenReturn(true);
        Mockito.when(jwtService.extractUsername(oldRefresh)).thenReturn("v@example.com");
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("v@example.com").password("x").authorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))).build();
        Mockito.when(customUserDetailsService.loadUserByUsername("v@example.com")).thenReturn(principal);
        RefreshToken stored = RefreshToken.builder()
                .id(10L)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(5))
                .build();
        Mockito.when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));
        Mockito.when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("new.access");
        Mockito.when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("new.refresh");
        User user = User.builder().id(2L).email("v@example.com").passwordHash("hash").role(Role.USER).isVerified(true).build();
        Mockito.when(userRepository.findByEmail("v@example.com")).thenReturn(Optional.of(user));

        String body = "{\"refreshToken\":\"" + oldRefresh + "\"}";
        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access"))
                .andExpect(jsonPath("$.refreshToken").value("new.refresh"));
    }

    @Test
    @DisplayName("refresh: reusing revoked token returns 401")
    void refresh_reuse_revoked_fails() throws Exception {
        String oldRefresh = "old.refresh";
        Mockito.when(jwtService.validateToken(oldRefresh)).thenReturn(true);
        Mockito.when(jwtService.extractUsername(oldRefresh)).thenReturn("v@example.com");
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("v@example.com").password("x").authorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))).build();
        Mockito.when(customUserDetailsService.loadUserByUsername("v@example.com")).thenReturn(principal);
        RefreshToken stored = RefreshToken.builder()
                .id(11L)
                .revoked(true)
                .expiresAt(LocalDateTime.now().plusDays(5))
                .build();
        Mockito.when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        String body = "{\"refreshToken\":\"" + oldRefresh + "\"}";
        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }
}


