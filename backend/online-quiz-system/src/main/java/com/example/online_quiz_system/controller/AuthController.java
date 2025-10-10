package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.JwtResponseDTO;
import com.example.online_quiz_system.dto.LoginDTO;
import com.example.online_quiz_system.dto.RegisterDto;
import com.example.online_quiz_system.dto.ForgotPasswordDTO;
import com.example.online_quiz_system.dto.ResetPasswordDTO;
import com.example.online_quiz_system.dto.ChangePasswordDTO;
import com.example.online_quiz_system.exception.BusinessException;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.UserService;
import com.example.online_quiz_system.service.VerificationService;
import com.example.online_quiz_system.service.PasswordResetService;
import com.example.online_quiz_system.service.JwtService;
import com.example.online_quiz_system.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final VerificationService verificationService;
    private final PasswordResetService passwordResetService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthController(UserService userService,
                          VerificationService verificationService,
                          PasswordResetService passwordResetService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          CustomUserDetailsService customUserDetailsService) {
        this.userService = userService;
        this.verificationService = verificationService;
        this.passwordResetService = passwordResetService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.customUserDetailsService = customUserDetailsService;
    }

    // ---------------- Register ----------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto) {
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );

            // Set SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // Tải thông tin User đầy đủ từ DB bằng ID có trong Principal
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userPrincipal.getId()));

            // Check email verified
            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Tài khoản chưa được xác thực. Vui lòng kiểm tra email."));
            }

            // Generate tokens (using userDetails)
            String accessToken = jwtService.generateAccessToken(userPrincipal);
            String refreshToken = jwtService.generateRefreshToken(userPrincipal);

            JwtResponseDTO jwtResponse = createJwtResponse(accessToken, refreshToken, user, userPrincipal);
            return ResponseEntity.ok(jwtResponse);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email hoặc mật khẩu không đúng"));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Tài khoản chưa được kích hoạt"));
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi đăng nhập"));
        }
    }

    // ---------------- Refresh token ----------------
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token là bắt buộc"));
        }

        try {
            // Validate refresh token first
            if (!jwtService.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token không hợp lệ"));
            }

            // Extract username and load user details via CustomUserDetailsService
            String username = jwtService.extractUsername(refreshToken);
            if (username == null || username.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token không hợp lệ"));
            }

            // Lấy UserPrincipal đầy đủ (có cả ID)
            UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(username);

            // Find User entity để đảm bảo dữ liệu mới nhất
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Build new tokens
            String newAccessToken = jwtService.generateAccessToken(userPrincipal);
            String newRefreshToken = jwtService.generateRefreshToken(userPrincipal);

            JwtResponseDTO jwtResponse = createJwtResponse(newAccessToken, newRefreshToken, user, userPrincipal);
            return ResponseEntity.ok(jwtResponse);

        } catch (Exception e) {
            logger.error("Refresh token error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token không hợp lệ"));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmailAndLogin(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token là bắt buộc"));
        }

        try {
            User user = verificationService.verifyTokenAndGetUser(token);

            // Tạo UserPrincipal để sinh token
            UserPrincipal userPrincipal = UserPrincipal.create(user);

            // Sinh access và refresh token
            String accessToken = jwtService.generateAccessToken(userPrincipal);
            String refreshToken = jwtService.generateRefreshToken(userPrincipal);

            return ResponseEntity.ok(createJwtResponse(accessToken, refreshToken, user, userPrincipal));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error while resending verification for {}: {}", email, e.getMessage(), e);
        }
        return ResponseEntity.ok().body(Map.of("message", "Nếu email tồn tại, một liên kết xác thực mới đã được gửi"));
    }

    // ---------------- Forgot Password ----------------
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        try {
            passwordResetService.sendPasswordResetEmail(forgotPasswordDTO.getEmail());
            return ResponseEntity.ok().body(Map.of("message", "Nếu email tồn tại, một liên kết đặt lại mật khẩu đã được gửi"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Forgot password error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi xử lý yêu cầu"));
        }
    }

    // ---------------- Reset Password ----------------
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        try {
            boolean success = passwordResetService.resetPassword(
                    resetPasswordDTO.getToken(), 
                    resetPasswordDTO.getNewPassword()
            );
            
            if (success) {
                return ResponseEntity.ok().body(Map.of("message", "Đặt lại mật khẩu thành công"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Token không hợp lệ hoặc đã hết hạn"));
            }
        } catch (Exception e) {
            logger.error("Reset password error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi đặt lại mật khẩu"));
        }
    }

    // ---------------- Change Password ----------------
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            // Lấy user hiện tại từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Bạn cần đăng nhập để thay đổi mật khẩu"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            passwordResetService.changePassword(
                    user.getId(),
                    changePasswordDTO.getCurrentPassword(),
                    changePasswordDTO.getNewPassword()
            );

            return ResponseEntity.ok().body(Map.of("message", "Đổi mật khẩu thành công"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Change password error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi đổi mật khẩu"));
        }
    }

    private JwtResponseDTO createJwtResponse(String accessToken, String refreshToken, User user, UserPrincipal userPrincipal) {
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Tạo một đối tượng UserDTO lồng nhau để phù hợp với cấu trúc frontend mong đợi
        JwtResponseDTO.UserDTO userDTO = new JwtResponseDTO.UserDTO(
                user.getId(),
                user.getEmail(),
                user.getName(), // Thêm tên người dùng
                roles,
                user.isVerified()
        );

        return new JwtResponseDTO(accessToken, refreshToken, userDTO);
    }
}
