package com.mharruengsang.service;

import com.mharruengsang.dto.RiderRatingDTO;

import java.util.List;

public interface RatingService {
    
    /**
     * Submit rating for rider after delivery
     */
    RiderRatingDTO rateRider(Long orderId, Long customerId, RiderRatingDTO ratingDTO);
    
    /**
     * Get all ratings for a rider
     */
    List<RiderRatingDTO> getRiderRatings(Long riderId);
    
    /**
     * Get average rating for a rider
     */
    Double getRiderAverageRating(Long riderId);
    
    /**
     * Get rating for specific order
     */
    RiderRatingDTO getOrderRating(Long orderId);
}
