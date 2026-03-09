package com.mharruengsang.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderRatingDTO {
    private Long id;
    private Long riderId;
    private Long customerId;
    private Long orderId;
    private Integer politenessScore; // 1-5
    private Integer speedScore; // 1-5
    private Double overallScore;
    private String review;
}
