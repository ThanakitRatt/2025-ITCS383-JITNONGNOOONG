package com.mharruengsang.controller;

import com.mharruengsang.dto.ApiResponse;
import com.mharruengsang.dto.PromotionDTO;
import com.mharruengsang.service.PromotionService;
import com.mharruengsang.service.dto.PromotionAppliedResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Promotions/Discount Management
 * 
 * BASE URL: /api/promotions
 */
@RestController
@RequestMapping("/promotions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:4000", "http://localhost:3000"})
@Slf4j
public class PromotionController {
    
    private final PromotionService promotionService;
    
    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }
    
    /**
     * POST /promotions
     * Create a new promotion (for restaurants)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PromotionDTO>> createPromotion(
            @RequestBody PromotionDTO promotionDTO) {
        try {
            log.info("Creating promotion: {}", promotionDTO.getPromotionName());
            
            PromotionDTO created = promotionService.createPromotion(promotionDTO);
            
            return ResponseEntity.ok(ApiResponse.ok(created, "Promotion created"));
            
        } catch (Exception e) {
            log.error("Error creating promotion: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Promotion creation failed: " + e.getMessage()));
        }
    }
    
    /**
     * PUT /promotions/:id
     * Update existing promotion
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionDTO>> updatePromotion(
            @PathVariable Long id,
            @RequestBody PromotionDTO promotionDTO) {
        try {
            log.info("Updating promotion: {}", id);
            
            PromotionDTO updated = promotionService.updatePromotion(id, promotionDTO);
            
            return ResponseEntity.ok(ApiResponse.ok(updated, "Promotion updated"));
            
        } catch (Exception e) {
            log.error("Error updating promotion: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Promotion update failed: " + e.getMessage()));
        }
    }
    
    /**
     * DELETE /promotions/:id
     * Delete promotion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePromotion(
            @PathVariable Long id) {
        try {
            log.info("Deleting promotion: {}", id);
            
            promotionService.deletePromotion(id);
            
            return ResponseEntity.ok(ApiResponse.ok("Promotion deleted"));
            
        } catch (Exception e) {
            log.error("Error deleting promotion: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Promotion deletion failed: " + e.getMessage()));
        }
    }
    
    /**
     * GET /promotions/code/:code
     * Get promotion by code (used during checkout)
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<PromotionDTO>> getByCode(
            @PathVariable String code) {
        try {
            PromotionDTO promotion = promotionService.getPromotionByCode(code);
            return ResponseEntity.ok(ApiResponse.ok(promotion));
            
        } catch (Exception e) {
            log.error("Error getting promotion by code: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Promotion not found: " + e.getMessage()));
        }
    }
    
    /**
     * GET /promotions/restaurant/:restaurantId
     * Get all active promotions for a restaurant
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<PromotionDTO>>> getRestaurantPromotions(
            @PathVariable Long restaurantId) {
        try {
            List<PromotionDTO> promotions = promotionService.getRestaurantPromotions(restaurantId);
            return ResponseEntity.ok(ApiResponse.ok(promotions));
            
        } catch (Exception e) {
            log.error("Error getting restaurant promotions: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get promotions: " + e.getMessage()));
        }
    }
    
    /**
     * POST /promotions/apply
     * Apply promotion code to order (during checkout)
     * 
     * Request body:
     * {
     *   "orderId": 123,
     *   "promotionCode": "SUMMER20",
     *   "customerId": 456
     * }
     */
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<PromotionAppliedResult>> applyPromotion(
            @RequestParam Long orderId,
            @RequestParam String promotionCode,
            @RequestParam Long customerId) {
        try {
            log.info("Applying promotion {} to order {}", promotionCode, orderId);
            
            PromotionAppliedResult result = promotionService.applyPromotion(
                    orderId, promotionCode, customerId
            );
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.ok(result, "Promotion applied"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(result.getMessage()));
            }
            
        } catch (Exception e) {
            log.error("Error applying promotion: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Promotion application failed: " + e.getMessage()));
        }
    }
}
