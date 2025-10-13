package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.CreatePaymentRequestDTO;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @PostMapping("/create-vnpay")
    public ResponseEntity<?> createPayment(@Valid @RequestBody CreatePaymentRequestDTO requestDTO,
                                           @AuthenticationPrincipal UserPrincipal currentUser,
                                           HttpServletRequest request) {
        try {
            String paymentUrl = paymentService.createVnpayPayment(requestDTO, currentUser.getId(), request);
            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Lỗi tạo URL thanh toán."));
        }
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<Void> vnpayReturn(HttpServletRequest request){
        // Backend xử lý kết quả trả về từ VNPay
        paymentService.handleVnpayReturn(request);

        // Lấy các tham số cần thiết để chuyển về cho frontend
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");

        // Tạo URL chuyển hướng về frontend
        String redirectUrl = frontendUrl + "/payment/result" +
                "?vnp_ResponseCode=" + URLEncoder.encode(vnp_ResponseCode, StandardCharsets.UTF_8) +
                "&vnp_TxnRef=" + URLEncoder.encode(vnp_TxnRef, StandardCharsets.UTF_8);

        return ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectUrl).build();
    }
}
