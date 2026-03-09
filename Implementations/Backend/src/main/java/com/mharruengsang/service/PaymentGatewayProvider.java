package com.mharruengsang.service;

import com.mharruengsang.dto.PaymentRequestDTO;
import com.mharruengsang.entity.Payment;
import com.mharruengsang.service.dto.PaymentProcessResult;
import com.mharruengsang.service.dto.BankTransferResult;
import com.mharruengsang.service.dto.PaymentVerificationResult;
import com.mharruengsang.service.dto.RefundResult;

public interface PaymentGatewayProvider {
    
    /**
     * Process credit card payment through the gateway
     */
    PaymentProcessResult processCardPayment(PaymentRequestDTO request, Payment payment);
    
    /**
     * Process bank transfer payment
     */
    BankTransferResult processBankTransfer(Payment payment);
    
    /**
     * Verify payment status with the gateway
     */
    PaymentVerificationResult verifyPayment(String transactionId);
    
    /**
     * Refund a payment
     */
    RefundResult refundPayment(String transactionId, java.math.BigDecimal amount);
}
