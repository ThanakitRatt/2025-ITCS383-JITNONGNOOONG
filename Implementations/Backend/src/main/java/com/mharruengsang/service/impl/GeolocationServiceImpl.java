package com.mharruengsang.service.impl;

import com.mharruengsang.entity.RiderLocation;
import com.mharruengsang.repository.RiderLocationRepository;
import com.mharruengsang.repository.RiderRatingRepository;
import com.mharruengsang.service.GeolocationService;
import com.mharruengsang.service.dto.NearbyRiderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeolocationServiceImpl implements GeolocationService {
    
    private final RiderLocationRepository riderLocationRepository;
    private final RiderRatingRepository riderRatingRepository;
    
    // Average rider speed in km/h
    private static final double AVERAGE_RIDER_SPEED = 20.0;
    
    public GeolocationServiceImpl(RiderLocationRepository riderLocationRepository,
                                 RiderRatingRepository riderRatingRepository) {
        this.riderLocationRepository = riderLocationRepository;
        this.riderRatingRepository = riderRatingRepository;
    }
    
    @Override
    public Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return 0.0;
        }
        
        // Haversine formula - more accurate for larger distances
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Earth's radius in kilometers
        double earthRadiusKm = 6371;
        
        return earthRadiusKm * c;
    }
    
    @Override
    public List<NearbyRiderInfo> findNearbyRiders(Double customerLat, Double customerLng, 
                                                   Double radiusKm, String riderStatus) {
        log.info("Finding nearby riders for location: {}, {}, within {} km", customerLat, customerLng, radiusKm);
        
        // Get all rider locations
        List<RiderLocation> allRiderLocations = riderLocationRepository.findAll();
        
        return allRiderLocations.stream()
                .filter(location -> {
                    // Filter by status if provided
                    if (riderStatus != null && !location.getStatus().name().equals(riderStatus)) {
                        return false;
                    }
                    
                    // Calculate distance
                    double distance = calculateDistance(
                            customerLat, customerLng,
                            location.getLatitude(), location.getLongitude()
                    );
                    
                    // Check if within radius
                    return distance <= radiusKm;
                })
                .map(location -> {
                    Double distance = calculateDistance(
                            customerLat, customerLng,
                            location.getLatitude(), location.getLongitude()
                    );
                    
                    Double estimatedArrival = (distance / AVERAGE_RIDER_SPEED) * 60; // Convert to minutes
                    
                    Double riderRating = riderRatingRepository.getAverageRatingForRider(
                            location.getRider().getId()
                    );
                    
                    return new NearbyRiderInfo(
                            location.getRider().getId(),
                            location.getRider().getName(),
                            location.getLatitude(),
                            location.getLongitude(),
                            distance,
                            estimatedArrival,
                            riderRating != null ? riderRating : 0.0
                    );
                })
                .sorted((r1, r2) -> Double.compare(r1.getDistanceKm(), r2.getDistanceKm()))
                .collect(Collectors.toList());
    }
}
