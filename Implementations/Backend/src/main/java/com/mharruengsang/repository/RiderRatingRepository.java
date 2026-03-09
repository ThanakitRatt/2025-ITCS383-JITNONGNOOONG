package com.mharruengsang.repository;

import com.mharruengsang.entity.RiderRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiderRatingRepository extends JpaRepository<RiderRating, Long> {
    List<RiderRating> findByRiderId(Long riderId);
    Optional<RiderRating> findByOrderId(Long orderId);
    
    @Query("SELECT AVG(r.overallScore) FROM RiderRating r WHERE r.rider.id = ?1")
    Double getAverageRatingForRider(Long riderId);
}
