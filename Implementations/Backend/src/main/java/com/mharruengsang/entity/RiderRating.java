package com.mharruengsang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rider_ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderRating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "politeness_score", nullable = false)
    private Integer politenessScore; // 1-5
    
    @Column(name = "speed_score", nullable = false)
    private Integer speedScore; // 1-5
    
    @Column(name = "overall_score", nullable = false)
    private Double overallScore; // Average of politeness and speed
    
    @Column(length = 1000)
    private String review;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
