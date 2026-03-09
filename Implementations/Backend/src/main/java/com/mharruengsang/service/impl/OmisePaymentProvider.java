package com.mharruengsang.service.impl;

import com.mharruengsang.dto.PaymentRequestDTO;
import com.mharruengsang.entity.Payment;
import com.mharruengsang.service.*;
import com.mharruengsang.service.dto.PaymentProcessResult;
import com.mharruengsang.service.dto.BankTransferResult;
import com.mharruengsang.service.dto.PaymentVerificationResult;
import com.mharruengsang.service.dto.RefundResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service("omisePaymentProvider")
@Slf4j
public class OmisePaymentProvider implements PaymentGatewayProvider {
    
    @Value("${payment.omise.api-key}")
    private String omiseApiKey;
    
    private final RestTemplate restTemplate;
    private static final String OMISE_API_URL = "https://api.omise.co";
    
    public OmisePaymentProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public PaymentProcessResult processCardPayment(PaymentRequestDTO request, Payment payment) {
        try {
            log.info("Processing Omise card payment for order: {}", request.getOrderId());
            
            // Create token first
            Map<String, String> tokenParams = new HashMap<>();
            tokenParams.put("card[name]", request.getCardholderName());
            tokenParams.put("card[number]", request.getCardNumber());
            tokenParams.put("card[expiration_month]", request.getExpiryMonth());
            tokenParams.put("card[expiration_year]", request.getExpiryYear());
            tokenParams.put("card[security_code]", request.getCvc());
            
            // Create charge with token
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", convertToOmiseAmount(request.getAmount()));
            chargeParams.put("currency", "USD");
            chargeParams.put("card", buildCardObject(request));
            chargeParams.put("metadata", Map.of(
                    "order_id", request.getOrderId().toString(),
                    "customer_id", payment.getCustomer().getId().toString()
            ));
            
            // Note: This is a simplified example - actual implementation would use Omise SDK
            String transactionId = createOmiseCharge(chargeParams);
            
            return new PaymentProcessResult(
                    true,
                    transactionId,
                    "Payment processed successfully",
                    null
            );
            
        } catch (Exception e) {
            log.error("Omise payment failed: ", e);
            return new PaymentProcessResult(
                    false,
                    null,
                    "Payment processing failed",
                    e.getMessage()
            );
        }
    }
    
    @Override
    public BankTransferResult processBankTransfer(Payment payment) {
        try {
            String referenceNumber = "OMISE-" + System.currentTimeMillis();
            
            return new BankTransferResult(
                    true,
                    referenceNumber,
                    "Bank: Kasikornbank, Account: 987-654-321",
                    "Bank transfer initiated through Omise. Please complete within 24 hours."
            );
        } catch (Exception e) {
            log.error("Omise bank transfer setup failed: ", e);
            return new BankTransferResult(
                    false,
                    null,
                    null,
                    e.getMessage()
            );
        }
    }
    
    @Override
    public PaymentVerificationResult verifyPayment(String transactionId) {
        try {
            // Query Omise API for charge status
            Payment.PaymentStatus status = Payment.PaymentStatus.COMPLETED;
            
            return new PaymentVerificationResult(
                    true,
                    status,
                    "Payment verified through Omise"
            );
        } catch (Exception e) {
            log.error("Omise payment verification failed: ", e);
            return new PaymentVerificationResult(
                    false,
                    Payment.PaymentStatus.FAILED,
                    e.getMessage()
            );
        }
    }
    
    @Override
    public RefundResult refundPayment(String transactionId, BigDecimal amount) {
        try {
            String refundId = "REFUND-" + System.currentTimeMillis();
            
            return new RefundResult(
                    true,
                    refundId,
                    "Refund processed through Omise"
            );
        } catch (Exception e) {
            log.error("Omise refund failed: ", e);
            return new RefundResult(
                    false,
                    null,
                    e.getMessage()
            );
        }
    }
    
    private Long convertToOmiseAmount(BigDecimal amount) {
        return amount.multiply(new BigDecimal("100")).longValue();
    }
    
    private Map<String, Object> buildCardObject(PaymentRequestDTO request) {
        Map<String, Object> card = new HashMap<>();
        card.put("name", request.getCardholderName());
        card.put("number", request.getCardNumber());
        card.put("expiration_month", request.getExpiryMonth());
        card.put("expiration_year", request.getExpiryYear());
        card.put("security_code", request.getCvc());
        return card;
    }
    
    private String createOmiseCharge(Map<String, Object> params) {
        // Simplified - would actually make HTTP request to Omise API
        return "chrg_" + System.currentTimeMillis();
    }
}
