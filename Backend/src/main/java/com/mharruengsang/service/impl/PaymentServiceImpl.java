package com.mharruengsang.service.impl;

import com.mharruengsang.dto.PaymentDTO;
import com.mharruengsang.dto.PaymentRequestDTO;
import com.mharruengsang.entity.Order;
import com.mharruengsang.entity.Payment;
import com.mharruengsang.entity.User;
import com.mharruengsang.repository.OrderRepository;
import com.mharruengsang.repository.PaymentRepository;
import com.mharruengsang.repository.UserRepository;
import com.mharruengsang.service.*;
import com.mharruengsang.service.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Core Payment Service - Orchestrates payment flow
 * 
 * PAYMENT FLOW:
 * 1. Customer selects "Pay" on checkout
 * 2. initiatePayment() is called
 * 3. Payment record is created with PENDING status
 * 4. Based on payment method:
 *    - CREDIT_CARD: Process via Stripe/Omise
 *    - BANK_TRANSFER: Generate reference number
 *    - PROMPTPAY: Generate QR code
 * 5. If payment successful:
 *    - Order status = PAYMENT_PROCESSING -> PAID
 *    - Send async event to Order Service
 *    - Send async event to Rider Service (find nearby riders)
 * 6. Frontend polls verifyPayment() to check status
 */
