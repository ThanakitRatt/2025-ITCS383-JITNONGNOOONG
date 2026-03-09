package com.mharruengsang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private User restaurant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = true)
    private User rider;
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "delivery_fee")
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    
    @Column(name = "discount_amount")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "discount_code")
    private String discountCode;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PLACED; // PLACED, CONFIRMED, PAYMENT_PROCESSING, PAID, DELIVERED, CANCELLED
    
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING; // PENDING, ASSIGNED, PICKED_UP, DELIVERED
    
    private String deliveryAddress;
    private String customerNotes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "delivered_at", nullable = true)
    private LocalDateTime deliveredAt;
    
    public enum OrderStatus {
        PLACED, CONFIRMED, PAYMENT_PROCESSING, PAID, DELIVERED, CANCELLED
    }
    
    public enum DeliveryStatus {
        PENDING, ASSIGNED, PICKED_UP, DELIVERED, FAILED
    }
}
