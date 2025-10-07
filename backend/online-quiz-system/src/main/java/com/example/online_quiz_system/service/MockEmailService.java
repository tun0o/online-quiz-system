package com.example.online_quiz_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock Email Service for Development
 * Logs email content instead of sending real emails
 */
@Service
@Slf4j
public class MockEmailService {

    public void sendVerificationEmail(String toEmail, String verificationLink) {
        log.info("📧 MOCK EMAIL - Verification Email");
        log.info("To: {}", toEmail);
        log.info("Subject: Xác thực email - Online Quiz System");
        log.info("Verification Link: {}", verificationLink);
        log.info("Content: Xin chào! Vui lòng nhấp vào liên kết trên để xác thực email của bạn.");
        log.info("📧 MOCK EMAIL END");
    }
    
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        log.info("📧 MOCK EMAIL - Password Reset Email");
        log.info("To: {}", toEmail);
        log.info("Subject: Đặt lại mật khẩu - Online Quiz System");
        log.info("Reset Link: {}", resetLink);
        log.info("Content: Xin chào! Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấp vào liên kết trên.");
        log.info("📧 MOCK EMAIL END");
    }
}