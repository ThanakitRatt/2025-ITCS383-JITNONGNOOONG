package com.mharruengsang.service;

import com.mharruengsang.service.dto.NearbyRiderInfo;
import java.util.List;

public interface GeolocationService {
    
    /**
     * Calculate distance between two coordinates in kilometers
     */
    Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2);
    
    /**
     * Find nearby riders within specified radius
     */
    List<NearbyRiderInfo> findNearbyRiders(Double customerLat, Double customerLng, Double radiusKm, String riderStatus);
}
