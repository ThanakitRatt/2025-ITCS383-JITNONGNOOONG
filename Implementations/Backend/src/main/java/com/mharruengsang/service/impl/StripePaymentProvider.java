package com.mharruengsang.service.impl;

import com.mharruengsang.dto.PaymentRequestDTO;
import com.mharruengsang.entity.Payment;
import com.mharruengsang.service.*;
import com.mharruengsang.service.dto.PaymentProcessResult;
import com.mharruengsang.service.dto.BankTransferResult;
import com.mharruengsang.service.dto.PaymentVerificationResult;
import com.mharruengsang.service.dto.RefundResult;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.model.Token;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.TokenCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service("stripePaymentProvider")
@Slf4j
public class StripePaymentProvider implements PaymentGatewayProvider {
    
    @Value("${payment.stripe.api-key}")
    private String stripeApiKey;
    
    public StripePaymentProvider(@Value("${payment.stripe.api-key}") String stripeApiKey) {
        Stripe.apiKey = stripeApiKey;
    }
    
    @Override
    public PaymentProcessResult processCardPayment(PaymentRequestDTO request, Payment payment) {
        try {
            log.info("Processing Stripe card payment for order: {}", request.getOrderId());
            
            // Create card token from request
            TokenCreateParams tokenParams = TokenCreateParams.builder()
                    .setCard(
                            TokenCreateParams.Card.builder()
                                    .setNumber(request.getCardNumber())
                                    .setExpMonth(request.getExpiryMonth())
                                    .setExpYear(request.getExpiryYear())
                                    .setCvc(request.getCvc())
                                    .build()
                    )
                    .build();
            
            Token token = Token.create(tokenParams);
            log.info("Card token created: {}", token.getId());
            
            // Create charge
            ChargeCreateParams chargeParams = ChargeCreateParams.builder()
                    .setAmount(convertToStripeAmount(request.getAmount()))
                    .setCurrency("usd")
                    .setSource(token.getId())
                    .setDescription("Order #" + request.getOrderId())
                    .putMetadata("order_id", String.valueOf(request.getOrderId()))
                    .putMetadata("customer_id", String.valueOf(payment.getCustomer().getId()))
                    .build();
            
            Charge charge = Charge.create(chargeParams);
            
            log.info("Charge created successfully: {}", charge.getId());
            return new PaymentProcessResult(
                    true,
                    charge.getId(),
                    "Payment processed successfully",
                    null
            );
            
        } catch (Exception e) {
            log.error("Stripe payment failed: ", e);
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
            // For bank transfer, generate a reference number
            String referenceNumber = "BANK-" + System.currentTimeMillis();
            
            return new BankTransferResult(
                    true,
                    referenceNumber,
                    "Bank: Thai Bank, Account: 123-456-789",
                    "Bank transfer initiated. Please complete within 24 hours."
            );
        } catch (Exception e) {
            log.error("Bank transfer setup failed: ", e);
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
            Charge charge = Charge.retrieve(transactionId);
            
            boolean success = charge.getPaid() && !charge.getRefunded();
            Payment.PaymentStatus status = success ? 
                    Payment.PaymentStatus.COMPLETED : Payment.PaymentStatus.FAILED;
            
            return new PaymentVerificationResult(
                    success,
                    status,
                    success ? "Payment verified" : "Payment not completed"
            );
        } catch (Exception e) {
            log.error("Payment verification failed: ", e);
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
            Charge charge = Charge.retrieve(transactionId);
            RefundCreateParams params = RefundCreateParams.builder()
                    .setCharge(transactionId)
                    .setAmount(convertToStripeAmount(amount))
                    .build();
            Refund refund = Refund.create(params);
            
            return new RefundResult(
                    true,
                    refund.getId(),
                    "Refund processed successfully"
            );
        } catch (Exception e) {
            log.error("Refund failed: ", e);
            return new RefundResult(
                    false,
                    null,
                    e.getMessage()
            );
        }
    }
    
    /**
     * Convert amount to Stripe format (cents)
     */
    private Long convertToStripeAmount(BigDecimal amount) {
        return amount.multiply(new BigDecimal("100")).longValue();
    }
}
