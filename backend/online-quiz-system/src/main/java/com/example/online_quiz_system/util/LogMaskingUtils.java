package com.example.online_quiz_system.util;

/**
 * Utility class for masking sensitive information in logs
 * 
 * @author Online Quiz System
 * @version 1.0
 */
public class LogMaskingUtils {

    /**
     * Mask email address for logging
     * Example: "user@example.com" -> "u***@example.com"
     * 
     * @param email Email address to mask
     * @return Masked email address
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "***";
        }
        
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        
        return email.substring(0, 1) + "***" + email.substring(atIndex);
    }

    /**
     * Mask phone number for logging
     * Example: "+84901234567" -> "+84***4567"
     * 
     * @param phone Phone number to mask
     * @return Masked phone number
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "***";
        }
        
        if (phone.length() <= 4) {
            return "***";
        }
        
        return phone.substring(0, 3) + "***" + phone.substring(phone.length() - 4);
    }

    /**
     * Mask user ID for logging (show only last 3 digits)
     * Example: 12345 -> "***45"
     * 
     * @param userId User ID to mask
     * @return Masked user ID
     */
    public static String maskUserId(Long userId) {
        if (userId == null) {
            return "***";
        }
        
        String userIdStr = userId.toString();
        if (userIdStr.length() <= 3) {
            return "***";
        }
        
        return "***" + userIdStr.substring(userIdStr.length() - 3);
    }

    /**
     * Mask JWT token for logging (show only first 10 and last 10 characters)
     * Example: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." -> "eyJhbGciOi***...J9"
     * 
     * @param token JWT token to mask
     * @return Masked JWT token
     */
    public static String maskJwtToken(String token) {
        if (token == null || token.isEmpty()) {
            return "***";
        }
        
        if (token.length() <= 20) {
            return "***";
        }
        
        return token.substring(0, 10) + "***" + token.substring(token.length() - 10);
    }

    /**
     * Mask any sensitive string (show only first 2 and last 2 characters)
     * 
     * @param sensitiveString String to mask
     * @return Masked string
     */
    public static String maskSensitiveString(String sensitiveString) {
        if (sensitiveString == null || sensitiveString.isEmpty()) {
            return "***";
        }
        
        if (sensitiveString.length() <= 4) {
            return "***";
        }
        
        return sensitiveString.substring(0, 2) + "***" + sensitiveString.substring(sensitiveString.length() - 2);
    }

    /**
     * Get domain from email for logging
     * Example: "user@example.com" -> "@example.com"
     * 
     * @param email Email address
     * @return Domain part of email
     */
    public static String getEmailDomain(String email) {
        if (email == null || email.isEmpty()) {
            return "@***";
        }
        
        int atIndex = email.indexOf("@");
        if (atIndex == -1) {
            return "@***";
        }
        
        return email.substring(atIndex);
    }
}
