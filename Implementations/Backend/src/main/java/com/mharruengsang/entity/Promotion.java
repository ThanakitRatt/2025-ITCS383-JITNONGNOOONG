package com.mharruengsang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private User restaurant;
    
    @Column(nullable = false)
    private String code;
    
    @Column(unique = true, nullable = false)
    private String promotionName;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DiscountType type = DiscountType.PERCENTAGE;
    
    @Column(nullable = false)
    private BigDecimal discountValue; // Either percentage or fixed amount
    
    @Column(name = "minimum_order_amount")
    private BigDecimal minimumOrderAmount;
    
    @Column(name = "maximum_discount_amount")
    private BigDecimal maximumDiscountAmount;
    
    @Column(name = "usage_limit")
    private Integer usageLimit;
    
    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;
    
    @Column(name = "user_limit")
    private Integer userUsageLimit; // Max uses per user
    
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;
    
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT, FREE_DELIVERY
    }
}
