package com.mharruengsang.service.impl;

import com.mharruengsang.dto.RiderRatingDTO;
import com.mharruengsang.entity.Order;
import com.mharruengsang.entity.RiderRating;
import com.mharruengsang.entity.User;
import com.mharruengsang.repository.OrderRepository;
import com.mharruengsang.repository.RiderRatingRepository;
import com.mharruengsang.repository.UserRepository;
import com.mharruengsang.service.RatingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class RatingServiceImpl implements RatingService {
    
    private final RiderRatingRepository riderRatingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    
    public RatingServiceImpl(RiderRatingRepository riderRatingRepository,
                           OrderRepository orderRepository,
                           UserRepository userRepository) {
        this.riderRatingRepository = riderRatingRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public RiderRatingDTO rateRider(Long orderId, Long customerId, RiderRatingDTO ratingDTO) {
        log.info("Customer {} rating rider for order {}", customerId, orderId);
        
        try {
            // Get order and validate
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            if (!order.getCustomer().getId().equals(customerId)) {
                throw new RuntimeException("Customer not authorized to rate this order");
            }
            
            if (!order.getStatus().equals(Order.OrderStatus.DELIVERED)) {
                throw new RuntimeException("Can only rate completed deliveries");
            }
            
            // Validate ratings
            if (ratingDTO.getPolitenessScore() < 1 || ratingDTO.getPolitenessScore() > 5) {
                throw new RuntimeException("Politeness score must be between 1 and 5");
            }
            
            if (ratingDTO.getSpeedScore() < 1 || ratingDTO.getSpeedScore() > 5) {
                throw new RuntimeException("Speed score must be between 1 and 5");
            }
            
            // Calculate overall score
            Double overallScore = (ratingDTO.getPolitenessScore() + ratingDTO.getSpeedScore()) / 2.0;
            
            // Create rating
            RiderRating rating = RiderRating.builder()
                    .rider(order.getRider())
                    .customer(order.getCustomer())
                    .order(order)
                    .politenessScore(ratingDTO.getPolitenessScore())
                    .speedScore(ratingDTO.getSpeedScore())
                    .overallScore(overallScore)
                    .review(ratingDTO.getReview())
                    .build();
            
            RiderRating saved = riderRatingRepository.save(rating);
            
            log.info("Rider rating saved: {}, overall score: {}", orderId, overallScore);
            
            return convertToDTO(saved);
            
        } catch (Exception e) {
            log.error("Error rating rider: {}", e.getMessage());
            throw new RuntimeException("Rating failed: " + e.getMessage());
        }
    }
    
    @Override
    public List<RiderRatingDTO> getRiderRatings(Long riderId) {
        try {
            return riderRatingRepository.findByRiderId(riderId)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting rider ratings: {}", riderId, e);
            throw new RuntimeException("Failed to get ratings: " + e.getMessage());
        }
    }
    
    @Override
    public Double getRiderAverageRating(Long riderId) {
        try {
            Double average = riderRatingRepository.getAverageRatingForRider(riderId);
            return average != null ? average : 0.0;
        } catch (Exception e) {
            log.error("Error getting average rating for rider: {}", riderId, e);
            return 0.0;
        }
    }
    
    @Override
    public RiderRatingDTO getOrderRating(Long orderId) {
        try {
            RiderRating rating = riderRatingRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Rating not found for this order"));
            return convertToDTO(rating);
        } catch (Exception e) {
            log.error("Error getting order rating: {}", orderId, e);
            throw new RuntimeException("Failed to get rating: " + e.getMessage());
        }
    }
    
    private RiderRatingDTO convertToDTO(RiderRating rating) {
        return RiderRatingDTO.builder()
                .id(rating.getId())
                .riderId(rating.getRider().getId())
                .customerId(rating.getCustomer().getId())
                .orderId(rating.getOrder().getId())
                .politenessScore(rating.getPolitenessScore())
                .speedScore(rating.getSpeedScore())
                .overallScore(rating.getOverallScore())
                .review(rating.getReview())
                .build();
    }
}
