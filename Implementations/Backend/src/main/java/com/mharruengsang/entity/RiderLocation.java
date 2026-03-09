package com.mharruengsang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rider_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderLocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RiderStatus status = RiderStatus.AVAILABLE; // AVAILABLE, ON_DELIVERY, OFFLINE
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum RiderStatus {
        AVAILABLE, ON_DELIVERY, OFFLINE, ON_BREAK
    }
}
