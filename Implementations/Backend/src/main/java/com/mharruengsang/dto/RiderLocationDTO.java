package com.mharruengsang.dto;

import com.mharruengsang.entity.RiderLocation;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderLocationDTO {
    private Long riderId;
    private Double latitude;
    private Double longitude;
    private String status;
    private Double distanceToOrder; // Distance in km to pickup location
    
    public static RiderLocationDTO fromEntity(RiderLocation location) {
        return RiderLocationDTO.builder()
                .riderId(location.getRider().getId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .status(location.getStatus().name())
                .build();
    }
}
