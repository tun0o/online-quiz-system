package com.example.online_quiz_system.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác thực email - Online Quiz System");
        message.setText("Xin chào!\n\n" +
                "Vui lòng nhấp vào liên kết dưới đây để xác thực email của bạn:\n" +
                verificationLink + "\n\n" +
                "Liên kết này có hiệu lực trong 24 giờ.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ Online Quiz System");

        mailSender.send(message);
    }
    
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Đặt lại mật khẩu - Online Quiz System");
        message.setText("Xin chào!\n\n" +
                "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình.\n\n" +
                "Vui lòng nhấp vào liên kết dưới đây để đặt lại mật khẩu:\n" +
                resetLink + "\n\n" +
                "Liên kết này có hiệu lực trong 1 giờ.\n" +
                "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ Online Quiz System");

        mailSender.send(message);
    }
}