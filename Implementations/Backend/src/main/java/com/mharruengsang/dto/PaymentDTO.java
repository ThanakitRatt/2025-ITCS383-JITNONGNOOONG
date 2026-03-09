package com.mharruengsang.dto;

import com.mharruengsang.entity.Payment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long id;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String provider;
    private String transactionId;
    private String bankReferenceNumber;
    private String qrCodeData;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    
    public static PaymentDTO fromEntity(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .customerId(payment.getCustomer().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .status(payment.getStatus().name())
                .provider(payment.getProvider().name())
                .transactionId(payment.getTransactionId())
                .bankReferenceNumber(payment.getBankReferenceNumber())
                .qrCodeData(payment.getQrCodeData())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .verifiedAt(payment.getVerifiedAt())
                .build();
    }
}
