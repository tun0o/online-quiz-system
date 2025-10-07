package com.example.online_quiz_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Email Service for sending verification and password reset emails
 * Uses JavaMailSender for real emails or MockEmailService for development
 */
@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final MockEmailService mockEmailService;
    
    @Value("${app.email.mock:true}")
    private boolean useMockEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender, MockEmailService mockEmailService) {
        this.mailSender = mailSender;
        this.mockEmailService = mockEmailService;
    }

    public void sendVerificationEmail(String toEmail, String verificationLink) {
        // Use mock service if configured
        if (useMockEmail) {
            mockEmailService.sendVerificationEmail(toEmail, verificationLink);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setFrom("doanviettu09122002@gmail.com");
            helper.setSubject("Xác thực email - Online Quiz System");
            
            String content = "Xin chào!<br><br>" +
                    "Vui lòng nhấp vào liên kết dưới đây để xác thực email của bạn:<br>" +
                    "<a href=\"" + verificationLink + "\">" + verificationLink + "</a><br><br>" +
                    "Liên kết này có hiệu lực trong 24 giờ.<br><br>" +
                    "Trân trọng,<br>" +
                    "Đội ngũ Online Quiz System";
            
            helper.setText(content, true); // true = HTML content

            mailSender.send(message);
        } catch (Exception e) {
            // Fallback to mock service if real email fails
            mockEmailService.sendVerificationEmail(toEmail, verificationLink);
        }
    }
    
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        // Use mock service if configured
        if (useMockEmail) {
            mockEmailService.sendPasswordResetEmail(toEmail, resetLink);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setFrom("doanviettu09122002@gmail.com");
            helper.setSubject("Đặt lại mật khẩu - Online Quiz System");
            
            String content = "Xin chào!<br><br>" +
                    "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình.<br><br>" +
                    "Vui lòng nhấp vào liên kết dưới đây để đặt lại mật khẩu:<br>" +
                    "<a href=\"" + resetLink + "\">" + resetLink + "</a><br><br>" +
                    "Liên kết này có hiệu lực trong 1 giờ.<br>" +
                    "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.<br><br>" +
                    "Trân trọng,<br>" +
                    "Đội ngũ Online Quiz System";
            
            helper.setText(content, true); // true = HTML content

            mailSender.send(message);
        } catch (Exception e) {
            // Fallback to mock service if real email fails
            mockEmailService.sendPasswordResetEmail(toEmail, resetLink);
        }
    }
}