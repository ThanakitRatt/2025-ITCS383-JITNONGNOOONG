package com.mharruengsang.service.impl;

import com.mharruengsang.dto.RiderLocationDTO;
import com.mharruengsang.entity.Order;
import com.mharruengsang.entity.RiderLocation;
import com.mharruengsang.entity.User;
import com.mharruengsang.repository.OrderRepository;
import com.mharruengsang.repository.RiderLocationRepository;
import com.mharruengsang.repository.UserRepository;
import com.mharruengsang.service.RiderService;
import com.mharruengsang.service.GeolocationService;
import com.mharruengsang.service.dto.NearbyRiderInfo;
import com.mharruengsang.service.OrderEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
public class RiderServiceImpl implements RiderService {
    
    private final RiderLocationRepository riderLocationRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final GeolocationService geolocationService;
    private final OrderEventPublisher orderEventPublisher;
    
    public RiderServiceImpl(RiderLocationRepository riderLocationRepository,
                          UserRepository userRepository,
                          OrderRepository orderRepository,
                          GeolocationService geolocationService,
                          OrderEventPublisher orderEventPublisher) {
        this.riderLocationRepository = riderLocationRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.geolocationService = geolocationService;
        this.orderEventPublisher = orderEventPublisher;
    }
    
    @Override
    public RiderLocationDTO updateRiderLocation(Long riderId, Double latitude, Double longitude, String status) {
        log.info("Updating rider location - ID: {}, lat: {}, lng: {}, status: {}", riderId, latitude, longitude, status);
        
        try {
            User rider = userRepository.findById(riderId)
                    .orElseThrow(() -> new RuntimeException("Rider not found"));
            
            // Update user location
            rider.setLatitude(latitude);
            rider.setLongitude(longitude);
            userRepository.save(rider);
            
            // Update rider location tracking
            RiderLocation riderLocation = riderLocationRepository.findByRiderId(riderId)
                    .orElse(new RiderLocation());
            
            riderLocation.setRider(rider);
            riderLocation.setLatitude(latitude);
            riderLocation.setLongitude(longitude);
            riderLocation.setStatus(RiderLocation.RiderStatus.valueOf(status));
            riderLocation.setUpdatedAt(LocalDateTime.now());
            
            RiderLocation saved = riderLocationRepository.save(riderLocation);
            
            return RiderLocationDTO.fromEntity(saved);
            
        } catch (Exception e) {
            log.error("Error updating rider location: {}", riderId, e);
            throw new RuntimeException("Failed to update location: " + e.getMessage());
        }
    }
    
    @Override
    public RiderLocationDTO getRiderLocation(Long riderId) {
        try {
            RiderLocation location = riderLocationRepository.findByRiderId(riderId)
                    .orElseThrow(() -> new RuntimeException("Rider location not found"));
            return RiderLocationDTO.fromEntity(location);
        } catch (Exception e) {
            log.error("Error getting rider location: {}", riderId, e);
            throw new RuntimeException("Failed to get location: " + e.getMessage());
        }
    }
    
    @Override
    @Async
    public void acceptOrder(Long riderId, Long orderId) {
        log.info("Rider {} accepting order {}", riderId, orderId);
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            User rider = userRepository.findById(riderId)
                    .orElseThrow(() -> new RuntimeException("Rider not found"));
            
            // Assign rider to order
            order.setRider(rider);
            order.setDeliveryStatus(Order.DeliveryStatus.PICKED_UP);
            order.setUpdatedAt(LocalDateTime.now());
            
            orderRepository.save(order);
            
            // Publish event
            orderEventPublisher.publishDeliveryAssignedEvent(order, riderId);
            
            log.info("Order {} assigned to rider {}", orderId, riderId);
            
        } catch (Exception e) {
            log.error("Error accepting order: {} for rider: {}", orderId, riderId, e);
        }
    }
    
    @Override
    public void confirmDelivery(Long riderId, Long orderId) {
        log.info("Rider {} confirming delivery for order {}", riderId, orderId);
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            if (!order.getRider().getId().equals(riderId)) {
                throw new RuntimeException("This rider is not assigned to this order");
            }
            
            order.setDeliveryStatus(Order.DeliveryStatus.DELIVERED);
            order.setStatus(Order.OrderStatus.DELIVERED);
            order.setDeliveredAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            
            orderRepository.save(order);
            
            // Update rider status to AVAILABLE
            RiderLocation riderLocation = riderLocationRepository.findByRiderId(riderId)
                    .orElseThrow(() -> new RuntimeException("Rider location not found"));
            
            riderLocation.setStatus(RiderLocation.RiderStatus.AVAILABLE);
            riderLocation.setUpdatedAt(LocalDateTime.now());
            riderLocationRepository.save(riderLocation);
            
            // Publish event
            orderEventPublisher.publishOrderDeliveredEvent(order);
            
            log.info("Delivery confirmed for order {} by rider {}", orderId, riderId);
            
        } catch (Exception e) {
            log.error("Error confirming delivery: {} for rider: {}", orderId, riderId, e);
            throw new RuntimeException("Delivery confirmation failed: " + e.getMessage());
        }
    }
    
    @Override
    public List<NearbyRiderInfo> getNearbyAvailableOrders(Long riderId) {
        // This would return orders near the rider's location
        // Implementation depends on order service
        return null;
    }
}