@Service
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    
    @Autowired
    @Qualifier("stripePaymentProvider")
    private PaymentGatewayProvider stripeProvider;
    
    @Autowired
    @Qualifier("omisePaymentProvider")
    private PaymentGatewayProvider omiseProvider;
    
    private final QRCodeService qrCodeService;
    private final GeolocationService geolocationService;
    private final OrderEventPublisher orderEventPublisher;
    
    public PaymentServiceImpl(OrderRepository orderRepository,
                             PaymentRepository paymentRepository,
                             UserRepository userRepository,
                             QRCodeService qrCodeService,
                             GeolocationService geolocationService,
                             OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
        this.geolocationService = geolocationService;
        this.orderEventPublisher = orderEventPublisher;
    }
    
    /**
     * MAIN ENTRY POINT - Initiate payment for an order
     */
    @Override
    public PaymentDTO initiatePayment(Long orderId, PaymentRequestDTO request) {
        log.info("=== INITIATING PAYMENT FOR ORDER {} ===", orderId);
        
        try {
            // 1. Validate order exists
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // 2. Validate order status is CONFIRMED
            if (!order.getStatus().equals(Order.OrderStatus.CONFIRMED)) {
                throw new RuntimeException("Order must be confirmed before payment. Current status: " + order.getStatus());
            }
            
            // 3. Create payment record with PENDING status
            Payment payment = Payment.builder()
                    .order(order)
                    .customer(order.getCustomer())
                    .amount(request.getAmount())
                    .paymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()))
                    .provider(Payment.PaymentProvider.valueOf(request.getProvider()))
                    .status(Payment.PaymentStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment record created with ID: {}", savedPayment.getId());
            
            // 4. Process payment based on method
            return processPayment(orderId, request);
            
        } catch (Exception e) {
            log.error("Error initiating payment for order: {}", orderId, e);
            throw new RuntimeException("Payment initiation failed: " + e.getMessage());
        }
    }
    
    /**
     * Process payment - delegates to appropriate payment provider
     */
    @Override
    public PaymentDTO processPayment(Long orderId, PaymentRequestDTO request) {
        log.info("=== PROCESSING PAYMENT FOR ORDER {} ===", orderId);
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for order"));
            
            // Update status to PROCESSING
            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            payment.setUpdatedAt(LocalDateTime.now());
            
            PaymentGatewayProvider provider = request.getProvider().equals("STRIPE") ? 
                    stripeProvider : omiseProvider;
            
            Payment.PaymentMethod method = Payment.PaymentMethod.valueOf(request.getPaymentMethod());
            
            switch (method) {
                case CREDIT_CARD:
                    return processCardPayment(order, payment, request, provider);
                    
                case BANK_TRANSFER:
                    return processBankTransfer(order, payment, provider);
                    
                case PROMPTPAY:
                    return generatePromptPayQR(orderId, order.getRestaurant().getPhoneNumber());
                    
                default:
                    throw new RuntimeException("Payment method not supported: " + method);
            }
            
        } catch (Exception e) {
            log.error("Error processing payment for order: {}", orderId, e);
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Process credit card payment
     */
    private PaymentDTO processCardPayment(Order order, Payment payment, 
                                          PaymentRequestDTO request, PaymentGatewayProvider provider) {
        log.info("Processing credit card payment for order: {}", order.getId());
        
        PaymentProcessResult result = provider.processCardPayment(request, payment);
        
        if (result.isSuccess()) {
            payment.setTransactionId(result.getTransactionId());
            payment.setStatus(Payment.PaymentStatus.VERIFIED);
            payment.setVerifiedAt(LocalDateTime.now());
            
            // Update order status to PAID
            order.setStatus(Order.OrderStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());
            
            paymentRepository.save(payment);
            orderRepository.save(order);
            
            log.info("Payment VERIFIED for order: {}, transaction: {}", order.getId(), payment.getTransactionId());
            
            // ASYNC: Publish order paid event for other services (Order, Rider, etc)
            publishOrderPaidEvent(order, payment);
            
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(result.getErrorCode());
            paymentRepository.save(payment);
            
            log.error("Payment FAILED for order: {}, reason: {}", order.getId(), result.getErrorCode());
        }
        
        return PaymentDTO.fromEntity(payment);
    }
    
    /**
     * Process bank transfer - generates reference number
     */
    private PaymentDTO processBankTransfer(Order order, Payment payment, PaymentGatewayProvider provider) {
        log.info("Processing bank transfer for order: {}", order.getId());
        
        BankTransferResult result = provider.processBankTransfer(payment);
        
        if (result.isSuccess()) {
            payment.setBankReferenceNumber(result.getBankReferenceNumber());
            payment.setStatus(Payment.PaymentStatus.PROCESSING); // Pending user action
            
            paymentRepository.save(payment);
            log.info("Bank transfer initiated for order: {}, ref: {}", order.getId(), result.getBankReferenceNumber());
        }
        
        return PaymentDTO.fromEntity(payment);
    }
    
    /**
     * Generate PromptPay QR code
     */
    @Override
    public PaymentDTO generatePromptPayQR(Long orderId, String restaurantPhoneNumber) {
        log.info("Generating PromptPay QR for order: {}", orderId);
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            QRCodeResult qrResult = qrCodeService.generatePromptPayQR(
                    restaurantPhoneNumber,
                    order.getTotalAmount(),
                    orderId.toString()
            );
            
            if (qrResult.isSuccess()) {
                payment.setQrCodeData(qrResult.getQrCodeBase64());
                payment.setStatus(Payment.PaymentStatus.PROCESSING);
                payment.setUpdatedAt(LocalDateTime.now());
                
                paymentRepository.save(payment);
                log.info("PromptPay QR generated for order: {}", orderId);
                
                // ASYNC: Verify payment status periodically
                schedulePromptPayVerification(payment.getId());
            }
            
            return PaymentDTO.fromEntity(payment);
            
        } catch (Exception e) {
            log.error("Error generating PromptPay QR for order: {}", orderId, e);
            throw new RuntimeException("QR generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Verify payment status with gateway
     */
    @Override
    public PaymentDTO verifyPayment(Long paymentId) {
        log.info("Verifying payment: {}", paymentId);
        
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            if (payment.getTransactionId() == null) {
                throw new RuntimeException("No transaction ID to verify");
            }
            
            // Get the appropriate provider
            PaymentGatewayProvider provider = payment.getProvider().equals(Payment.PaymentProvider.STRIPE) ?
                    stripeProvider : omiseProvider;
            
            // Verify with gateway
            PaymentVerificationResult result = provider.verifyPayment(payment.getTransactionId());
            
            if (result.isSuccess()) {
                payment.setStatus(result.getStatus());
                payment.setVerifiedAt(LocalDateTime.now());
                
                // Update order status if payment is completed
                if (result.getStatus().equals(Payment.PaymentStatus.COMPLETED)) {
                    Order order = payment.getOrder();
                    order.setStatus(Order.OrderStatus.PAID);
                    order.setUpdatedAt(LocalDateTime.now());
                    
                    orderRepository.save(order);
                    publishOrderPaidEvent(order, payment);
                }
                
                paymentRepository.save(payment);
                log.info("Payment verified: {}, status: {}", paymentId, result.getStatus());
            }
            
            return PaymentDTO.fromEntity(payment);
            
        } catch (Exception e) {
            log.error("Error verifying payment: {}", paymentId, e);
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }
    
    /**
     * Refund a payment
     */
    @Override
    public PaymentDTO refundPayment(Long paymentId) {
        log.info("Processing refund for payment: {}", paymentId);
        
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            if (!payment.getStatus().equals(Payment.PaymentStatus.COMPLETED)) {
                throw new RuntimeException("Only completed payments can be refunded");
            }
            
            PaymentGatewayProvider provider = payment.getProvider().equals(Payment.PaymentProvider.STRIPE) ?
                    stripeProvider : omiseProvider;
            
            RefundResult result = provider.refundPayment(payment.getTransactionId(), payment.getAmount());
            
            if (result.isSuccess()) {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
                payment.setUpdatedAt(LocalDateTime.now());
                
                // Update order status
                Order order = payment.getOrder();
                order.setStatus(Order.OrderStatus.CANCELLED);
                order.setUpdatedAt(LocalDateTime.now());
                
                paymentRepository.save(payment);
                orderRepository.save(order);
                
                log.info("Refund processed for payment: {}", paymentId);
            }
            
            return PaymentDTO.fromEntity(payment);
            
        } catch (Exception e) {
            log.error("Error refunding payment: {}", paymentId, e);
            throw new RuntimeException("Refund failed: " + e.getMessage());
        }
    }
    
    /**
     * Get payment details
     */
    @Override
    public PaymentDTO getPaymentDetails(Long paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            return PaymentDTO.fromEntity(payment);
        } catch (Exception e) {
            log.error("Error getting payment details: {}", paymentId, e);
            throw new RuntimeException("Failed to get payment details: " + e.getMessage());
        }
    }
    
    /**
     * Handle webhook callback from payment gateway
     */
    @Override
    @Async
    public void handlePaymentCallback(String transactionId, String status) {
        log.info("=== PAYMENT CALLBACK RECEIVED ===");
        log.info("Transaction: {}, Status: {}", transactionId, status);
        
        try {
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
            
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for transaction: {}", transactionId);
                return;
            }
            
            Payment payment = paymentOpt.get();
            Order order = payment.getOrder();
            
            if ("success".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setVerifiedAt(LocalDateTime.now());
                order.setStatus(Order.OrderStatus.PAID);
                
                paymentRepository.save(payment);
                orderRepository.save(order);
                
                publishOrderPaidEvent(order, payment);
                log.info("Payment callback processed successfully for order: {}", order.getId());
                
            } else if ("failed".equalsIgnoreCase(status)) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason(status);
                paymentRepository.save(payment);
                log.warn("Payment callback - failed for order: {}", order.getId());
            }
            
        } catch (Exception e) {
            log.error("Error handling payment callback for transaction: {}", transactionId, e);
        }
    }
    
    /**
     * Publish order paid event to other services
     */
    private void publishOrderPaidEvent(Order order, Payment payment) {
        // This will be handled by Kafka or event bus
        orderEventPublisher.publishOrderPaidEvent(order, payment);
        
        // Also trigger rider assignment
        findAndAssignNearbyRider(order);
    }
    
    /**
     * Find and assign nearby rider for delivery
     */
    @Async
    public void findAndAssignNearbyRider(Order order) {
        try {
            log.info("Finding nearby riders for order: {}", order.getId());
            
            // Get restaurant location
            User restaurant = order.getRestaurant();
            
            var nearbyRiders = geolocationService.findNearbyRiders(
                    restaurant.getLatitude(),
                    restaurant.getLongitude(),
                    5.0, // 5 km radius
                    "AVAILABLE"
            );
            
            if (!nearbyRiders.isEmpty()) {
                // Assign the closest rider (you can implement more sophisticated logic)
                Long closestRiderId = nearbyRiders.get(0).getRiderId();
                User rider = userRepository.findById(closestRiderId)
                        .orElseThrow(() -> new RuntimeException("Rider not found"));
                
                order.setRider(rider);
                order.setDeliveryStatus(Order.DeliveryStatus.ASSIGNED);
                orderRepository.save(order);
                
                log.info("Rider assigned to order: {}, riderId: {}", order.getId(), closestRiderId);
            } else {
                log.warn("No nearby riders found for order: {}", order.getId());
            }
            
        } catch (Exception e) {
            log.error("Error assigning rider for order: {}", order.getId(), e);
        }
    }
    
    /**
     * Schedule periodic verification for PromptPay QR payments
     */
    @Async
    public void schedulePromptPayVerification(Long paymentId) {
        // This could be replaced with a scheduled task that checks payment status every 30 seconds
        try {
            Thread.sleep(30000); // Wait 30 seconds
            verifyPayment(paymentId);
        } catch (Exception e) {
            log.error("Error in PromptPay verification scheduling: {}", paymentId, e);
        }
    }
}
