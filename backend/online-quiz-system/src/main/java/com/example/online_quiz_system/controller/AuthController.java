package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.JwtResponseDTO;
import com.example.online_quiz_system.dto.LoginDTO;
import com.example.online_quiz_system.dto.RegisterDto;
import com.example.online_quiz_system.dto.ChangePasswordDTO;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.repository.RefreshTokenRepository;
import com.example.online_quiz_system.repository.LoginSessionRepository;
import com.example.online_quiz_system.entity.RefreshToken;
import com.example.online_quiz_system.entity.LoginSession;
import com.example.online_quiz_system.service.RedisService;
import com.example.online_quiz_system.util.HashUtils;
import com.example.online_quiz_system.service.UserService;
import com.example.online_quiz_system.service.VerificationService;
import com.example.online_quiz_system.service.JwtService;
import com.example.online_quiz_system.service.CustomUserDetailsService;
import com.example.online_quiz_system.service.PasswordResetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.frontend.origin}", maxAge = 3600)
@Validated
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final VerificationService verificationService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final RedisService redisService;
    private final PasswordResetService passwordResetService;
    private final com.example.online_quiz_system.service.AuditLogService auditLogService;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenExpiryMs;

    public AuthController(UserService userService,
                          VerificationService verificationService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          CustomUserDetailsService customUserDetailsService,
                          RefreshTokenRepository refreshTokenRepository,
                          LoginSessionRepository loginSessionRepository,
                          RedisService redisService,
                          PasswordResetService passwordResetService,
                          com.example.online_quiz_system.service.AuditLogService auditLogService) {
        this.userService = userService;
        this.verificationService = verificationService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.customUserDetailsService = customUserDetailsService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginSessionRepository = loginSessionRepository;
        this.redisService = redisService;
        this.passwordResetService = passwordResetService;
        this.auditLogService = auditLogService;
    }

    // helper to build refresh cookie respecting request security (useful for localhost vs https deploy)
    private ResponseCookie buildRefreshCookie(String token, HttpServletRequest request, long maxAgeSec) {
        boolean isSecure = request.isSecure();
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (!isSecure && forwardedProto != null && forwardedProto.equalsIgnoreCase("https")) {
            isSecure = true;
        }
        // If running behind a proxy which sets X-Forwarded-Proto, use that

        // For cross-origin cookie sent from frontend (different port), modern browsers require SameSite=None and Secure=true.
        // For local development over HTTP we fallback to SameSite=Lax and secure=false to allow testing on localhost.
        String sameSite = isSecure ? "None" : "Lax";

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(maxAgeSec);
        // set SameSite via builder
        builder.sameSite(sameSite);
        return builder.build();
    }

    // ---------------- Register ----------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        try {
            userService.registerUser(
                    registerDto.getEmail(),
                    registerDto.getPassword(),
                    registerDto.getGrade(),
                    registerDto.getGoal()
            );
            return ResponseEntity.ok().body(Map.of("message", "Đăng ký thành công. Vui lòng kiểm tra email để xác thực."));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Register error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi đăng ký"));
        }
    }

    // ---------------- Login (create access + refresh token) ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        try {
            // Simple rate limit: 5 attempts per minute per IP+device
            String userAgent = request.getHeader("User-Agent");
            String deviceFingerprint = request.getHeader("X-Device-Fingerprint");
            if (deviceFingerprint == null || deviceFingerprint.isBlank()) {
                deviceFingerprint = HashUtils.sha256((userAgent == null ? "" : userAgent) + "|" + request.getRemoteAddr());
            }
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
            String rlKey = "rl:login:" + ip + ":" + deviceFingerprint;
            long attempts = 0L;
            try { attempts = redisService.incrementWithTtl(rlKey, java.time.Duration.ofMinutes(1)); } catch (Exception ignore) {}
            if (attempts > 5) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Quá nhiều yêu cầu đăng nhập. Vui lòng thử lại sau."));
            }
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );

            // Set SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Principal as UserDetails
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Prefer using UserPrincipal to avoid extra DB lookup
            Long userId;
            String userEmail;
            boolean verified;
            if (userDetails instanceof com.example.online_quiz_system.security.UserPrincipal up) {
                userId = up.getId();
                userEmail = up.getUsername();
                verified = up.isVerified();
            } else {
                User user = userRepository.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                userId = user.getId();
                userEmail = user.getEmail();
                verified = user.isVerified();
            }

            // Check email verified
            if (!verified) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Tài khoản chưa được xác thực. Vui lòng kiểm tra email."));
            }

            // Generate tokens (using userDetails)
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Extract roles
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Persist refresh token (hashed) and create login session
            String tokenHash = HashUtils.sha256(refreshToken);
            String deviceName = request.getHeader("X-Device-Name");

            RefreshToken rt = RefreshToken.builder()
                    .user(userRepository.getReferenceById(userId))
                    .tokenHash(tokenHash)
                    .deviceFingerprint(deviceFingerprint)
                    .deviceName(deviceName)
                    .ipAddress(ip)
                    .location(null)
                    .expiresAt(java.time.LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                    .revoked(false)
                    .lastUsedAt(java.time.LocalDateTime.now())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            refreshTokenRepository.save(rt);

            LoginSession session = LoginSession.builder()
                    .user(userRepository.getReferenceById(userId))
                    .deviceFingerprint(deviceFingerprint)
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .location(null)
                    .loginTime(java.time.LocalDateTime.now())
                    .riskScore(0.0)
                    .flaggedReasons(null)
                    .build();
            loginSessionRepository.save(session);

            // Cache tokens in Redis
            try {
                String[] parts = accessToken.split("\\.");
                if (parts.length == 3) {
                    String sig = parts[2];
                    long accessExpTs = System.currentTimeMillis() + accessTokenExpiryMs;
                    redisService.cacheAccessToken(sig, userId + ":" + accessExpTs, java.time.Duration.ofMillis(accessTokenExpiryMs));
                }
                redisService.cacheRefreshToken(tokenHash, userId + ":" + deviceFingerprint, java.time.Duration.ofMillis(refreshTokenExpiryMs));
            } catch (Exception ignore) {}

            // Set refresh token as HttpOnly cookie. Do NOT include it in response body.
            try {
                long maxAgeSec = Math.max(0, refreshTokenExpiryMs / 1000);
                ResponseCookie cookie = buildRefreshCookie(refreshToken, request, maxAgeSec);
                response.addHeader("Set-Cookie", cookie.toString());
            } catch (Exception ignore) {}

            JwtResponseDTO jwtResponse = new JwtResponseDTO(
                    accessToken,
                    null,
                    userId,
                    userEmail,
                    roles,
                    verified
            );

            // Sau khi đăng nhập thành công
            userService.logLogin(userRepository.getReferenceById(userId), ip, deviceFingerprint);
            try { auditLogService.log(userRepository.getReferenceById(userId), "LOGIN_SUCCESS", true, null, ip, deviceFingerprint, userAgent); } catch (Exception ignore) {}

            return ResponseEntity.ok(jwtResponse);

        } catch (BadCredentialsException e) {
            String ua = request.getHeader("User-Agent");
            String dfp = request.getHeader("X-Device-Fingerprint");
            if (dfp == null || dfp.isBlank()) {
                dfp = HashUtils.sha256((ua == null ? "" : ua) + "|" + request.getRemoteAddr());
            }
            String ipAddr = request.getHeader("X-Forwarded-For");
            if (ipAddr == null || ipAddr.isBlank()) ipAddr = request.getRemoteAddr();
            try { auditLogService.log(null, "LOGIN_FAIL", false, "Bad credentials", ipAddr, dfp, ua); } catch (Exception ignore) {}
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email hoặc mật khẩu không đúng"));
        } catch (DisabledException e) {
            String ua = request.getHeader("User-Agent");
            String dfp = request.getHeader("X-Device-Fingerprint");
            if (dfp == null || dfp.isBlank()) {
                dfp = HashUtils.sha256((ua == null ? "" : ua) + "|" + request.getRemoteAddr());
            }
            String ipAddr = request.getHeader("X-Forwarded-For");
            if (ipAddr == null || ipAddr.isBlank()) ipAddr = request.getRemoteAddr();
            try { auditLogService.log(null, "LOGIN_FAIL", false, "Disabled account", ipAddr, dfp, ua); } catch (Exception ignore) {}
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Tài khoản chưa được kích hoạt"));
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi đăng nhập"));
        }
    }

    // ---------------- Forgot password (request reset link) ----------------
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email là bắt buộc"));
        }
        try {
            passwordResetService.requestReset(email);
        } catch (Exception ignore) {}
        return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, liên kết đặt lại đã được gửi"));
    }

    // ---------------- Reset password ----------------
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu token hoặc mật khẩu mới"));
        }
        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Không thể đặt lại mật khẩu"));
        }
    }

    // ---------------- Refresh token ----------------
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // Read refresh token from HttpOnly cookie
        String refreshToken = null;
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("refreshToken".equals(c.getName())) {
                        refreshToken = c.getValue();
                        break;
                    }
                }
            }
        } catch (Exception ignore) {}

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token là bắt buộc"));
        }

        try {
            // Validate refresh token structure/signature first
            if (!jwtService.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token không hợp lệ"));
            }

            // Ensure token is a refresh token (typ=refresh)
            try {
                String typ = jwtService.extractClaim(refreshToken, claims -> claims.get("typ", String.class));
                if (!"refresh".equalsIgnoreCase(typ)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Sai loại token. Yêu cầu refresh token"));
                }
            } catch (Exception ignore) {
                // If claim extraction fails here, we already validated signature above; continue to other checks.
            }

            // Extract username and load user details via CustomUserDetailsService
            String username = jwtService.extractUsername(refreshToken);
            if (username == null || username.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token không hợp lệ"));
            }

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Enforce device/IP matching and check refresh token against DB
            String reqUserAgent = request.getHeader("User-Agent");
            String reqDeviceFp = request.getHeader("X-Device-Fingerprint");
            if (reqDeviceFp == null || reqDeviceFp.isBlank()) {
                logger.warn("Missing X-Device-Fingerprint header on refresh request from IP={}", request.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Thiết bị không xác định"));
            }
            String reqIp = request.getHeader("X-Forwarded-For");
            if (reqIp == null || reqIp.isBlank()) reqIp = request.getRemoteAddr();

            String tokenHash = HashUtils.sha256(refreshToken);
            var stored = refreshTokenRepository.findByTokenHash(tokenHash).orElse(null);
            if (stored == null || stored.isRevoked() || stored.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                try { auditLogService.log(null, "REFRESH_FAIL", false, "Token expired/revoked/missing", reqIp, reqDeviceFp, reqUserAgent); } catch (Exception ignore) {}
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token đã hết hạn/thu hồi"));
            }

            if ((stored.getDeviceFingerprint() != null && !stored.getDeviceFingerprint().equals(reqDeviceFp))
                    || (stored.getIpAddress() != null && !stored.getIpAddress().equals(reqIp))) {
                try { auditLogService.log(null, "REFRESH_FAIL", false, "Device/IP mismatch", reqIp, reqDeviceFp, reqUserAgent); } catch (Exception ignore) {}
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Thiết bị hoặc IP không khớp"));
            }

            // Revoke old token BEFORE issuing new one (one-time use)
            stored.setRevoked(true);
            stored.setLastUsedAt(java.time.LocalDateTime.now());
            refreshTokenRepository.save(stored);

            // Build new tokens
            String newAccessToken = jwtService.generateAccessToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            Long userId;
            String userEmail;
            boolean verified;
            if (userDetails instanceof com.example.online_quiz_system.security.UserPrincipal up) {
                userId = up.getId();
                userEmail = up.getUsername();
                verified = up.isVerified();
            } else {
                com.example.online_quiz_system.entity.User userEntity = userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                userId = userEntity.getId();
                userEmail = userEntity.getEmail();
                verified = userEntity.isVerified();
            }

            // Save new refresh token and set cookie
            String newHash = HashUtils.sha256(newRefreshToken);
            String userAgent = request.getHeader("User-Agent");
            String deviceFingerprint = request.getHeader("X-Device-Fingerprint");
            if (deviceFingerprint == null || deviceFingerprint.isBlank()) {
                deviceFingerprint = HashUtils.sha256((userAgent == null ? "" : userAgent) + "|" + request.getRemoteAddr());
            }
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();

            RefreshToken rt = RefreshToken.builder()
                    .user(userRepository.getReferenceById(userId))
                    .tokenHash(newHash)
                    .deviceFingerprint(deviceFingerprint)
                    .deviceName(request.getHeader("X-Device-Name"))
                    .ipAddress(ip)
                    .location(null)
                    .expiresAt(java.time.LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                    .revoked(false)
                    .lastUsedAt(java.time.LocalDateTime.now())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            refreshTokenRepository.save(rt);

            // Cache tokens
            try {
                String[] parts = newAccessToken.split("\\.");
                if (parts.length == 3) {
                    String sig = parts[2];
                    long accessExpTs = System.currentTimeMillis() + accessTokenExpiryMs;
                    redisService.cacheAccessToken(sig, userId + ":" + accessExpTs, java.time.Duration.ofMillis(accessTokenExpiryMs));
                }
                redisService.cacheRefreshToken(newHash, userId + ":" + deviceFingerprint, java.time.Duration.ofMillis(refreshTokenExpiryMs));
            } catch (Exception ignore) {}

            // Set new refresh token cookie
            try {
                long maxAgeSec = Math.max(0, refreshTokenExpiryMs / 1000);
                ResponseCookie cookie = buildRefreshCookie(newRefreshToken, request, maxAgeSec);
                response.addHeader("Set-Cookie", cookie.toString());
            } catch (Exception ignore) {}

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            JwtResponseDTO jwtResponse = new JwtResponseDTO(
                    newAccessToken,
                    null,
                    userId,
                    userEmail,
                    roles,
                    verified
            );

            // Sau khi refresh token thành công
            userService.logRefreshTokenRotation(userRepository.getReferenceById(userId), ip, deviceFingerprint);

            try { auditLogService.log(userRepository.getReferenceById(userId), "REFRESH_SUCCESS", true, null, ip, deviceFingerprint, userAgent); } catch (Exception ignore) {}
            return ResponseEntity.ok(jwtResponse);

        } catch (Exception e) {
            logger.error("Refresh token error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token không hợp lệ"));
        }
    }

    // ---------------- Logout (blacklist access token + delete refresh token) ----------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) Map<String, String> body) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Thiếu token"));
            }
            String accessToken = authHeader.substring(7);

            if (!jwtService.validateToken(accessToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token không hợp lệ"));
            }

            String[] parts = accessToken.split("\\.");
            if (parts.length == 3) {
                String sig = parts[2];
                long ttlMs = accessTokenExpiryMs;
                redisService.blacklistToken(sig, java.time.Duration.ofMillis(ttlMs));
                try { redisService.deleteAccessTokenSignature(sig); } catch (Exception ignore) {}
            }

            // Remove refresh token cookie and delete stored refresh token(s)
            try {
                // delete cookie on client
                // Use same secure/samesite handling as when creating the cookie
                long maxAgeSec = 0L;
                ResponseCookie delete = buildRefreshCookie("", request, maxAgeSec);
                // setting maxAge=0 will remove cookie on client
                response.addHeader("Set-Cookie", delete.toString());
            } catch (Exception ignore) {}
            // also attempt to remove refresh tokens related to this device if possible (by cookie or body)
            try {
                String refreshToken = null;
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie c : cookies) {
                        if ("refreshToken".equals(c.getName())) { refreshToken = c.getValue(); break; }
                    }
                }
                if ((body != null && body.get("refreshToken") != null) || (refreshToken != null && !refreshToken.isBlank())) {
                    String rt = body != null && body.get("refreshToken") != null ? body.get("refreshToken") : refreshToken;
                    if (rt != null && !rt.isBlank() && jwtService.validateToken(rt)) {
                        String refreshHash = HashUtils.sha256(rt);
                        try { refreshTokenRepository.deleteByTokenHash(refreshHash); } catch (Exception ignore) {}
                        try { redisService.deleteRefreshTokenHash(refreshHash); } catch (Exception ignore) {}
                    }
                }
            } catch (Exception ignore) {}

            // Khi logout/revoke session
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
            String deviceFingerprint = request.getHeader("X-Device-Fingerprint");
            if (deviceFingerprint == null || deviceFingerprint.isBlank()) {
                String userAgent = request.getHeader("User-Agent");
                deviceFingerprint = HashUtils.sha256((userAgent == null ? "" : userAgent) + "|" + request.getRemoteAddr());
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String email = auth.getName();
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    userService.logLogout(user, ip, deviceFingerprint);
                }
            }

            return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
        } catch (Exception e) {
            logger.error("Logout error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Có lỗi xảy ra khi đăng xuất"));
        }
    }

    // ---------------- Change password (revoke all refresh tokens) ----------------
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
            }

            userService.changePasswordAndRevokeAll(user, dto.getCurrentPassword(), dto.getNewPassword());

            // Blacklist current access token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                if (jwtService.validateToken(accessToken)) {
                    String[] parts = accessToken.split("\\.");
                    if (parts.length == 3) {
                        String sig = parts[2];
                        redisService.blacklistToken(sig, java.time.Duration.ofMillis(accessTokenExpiryMs));
                        try { redisService.deleteAccessTokenSignature(sig); } catch (Exception ignore) {}
                    }
                }
            }

            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công. Tất cả phiên đăng nhập đã bị thu hồi."));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Change password error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Không thể đổi mật khẩu"));
        }
    }

    // ---------------- Verify email ----------------
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token là bắt buộc"));
        }

        boolean isVerified = verificationService.verifyToken(token);
        if (isVerified) {
            return ResponseEntity.ok().body(Map.of("message", "Xác thực email thành công"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Token không hợp lệ hoặc đã hết hạn"));
        }
    }

    // ---------------- Resend verification ----------------
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email là bắt buộc"));
        }

        email = email.trim().toLowerCase();

        try {
            userService.resendVerificationByEmail(email);
            logger.info("Requested resend verification for email: {}", email);
        } catch (BusinessException e) {
            // Avoid leaking whether email exists - respond with the same generic message as forgot-password
            logger.info("Resend verification requested for non-existing or invalid email: {} - {}", email, e.getMessage());
        } catch (Exception e) {
            logger.error("Error while resending verification for {}: {}", email, e.getMessage(), e);
        }
        return ResponseEntity.ok().body(Map.of("message", "Nếu email tồn tại, một liên kết xác thực mới đã được gửi"));
    }
}
