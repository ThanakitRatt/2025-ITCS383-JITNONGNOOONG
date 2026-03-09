package com.mharruengsang.service;

import com.mharruengsang.dto.OrderDTO;
import com.mharruengsang.dto.PaymentDTO;
import com.mharruengsang.dto.PaymentRequestDTO;
import com.mharruengsang.entity.Order;
import com.mharruengsang.entity.Payment;

public interface PaymentService {
    
    /**
     * Initialize payment for an order - main entry point
     */
    PaymentDTO initiatePayment(Long orderId, PaymentRequestDTO request);
    
    /**
     * Process payment based on selected method
     */
    PaymentDTO processPayment(Long orderId, PaymentRequestDTO request);
    
    /**
     * Verify payment status with gateway and update order
     */
    PaymentDTO verifyPayment(Long paymentId);
    
    /**
     * Generate PromptPay QR code for payment
     */
    PaymentDTO generatePromptPayQR(Long orderId, String restaurantPhoneNumber);
    
    /**
     * Refund a completed payment
     */
    PaymentDTO refundPayment(Long paymentId);
    
    /**
     * Get payment details
     */
    PaymentDTO getPaymentDetails(Long paymentId);
    
    /**
     * Async callback method when payment is verified
     */
    void handlePaymentCallback(String transactionId, String status);
}
