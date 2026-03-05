package com.mharruengsang.service;

import com.mharruengsang.dto.PromotionDTO;
import com.mharruengsang.service.dto.PromotionAppliedResult;

import java.util.List;

public interface PromotionService {
    
    /**
     * Create a new promotion/discount package
     */
    PromotionDTO createPromotion(PromotionDTO promotionDTO);
    
    /**
     * Update existing promotion
     */
    PromotionDTO updatePromotion(Long promotionId, PromotionDTO promotionDTO);
    
    /**
     * Delete promotion
     */
    void deletePromotion(Long promotionId);
    
    /**
     * Get promotion by code - used during checkout
     */
    PromotionDTO getPromotionByCode(String code);
    
    /**
     * Get all active promotions for a restaurant
     */
    List<PromotionDTO> getRestaurantPromotions(Long restaurantId);
    
    /**
     * Validate and apply promotion code to order
     */
    PromotionAppliedResult applyPromotion(Long orderId, String promotionCode, Long customerId);
}
