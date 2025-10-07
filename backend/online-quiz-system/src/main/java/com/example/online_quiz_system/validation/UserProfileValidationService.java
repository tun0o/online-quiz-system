package com.example.online_quiz_system.validation;

import com.example.online_quiz_system.entity.UserProfile;
import com.example.online_quiz_system.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CENTRALIZED Validation Service
 * - Application-layer validation only
 * - No DB constraints dependency
 * - Comprehensive validation rules
 */
@Service
@Slf4j
public class UserProfileValidationService {

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+84|0)[0-9]{9,10}$"
    );
    
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://.*"
    );

    /**
     * FULL VALIDATION: Validate toàn bộ UserProfile
     */
    public ValidationResult validate(UserProfile profile) {
        ValidationResult result = new ValidationResult();
        
        if (profile == null) {
            result.addError("profile", "UserProfile không được null");
            return result;
        }

        // Validate individual fields
        validateFullName(profile.getFullName(), result);
        validateEmail(profile.getEmail(), result);
        validateDateOfBirth(profile.getDateOfBirth(), result);
        validateEmergencyPhone(profile.getEmergencyPhone(), result);
        validateAvatarUrl(profile.getAvatarUrl(), result);
        validateBio(profile.getBio(), result);
        validateGrade(profile.getGrade(), result);
        validateGoal(profile.getGoal(), result);

        log.debug("Validation completed for UserProfile. Errors: {}", result.getErrorCount());
        return result;
    }

    /**
     * QUICK VALIDATION: Validate chỉ các trường bắt buộc
     */
    public ValidationResult validateRequired(UserProfile profile) {
        ValidationResult result = new ValidationResult();
        
        if (profile == null) {
            result.addError("profile", "UserProfile không được null");
            return result;
        }

        validateFullName(profile.getFullName(), result);
        validateEmail(profile.getEmail(), result);

        return result;
    }

    /**
     * FIELD VALIDATION: Validate từng field riêng lẻ
     */
    public boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidPhone(String phone) {
        return phone == null || phone.trim().isEmpty() || PHONE_PATTERN.matcher(phone).matches();
    }

    public boolean isValidUrl(String url) {
        return url == null || url.trim().isEmpty() || URL_PATTERN.matcher(url).matches();
    }

    // Private validation methods
    private void validateFullName(String fullName, ValidationResult result) {
        if (fullName == null || fullName.trim().isEmpty()) {
            result.addError("fullName", "Tên không được để trống");
            return;
        }

        String trimmed = fullName.trim();
        if (trimmed.length() < 2) {
            result.addError("fullName", "Tên phải có ít nhất 2 ký tự");
        } else if (trimmed.length() > 100) {
            result.addError("fullName", "Tên không được vượt quá 100 ký tự");
        }
    }

    private void validateEmail(String email, ValidationResult result) {
        if (email == null || email.trim().isEmpty()) {
            result.addError("email", "Email không được để trống");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            result.addError("email", "Email không đúng định dạng");
        }
    }

    private void validateDateOfBirth(LocalDate dateOfBirth, ValidationResult result) {
        if (dateOfBirth != null) {
            LocalDate now = LocalDate.now();
            
            if (dateOfBirth.isAfter(now)) {
                result.addError("dateOfBirth", "Ngày sinh phải là ngày trong quá khứ");
            }
            
            LocalDate hundredYearsAgo = now.minusYears(100);
            if (dateOfBirth.isBefore(hundredYearsAgo)) {
                result.addError("dateOfBirth", "Ngày sinh không hợp lệ (quá 100 năm)");
            }
        }
    }

    private void validateEmergencyPhone(String phone, ValidationResult result) {
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                result.addError("emergencyPhone", "Số điện thoại Việt Nam không hợp lệ");
            }
        }
    }

    private void validateAvatarUrl(String url, ValidationResult result) {
        if (url != null && !url.trim().isEmpty()) {
            if (!URL_PATTERN.matcher(url).matches()) {
                result.addError("avatarUrl", "URL không hợp lệ");
            }
        }
    }

    private void validateBio(String bio, ValidationResult result) {
        if (bio != null && bio.length() > 1000) {
            result.addError("bio", "Bio không được vượt quá 1000 ký tự");
        }
    }

    private void validateGrade(String grade, ValidationResult result) {
        if (grade != null && !grade.trim().isEmpty()) {
            if (!grade.matches("^(10|11|12)$")) {
                result.addError("grade", "Lớp học chỉ được là 10, 11, hoặc 12");
            }
        }
    }

    private void validateGoal(String goal, ValidationResult result) {
        if (goal != null && goal.length() > 500) {
            result.addError("goal", "Mục tiêu không được vượt quá 500 ký tự");
        }
    }

    /**
     * VALIDATION RESULT class
     */
    public static class ValidationResult {
        private final List<ValidationError> errors = new ArrayList<>();

        public void addError(String field, String message) {
            errors.add(new ValidationError(field, message));
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<ValidationError> getErrors() {
            return new ArrayList<>(errors);
        }

        public int getErrorCount() {
            return errors.size();
        }

        public void throwIfInvalid() {
            if (!isValid()) {
                String message = "Validation failed: " + 
                    errors.stream()
                        .map(e -> e.getField() + ": " + e.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Unknown error");
                throw new BusinessException(message);
            }
        }
    }

    /**
     * VALIDATION ERROR class
     */
    public static class ValidationError {
        private final String field;
        private final String message;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public String getMessage() { return message; }
    }
}
