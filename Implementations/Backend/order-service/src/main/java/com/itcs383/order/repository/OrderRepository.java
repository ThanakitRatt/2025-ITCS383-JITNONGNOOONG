package com.itcs383.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.itcs383.common.enums.OrderStatus;
import com.itcs383.order.entity.Order;

/**
 * Order Repository - Data access layer for Order entity
 * Optimized for 10M users with efficient queries and indexing
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find by order number (unique identifier)
    Optional<Order> findByOrderNumber(String orderNumber);

    // Customer-related queries (for customer order history)
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId ORDER BY o.createdAt DESC")
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(@Param("customerId") Long customerId, 
                                                             @Param("status") OrderStatus status, 
                                                             Pageable pageable);

    // Restaurant-related queries (for restaurant order management)
    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId ORDER BY o.createdAt DESC")
    Page<Order> findByRestaurantIdOrderByCreatedAtDesc(@Param("restaurantId") Long restaurantId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.status = :status ORDER BY o.createdAt ASC")
    Page<Order> findByRestaurantIdAndStatusOrderByCreatedAtAsc(@Param("restaurantId") Long restaurantId, 
                                                              @Param("status") OrderStatus status, 
                                                              Pageable pageable);

    // Rider-related queries (for delivery management)
    @Query("SELECT o FROM Order o WHERE o.riderId = :riderId ORDER BY o.createdAt DESC")
    Page<Order> findByRiderIdOrderByCreatedAtDesc(@Param("riderId") Long riderId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.riderId = :riderId AND o.status IN :statuses ORDER BY o.createdAt ASC")
    List<Order> findByRiderIdAndStatusIn(@Param("riderId") Long riderId, @Param("statuses") List<OrderStatus> statuses);

    // Find available orders for riders (READY_FOR_PICKUP status and no assigned rider)
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.riderId IS NULL ORDER BY o.createdAt ASC")
    Page<Order> findAvailableOrdersForRiders(@Param("status") OrderStatus status, Pageable pageable);

    // Status-based queries (for order processing)
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt ASC")
    Page<Order> findByStatusOrderByCreatedAtAsc(@Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt ASC")
    Page<Order> findByStatusInOrderByCreatedAtAsc(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);

    // Time-based queries (for analytics and monitoring)
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    Page<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate, 
                                                          Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByRestaurantIdAndCreatedAtBetween(@Param("restaurantId") Long restaurantId, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);

    // Count queries for dashboard metrics
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId")
    Long countByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurantId = :restaurantId AND o.status = :status")
    Long countByRestaurantIdAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :since AND o.status = :status")
    Long countByStatusSince(@Param("status") OrderStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt >= :since AND o.status = :status")
    java.math.BigDecimal sumTotalAmountByStatusSince(@Param("status") OrderStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :since")
    Long countAllSince(@Param("since") LocalDateTime since);

    // Advanced queries for business intelligence
    @Query("SELECT o FROM Order o WHERE o.estimatedDeliveryTime < :currentTime AND o.status IN :activeStatuses")
    List<Order> findDelayedOrders(@Param("currentTime") LocalDateTime currentTime, 
                                 @Param("activeStatuses") List<OrderStatus> activeStatuses);

    @Query("SELECT o.restaurantId, COUNT(o), AVG(o.totalAmount) FROM Order o " +
           "WHERE o.createdAt >= :since GROUP BY o.restaurantId ORDER BY COUNT(o) DESC")
    List<Object[]> findRestaurantOrderStats(@Param("since") LocalDateTime since);

    // Location-based queries (for nearby orders)
    @Query("SELECT o FROM Order o WHERE o.deliveryLatitude BETWEEN :minLat AND :maxLat " +
           "AND o.deliveryLongitude BETWEEN :minLon AND :maxLon AND o.status = :status")
    List<Order> findOrdersInArea(@Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
                                @Param("minLon") Double minLon, @Param("maxLon") Double maxLon,
                                @Param("status") OrderStatus status);

    // Additional queries for test support
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCustomerIdAndCreatedAtBetween(@Param("customerId") Long customerId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :dateTime")
    List<Order> findByStatusAndCreatedAtBefore(@Param("status") OrderStatus status,
                                              @Param("dateTime") LocalDateTime dateTime);

    // Optimized exists queries for validation
    boolean existsByOrderNumber(String orderNumber);
    
    boolean existsByCustomerIdAndRestaurantIdAndStatusIn(Long customerId, Long restaurantId, 
                                                        List<OrderStatus> statuses);
}
