package com.mharruengsang.repository;

import com.mharruengsang.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByRestaurantId(Long restaurantId);
    List<Order> findByRiderId(Long riderId);
    List<Order> findByStatus(Order.OrderStatus status);
    Optional<Order> findByIdAndCustomerId(Long orderId, Long customerId);
}
