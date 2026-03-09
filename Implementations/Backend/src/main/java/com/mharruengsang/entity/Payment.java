package com.mharruengsang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod; // CREDIT_CARD, BANK_TRANSFER, PROMPTPAY, PAYPAL
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentProvider provider = PaymentProvider.STRIPE;
    
    // Gateway-specific transaction ID
    @Column(unique = true)
    private String transactionId;
    
    // For bank transfer - reference number
    private String bankReferenceNumber;
    
    // For PromptPay - QR code data
    @Column(columnDefinition = "LONGTEXT")
    private String qrCodeData;
    
    private String failureReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    public enum PaymentMethod {
        CREDIT_CARD, BANK_TRANSFER, PROMPTPAY, PAYPAL, WALLET
    }
    
    public enum PaymentStatus {
        PENDING, PROCESSING, VERIFIED, COMPLETED, FAILED, REFUNDED
    }
    
    public enum PaymentProvider {
        STRIPE, OMISE, INTERNAL
    }
}
