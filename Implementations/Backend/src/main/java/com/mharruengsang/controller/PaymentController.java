package com.mharruengsang.controller;

import com.mharruengsang.dto.ApiResponse;
import com.mharruengsang.dto.PaymentDTO;
import com.mharruengsang.dto.PaymentRequestDTO;
import com.mharruengsang.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Payment Processing
 * 
 * BASE URL: /api/payments
 */
@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:4000", "http://localhost:3000"})
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    /**
     * POST /payments/initiate
     * Initiate payment for an order
     * 
     * Request body:
     * {
     *   "orderId": 123,
     *   "paymentMethod": "CREDIT_CARD", // or BANK_TRANSFER, PROMPTPAY
     *   "provider": "STRIPE", // or OMISE
     *   "amount": 500.00,
     *   "cardNumber": "4242424242424242",
     *   "cardholderName": "John Doe",
     *   "expiryMonth": "12",
     *   "expiryYear": "2025",
     *   "cvc": "123"
     * }
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentDTO>> initiatePayment(
            @RequestBody PaymentRequestDTO request) {
        try {
            log.info("Payment initiation request for order: {}", request.getOrderId());
            
            PaymentDTO payment = paymentService.initiatePayment(request.getOrderId(), request);
            return ResponseEntity.ok(ApiResponse.ok(payment, "Payment initiated successfully"));
            
        } catch (Exception e) {
            log.error("Error initiating payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Payment initiation failed: " + e.getMessage()));
        }
    }
    
    /**
     * POST /payments/process
     * Process payment (internal call after initiation)
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentDTO>> processPayment(
            @RequestBody PaymentRequestDTO request) {
        try {
            log.info("Processing payment for order: {}", request.getOrderId());
            
            PaymentDTO payment = paymentService.processPayment(request.getOrderId(), request);
            return ResponseEntity.ok(ApiResponse.ok(payment, "Payment processed"));
            
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Payment processing failed: " + e.getMessage()));
        }
    }
    
    /**
     * GET /payments/:paymentId/verify
     * Verify payment status with payment gateway
     */
    @GetMapping("/{paymentId}/verify")
    public ResponseEntity<ApiResponse<PaymentDTO>> verifyPayment(
            @PathVariable Long paymentId) {
        try {
            log.info("Verifying payment: {}", paymentId);
            
            PaymentDTO payment = paymentService.verifyPayment(paymentId);
            return ResponseEntity.ok(ApiResponse.ok(payment, "Payment verified"));
            
        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Payment verification failed: " + e.getMessage()));
        }
    }
    
    /**
     * POST /payments/:orderId/promptpay
     * Generate PromptPay QR code for payment
     */
    @PostMapping("/{orderId}/promptpay")
    public ResponseEntity<ApiResponse<PaymentDTO>> generatePromptPayQR(
            @PathVariable Long orderId,
            @RequestParam String restaurantPhone) {
        try {
            log.info("Generating PromptPay QR for order: {}", orderId);
            
            PaymentDTO payment = paymentService.generatePromptPayQR(orderId, restaurantPhone);
            return ResponseEntity.ok(ApiResponse.ok(payment, "PromptPay QR generated"));
            
        } catch (Exception e) {
            log.error("Error generating PromptPay QR: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("QR generation failed: " + e.getMessage()));
        }
    }
    
    /**
     * GET /payments/:paymentId
     * Get payment details
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentDetails(
            @PathVariable Long paymentId) {
        try {
            PaymentDTO payment = paymentService.getPaymentDetails(paymentId);
            return ResponseEntity.ok(ApiResponse.ok(payment));
            
        } catch (Exception e) {
            log.error("Error getting payment details: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get payment details: " + e.getMessage()));
        }
    }
    
    /**
     * POST /payments/:paymentId/refund
     * Refund a payment
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<PaymentDTO>> refundPayment(
            @PathVariable Long paymentId) {
        try {
            log.info("Refunding payment: {}", paymentId);
            
            PaymentDTO payment = paymentService.refundPayment(paymentId);
            return ResponseEntity.ok(ApiResponse.ok(payment, "Refund processed"));
            
        } catch (Exception e) {
            log.error("Error refunding payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Refund failed: " + e.getMessage()));
        }
    }
    
    /**
     * POST /payments/callback
     * Webhook endpoint for payment gateway callbacks (Stripe, Omise)
     * Called asynchronously by payment provider
     */
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<String>> handlePaymentCallback(
            @RequestParam String transactionId,
            @RequestParam String status) {
        try {
            log.info("Payment callback received - txn: {}, status: {}", transactionId, status);
            
            paymentService.handlePaymentCallback(transactionId, status);
            
            return ResponseEntity.ok(ApiResponse.ok("Callback processed", "Success"));
            
        } catch (Exception e) {
            log.error("Error handling payment callback: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Callback processing failed: " + e.getMessage()));
        }
    }
}
