package com.example.online_quiz_system.service;

import com.example.online_quiz_system.config.VnpayConfig;
import com.example.online_quiz_system.dto.CreatePaymentRequestDTO;
import com.example.online_quiz_system.entity.PaymentTransaction;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.entity.UserRanking;
import com.example.online_quiz_system.enums.PaymentStatus;
import com.example.online_quiz_system.repository.PaymentTransactionRepository;
import com.example.online_quiz_system.repository.UserRankingRepository;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.util.VnpayUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VnpayConfig vnpayConfig;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private UserRankingRepository userRankingRepository;

    private long getAmountFromPackageId(Integer packageId){
        if (packageId == 1) {
            return 20000L;
        } else if (packageId == 2) {
            return 90000L;
        }
        return 200000L;
    }

    private int getPointsFromPackageId(Integer packageId){
        if (packageId == 1) {
            return 200;
        } else if (packageId == 2) {
            return 1000;
        }
        return 2500;
    }

    @Transactional
    public String createVnpayPayment(CreatePaymentRequestDTO requestDTO, Long userId, HttpServletRequest request) throws UnsupportedEncodingException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        long amount = getAmountFromPackageId(requestDTO.getPackageId()) * 100;
        int points = getPointsFromPackageId(requestDTO.getPackageId());

        String vnp_TxnRef = VnpayUtils.getRandomNumber(8);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .user(user)
                .amount(amount/100)
                .pointsPurchased(points)
                .status(PaymentStatus.PENDING)
                .vnpTxnRef(vnp_TxnRef)
                .build();
        paymentTransactionRepository.save(transaction);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnpayConfig.getVersion());
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", VnpayUtils.getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        String queryUrl = VnpayUtils.getQueryUrl(vnp_Params, vnpayConfig.getHashSecret());

        return vnpayConfig.getUrl() + "?" + queryUrl;
    }

    @Transactional
    public boolean handleVnpayReturn(HttpServletRequest request){
        try {
            // Lấy tất cả các tham số từ VNPay
            Map<String, String> fields = new HashMap<>();
            for(Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ){
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if((fieldValue != null) && (!fieldValue.isEmpty())) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }
            if (vnp_SecureHash == null) {
                logger.error("Missing vnp_SecureHash");
                return false;
            }

            // Tạo chuỗi dữ liệu để hash và tính toán chữ ký
            String signValue = VnpayUtils.hashAllFields(fields, vnpayConfig.getHashSecret());

            if(!signValue.equals(vnp_SecureHash)){
                logger.warn("VNPay checksum mismatch. Calculated: {}, Received: {}", signValue, vnp_SecureHash);
                return false;
            }

            String vnp_TxnRef = request.getParameter("vnp_TxnRef");
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_Amount = request.getParameter("vnp_Amount");

            Optional<PaymentTransaction> transactionOptional = paymentTransactionRepository.findByVnpTxnRef(vnp_TxnRef);
            if (transactionOptional.isEmpty()) {
                logger.warn("Transaction not found for vnp_TcnRef: {}", vnp_TxnRef);
                return false;
            }

            PaymentTransaction transaction = transactionOptional.get();

            if(transaction.getAmount() * 100 != Long.parseLong(vnp_Amount)){
                logger.warn("Amount mismatch for transaction {}. Expected: {}, Received: {}", vnp_TxnRef, transaction.getAmount() * 100, vnp_Amount); // Amount is in cents
                return false;
            }

            if(transaction.getStatus() != PaymentStatus.PENDING) {
                logger.warn("Transaction {} already processed with status {}. Ignoring.", vnp_TxnRef, transaction.getStatus());
                return false;
            }

            if("00".equals(vnp_ResponseCode)) {
                logger.info("Payment success for transaction: {}", vnp_TxnRef);
                transaction.setStatus(PaymentStatus.SUCCESS);
                UserRanking userRanking = userRankingRepository.findByUserId(transaction.getUser().getId())
                        .orElseThrow(() -> new EntityNotFoundException("UserRanking not found for user id: " + transaction.getUser().getId()));
                userRanking.setConsumptionPoints(userRanking.getConsumptionPoints() + transaction.getPointsPurchased());
                userRankingRepository.save(userRanking);
            } else {
                logger.warn("Payment failed for transaction: {}, ResponseCode: {}", vnp_TxnRef, vnp_ResponseCode);
                transaction.setStatus(PaymentStatus.FAILED);
            }
            paymentTransactionRepository.save(transaction);
            return "00".equals(vnp_ResponseCode);
        } catch (Exception e) {
            logger.error("Error processing VNPay return", e);
            return false;
        }
    }
}
