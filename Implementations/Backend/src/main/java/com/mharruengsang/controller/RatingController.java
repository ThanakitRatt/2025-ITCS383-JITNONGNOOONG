package com.mharruengsang.controller;

import com.mharruengsang.dto.ApiResponse;
import com.mharruengsang.dto.RiderRatingDTO;
import com.mharruengsang.service.RatingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Rider Ratings
 * 
 * BASE URL: /api/ratings
 */
@RestController
@RequestMapping("/ratings")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:4000", "http://localhost:3000"})
@Slf4j
public class RatingController {
    
    private final RatingService ratingService;
    
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }
    
    /**
     * POST /ratings
     * Submit rating for rider after delivery
     * Customer can rate "politeness" and "speed" on a scale of 1-5
     * 
     * Request body:
     * {
     *   "orderId": 123,
     *   "customerId": 456,
     *   "riderId": 789,
     *   "politenessScore": 5,
     *   "speedScore": 4,
     *   "review": "Great delivery, very professional"
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RiderRatingDTO>> rateRider(
            @RequestParam Long orderId,
            @RequestParam Long customerId,
            @RequestBody RiderRatingDTO ratingDTO) {
        try {
            log.info("Rating rider for order: {}", orderId);
            
            RiderRatingDTO rating = ratingService.rateRider(orderId, customerId, ratingDTO);
            
            return ResponseEntity.ok(ApiResponse.ok(rating, "Rating submitted"));
            
        } catch (Exception e) {
            log.error("Error submitting rating: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Rating submission failed: " + e.getMessage()));
        }
    }
    
    /**
     * GET /ratings/rider/:riderId
     * Get all ratings for a specific rider
     */
    @GetMapping("/rider/{riderId}")
    public ResponseEntity<ApiResponse<List<RiderRatingDTO>>> getRiderRatings(
            @PathVariable Long riderId) {
        try {
            List<RiderRatingDTO> ratings = ratingService.getRiderRatings(riderId);
            return ResponseEntity.ok(ApiResponse.ok(ratings));
            
        } catch (Exception e) {
            log.error("Error getting rider ratings: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get ratings: " + e.getMessage()));
        }
    }
    
    /**
     * GET /ratings/rider/:riderId/average
     * Get average rating for a rider
     */
    @GetMapping("/rider/{riderId}/average")
    public ResponseEntity<ApiResponse<Double>> getRiderAverageRating(
            @PathVariable Long riderId) {
        try {
            Double average = ratingService.getRiderAverageRating(riderId);
            return ResponseEntity.ok(ApiResponse.ok(average));
            
        } catch (Exception e) {
            log.error("Error getting average rating: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get rating: " + e.getMessage()));
        }
    }
    
    /**
     * GET /ratings/order/:orderId
     * Get rating for a specific order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<RiderRatingDTO>> getOrderRating(
            @PathVariable Long orderId) {
        try {
            RiderRatingDTO rating = ratingService.getOrderRating(orderId);
            return ResponseEntity.ok(ApiResponse.ok(rating));
            
        } catch (Exception e) {
            log.error("Error getting order rating: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get order rating: " + e.getMessage()));
        }
    }
}
