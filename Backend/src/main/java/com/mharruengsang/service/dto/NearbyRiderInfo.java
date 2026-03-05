package com.mharruengsang.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyRiderInfo {
    private Long riderId;
    private String riderName;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private Double estimatedArrivalMinutes;
    private Double riderRating;
}
