package com.mharruengsang.service;

import com.mharruengsang.dto.RiderLocationDTO;
import com.mharruengsang.service.dto.NearbyRiderInfo;

import java.util.List;

public interface RiderService {
    
    /**
     * Update rider location (GPS tracking)
     */
    RiderLocationDTO updateRiderLocation(Long riderId, Double latitude, Double longitude, String status);
    
    /**
     * Get rider's current location
     */
    RiderLocationDTO getRiderLocation(Long riderId);
    
    /**
     * Accept an order for delivery
     */
    void acceptOrder(Long riderId, Long orderId);
    
    /**
     * Confirm delivery completion - ride can rate rider after this
     */
    void confirmDelivery(Long riderId, Long orderId);
    
    /**
     * Get available orders near rider's location
     */
    List<NearbyRiderInfo> getNearbyAvailableOrders(Long riderId);
}
