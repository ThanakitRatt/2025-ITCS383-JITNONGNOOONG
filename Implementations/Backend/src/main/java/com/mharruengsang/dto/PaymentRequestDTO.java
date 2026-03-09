package com.mharruengsang.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    private Long orderId;
    private String paymentMethod; // CREDIT_CARD, BANK_TRANSFER, PROMPTPAY
    private String provider; // STRIPE or OMISE
    
    // For credit card
    private String cardNumber;
    private String cardholderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvc;
    
    private BigDecimal amount;
}
