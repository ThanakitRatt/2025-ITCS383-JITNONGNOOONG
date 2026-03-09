package com.mharruengsang.service.impl;

import com.mharruengsang.dto.PromotionDTO;
import com.mharruengsang.entity.Order;
import com.mharruengsang.entity.Promotion;
import com.mharruengsang.entity.User;
import com.mharruengsang.repository.OrderRepository;
import com.mharruengsang.repository.PromotionRepository;
import com.mharruengsang.repository.UserRepository;
import com.mharruengsang.service.PromotionService;
import com.mharruengsang.service.dto.PromotionAppliedResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PromotionServiceImpl implements PromotionService {
    
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    
    public PromotionServiceImpl(PromotionRepository promotionRepository,
                              UserRepository userRepository,
                              OrderRepository orderRepository) {
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }
    
    @Override
    public PromotionDTO createPromotion(PromotionDTO promotionDTO) {
        log.info("Creating promotion: {}", promotionDTO.getPromotionName());
        
        try {
            User restaurant = userRepository.findById(promotionDTO.getRestaurantId())
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            
            Promotion promotion = Promotion.builder()
                    .restaurant(restaurant)
                    .code(promotionDTO.getCode())
                    .promotionName(promotionDTO.getPromotionName())
                    .description(promotionDTO.getDescription())
                    .type(Promotion.DiscountType.valueOf(promotionDTO.getType()))
                    .discountValue(promotionDTO.getDiscountValue())
                    .minimumOrderAmount(promotionDTO.getMinimumOrderAmount())
                    .maximumDiscountAmount(promotionDTO.getMaximumDiscountAmount())
                    .usageLimit(promotionDTO.getUsageLimit())
                    .userUsageLimit(promotionDTO.getUserUsageLimit())
                    .validFrom(promotionDTO.getValidFrom())
                    .validUntil(promotionDTO.getValidUntil())
                    .active(true)
                    .build();
            
            Promotion saved = promotionRepository.save(promotion);
            return PromotionDTO.fromEntity(saved);
            
        } catch (Exception e) {
            log.error("Error creating promotion: {}", e.getMessage());
            throw new RuntimeException("Promotion creation failed: " + e.getMessage());
        }
    }
    
    @Override
    public PromotionDTO updatePromotion(Long promotionId, PromotionDTO promotionDTO) {
        try {
            Promotion promotion = promotionRepository.findById(promotionId)
                    .orElseThrow(() -> new RuntimeException("Promotion not found"));
            
            promotion.setCode(promotionDTO.getCode());
            promotion.setDescription(promotionDTO.getDescription());
            promotion.setDiscountValue(promotionDTO.getDiscountValue());
            promotion.setMinimumOrderAmount(promotionDTO.getMinimumOrderAmount());
            promotion.setMaximumDiscountAmount(promotionDTO.getMaximumDiscountAmount());
            promotion.setValidFrom(promotionDTO.getValidFrom());
            promotion.setValidUntil(promotionDTO.getValidUntil());
            promotion.setActive(promotionDTO.getActive());
            promotion.setUpdatedAt(LocalDateTime.now());
            
            Promotion updated = promotionRepository.save(promotion);
            return PromotionDTO.fromEntity(updated);
            
        } catch (Exception e) {
            log.error("Error updating promotion: {}", e.getMessage());
            throw new RuntimeException("Promotion update failed: " + e.getMessage());
        }
    }
    
    @Override
    public void deletePromotion(Long promotionId) {
        try {
            promotionRepository.deleteById(promotionId);
            log.info("Promotion deleted: {}", promotionId);
        } catch (Exception e) {
            log.error("Error deleting promotion: {}", e.getMessage());
            throw new RuntimeException("Promotion deletion failed: " + e.getMessage());
        }
    }
    
    @Override
    public PromotionDTO getPromotionByCode(String code) {
        try {
            Promotion promotion = promotionRepository.findByCode(code)
                    .orElseThrow(() -> new RuntimeException("Promotion code not found"));
            
            return PromotionDTO.fromEntity(promotion);
        } catch (Exception e) {
            log.error("Error getting promotion by code: {}", code, e);
            throw new RuntimeException("Promotion lookup failed: " + e.getMessage());
        }
    }
    
    @Override
    public List<PromotionDTO> getRestaurantPromotions(Long restaurantId) {
        try {
            return promotionRepository.findByRestaurantId(restaurantId)
                    .stream()
                    .map(PromotionDTO::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting restaurant promotions: {}", restaurantId, e);
            throw new RuntimeException("Failed to get promotions: " + e.getMessage());
        }
    }
    
    @Override
    public PromotionAppliedResult applyPromotion(Long orderId, String promotionCode, Long customerId) {
        log.info("Applying promotion {} to order {}", promotionCode, orderId);
        
        try {
            // Get order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Get promotion
            Promotion promotion = promotionRepository.findByCode(promotionCode)
                    .orElseThrow(() -> new RuntimeException("Promotion code invalid"));
            
            // Validate promotion
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(promotion.getValidFrom()) || now.isAfter(promotion.getValidUntil())) {
                return new PromotionAppliedResult(false, null, null, "Promotion expired or not yet valid");
            }
            
            if (!promotion.getActive()) {
                return new PromotionAppliedResult(false, null, null, "Promotion is inactive");
            }
            
            if (promotion.getUsageLimit() != null && promotion.getUsageCount() >= promotion.getUsageLimit()) {
                return new PromotionAppliedResult(false, null, null, "Promotion usage limit reached");
            }
            
            if (order.getTotalAmount().compareTo(promotion.getMinimumOrderAmount() != null ? 
                    promotion.getMinimumOrderAmount() : BigDecimal.ZERO) < 0) {
                return new PromotionAppliedResult(false, null, null, 
                    "Order amount below minimum for this promotion");
            }
            
            // Calculate discount
            BigDecimal discountAmount = calculateDiscount(order.getTotalAmount(), promotion);
            
            if (promotion.getMaximumDiscountAmount() != null && 
                discountAmount.compareTo(promotion.getMaximumDiscountAmount()) > 0) {
                discountAmount = promotion.getMaximumDiscountAmount();
            }
            
            // Apply discount to order
            order.setDiscountCode(promotionCode);
            order.setDiscountAmount(discountAmount);
            order.setUpdatedAt(LocalDateTime.now());
            
            // Increment usage count
            promotion.setUsageCount(promotion.getUsageCount() + 1);
            promotion.setUpdatedAt(LocalDateTime.now());
            
            orderRepository.save(order);
            promotionRepository.save(promotion);
            
            BigDecimal newTotal = order.getTotalAmount().subtract(discountAmount);
            
            return new PromotionAppliedResult(true, discountAmount, newTotal, "Promotion applied successfully");
            
        } catch (Exception e) {
            log.error("Error applying promotion: {}", promotionCode, e);
            return new PromotionAppliedResult(false, null, null, "Error: " + e.getMessage());
        }
    }
    
    /**
     * Calculate discount based on promotion type
     */
    private BigDecimal calculateDiscount(BigDecimal orderAmount, Promotion promotion) {
        if (promotion.getType().equals(Promotion.DiscountType.PERCENTAGE)) {
            return orderAmount.multiply(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100));
        } else if (promotion.getType().equals(Promotion.DiscountType.FIXED_AMOUNT)) {
            return promotion.getDiscountValue();
        } else if (promotion.getType().equals(Promotion.DiscountType.FREE_DELIVERY)) {
            // Assume delivery fee is 50 baht
            return BigDecimal.valueOf(50);
        }
        return BigDecimal.ZERO;
    }
}
