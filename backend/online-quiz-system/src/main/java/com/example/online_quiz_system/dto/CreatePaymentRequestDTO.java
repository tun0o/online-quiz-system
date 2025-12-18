package com.example.online_quiz_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequestDTO {
    @NotNull(message = "Mã gói không được để trống")
    private Integer packageId;
}
