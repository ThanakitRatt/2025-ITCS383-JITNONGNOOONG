package com.mharruengsang.dto;

import com.mharruengsang.entity.Promotion;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDTO {
    private Long id;
    private Long restaurantId;
    private String code;
    private String promotionName;
    private String description;
    private String type; // PERCENTAGE, FIXED_AMOUNT, FREE_DELIVERY
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscountAmount;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer userUsageLimit;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean active;
    
    public static PromotionDTO fromEntity(Promotion promotion) {
        return PromotionDTO.builder()
                .id(promotion.getId())
                .restaurantId(promotion.getRestaurant().getId())
                .code(promotion.getCode())
                .promotionName(promotion.getPromotionName())
                .description(promotion.getDescription())
                .type(promotion.getType().name())
                .discountValue(promotion.getDiscountValue())
                .minimumOrderAmount(promotion.getMinimumOrderAmount())
                .maximumDiscountAmount(promotion.getMaximumDiscountAmount())
                .usageLimit(promotion.getUsageLimit())
                .usageCount(promotion.getUsageCount())
                .userUsageLimit(promotion.getUserUsageLimit())
                .validFrom(promotion.getValidFrom())
                .validUntil(promotion.getValidUntil())
                .active(promotion.getActive())
                .build();
    }
}
