package com.mharruengsang.controller;

import com.mharruengsang.dto.ApiResponse;
import com.mharruengsang.dto.RiderLocationDTO;
import com.mharruengsang.service.RiderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Rider Services
 * 
 * BASE URL: /api/riders
 */
@RestController
@RequestMapping("/riders")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:4000", "http://localhost:3000"})
@Slf4j
public class RiderController {
    
    private final RiderService riderService;
    
    public RiderController(RiderService riderService) {
        this.riderService = riderService;
    }
    
    /**
     * POST /riders/:riderId/location
     * Update rider's GPS location (real-time tracking)
     * 
     * Request body:
     * {
     *   "latitude": 13.7563,
     *   "longitude": 100.5018,
     *   "status": "AVAILABLE" // or ON_DELIVERY, OFFLINE
     * }
     */
    @PostMapping("/{riderId}/location")
    public ResponseEntity<ApiResponse<RiderLocationDTO>> updateLocation(
            @PathVariable Long riderId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String status) {
        try {
            log.info("Updating rider {} location to ({}, {}) - Status: {}", 
                    riderId, latitude, longitude, status);
            
            RiderLocationDTO location = riderService.updateRiderLocation(riderId, latitude, longitude, status);
            
            return ResponseEntity.ok(ApiResponse.ok(location, "Location updated"));
            
        } catch (Exception e) {
            log.error("Error updating rider location: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Location update failed: " + e.getMessage()));
        }
    }
    
    /**
     * GET /riders/:riderId/location
     * Get rider's current location
     */
    @GetMapping("/{riderId}/location")
    public ResponseEntity<ApiResponse<RiderLocationDTO>> getLocation(
            @PathVariable Long riderId) {
        try {
            RiderLocationDTO location = riderService.getRiderLocation(riderId);
            return ResponseEntity.ok(ApiResponse.ok(location));
            
        } catch (Exception e) {
            log.error("Error getting rider location: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get location: " + e.getMessage()));
        }
    }
    
    /**
     * POST /riders/:riderId/accept-order/:orderId
     * Rider accepts an available order
     */
    @PostMapping("/{riderId}/accept-order/{orderId}")
    public ResponseEntity<ApiResponse<String>> acceptOrder(
            @PathVariable Long riderId,
            @PathVariable Long orderId) {
        try {
            log.info("Rider {} accepting order {}", riderId, orderId);
            
            riderService.acceptOrder(riderId, orderId);
            
            return ResponseEntity.ok(ApiResponse.ok("Order accepted successfully"));
            
        } catch (Exception e) {
            log.error("Error accepting order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Order acceptance failed: " + e.getMessage()));
        }
    }
    
    /**
     * POST /riders/:riderId/confirm-delivery/:orderId
     * Rider confirms delivery completion
     * After this, customer can rate the rider
     */
    @PostMapping("/{riderId}/confirm-delivery/{orderId}")
    public ResponseEntity<ApiResponse<String>> confirmDelivery(
            @PathVariable Long riderId,
            @PathVariable Long orderId) {
        try {
            log.info("Rider {} confirming delivery for order {}", riderId, orderId);
            
            riderService.confirmDelivery(riderId, orderId);
            
            return ResponseEntity.ok(ApiResponse.ok("Delivery confirmed"));
            
        } catch (Exception e) {
            log.error("Error confirming delivery: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Delivery confirmation failed: " + e.getMessage()));
        }
    }
}
