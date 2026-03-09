package com.mharruengsang.repository;

import com.mharruengsang.entity.RiderLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderLocationRepository extends JpaRepository<RiderLocation, Long> {
    Optional<RiderLocation> findByRiderId(Long riderId);
}
