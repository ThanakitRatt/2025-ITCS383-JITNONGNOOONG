package com.mharruengsang.service.dto;

import com.mharruengsang.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationResult {
    private boolean success;
    private Payment.PaymentStatus status;
    private String message;
}
