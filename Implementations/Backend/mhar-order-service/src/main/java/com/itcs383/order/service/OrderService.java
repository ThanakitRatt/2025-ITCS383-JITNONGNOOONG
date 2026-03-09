package com.itcs383.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itcs383.common.dto.OrderDTO;
import com.itcs383.common.enums.OrderStatus;
import com.itcs383.order.dto.CreateOrderRequest;
import com.itcs383.order.dto.UpdateOrderStatusRequest;
import com.itcs383.order.entity.Order;
import com.itcs383.order.entity.OrderItem;
import com.itcs383.order.repository.OrderRepository;

/**
 * Order Service - Core business logic for order management
 * Handles order lifecycle: creation, status updates, tracking
 * 
 * Designed for 10M concurrent users with caching and optimization
 */
@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    // TODO: Add RestaurantServiceClient when Restaurant Service is ready
    // TODO: Add PaymentServiceClient when Payment Service is ready

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Create a new order with validation and business rules
     */
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        logger.info("Creating order for customer {} at restaurant {}", 
                   request.getCustomerId(), request.getRestaurantId());

        try {
            // Validate business rules
            validateCreateOrderRequest(request);

            // Generate unique order number
            String orderNumber = generateOrderNumber();

            // Calculate totals
            BigDecimal itemsTotal = calculateItemsTotal(request.getOrderItems());
            BigDecimal deliveryFee = calculateDeliveryFee(request.getRestaurantId(), itemsTotal);
            BigDecimal totalAmount = itemsTotal.add(deliveryFee);

            // Create order entity
            Order order = new Order(
                orderNumber,
                request.getCustomerId(),
                request.getRestaurantId(),
                totalAmount,
                request.getDeliveryAddress()
            );

            order.setDeliveryFee(deliveryFee);
            order.setDeliveryLatitude(request.getDeliveryLatitude());
            order.setDeliveryLongitude(request.getDeliveryLongitude());
            order.setSpecialInstructions(request.getSpecialInstructions());

            // Add order items
            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
                OrderItem orderItem = new OrderItem(
                    itemRequest.getMenuItemId(),
                    itemRequest.getMenuItemName(),
                    itemRequest.getQuantity(),
                    itemRequest.getUnitPrice(),
                    itemRequest.getSpecialRequests()
                );
                order.addOrderItem(orderItem);
            }

            // Save order
            Order savedOrder = orderRepository.save(order);

            // TODO: Notify restaurant service
            // TODO: Reserve payment with payment service

            logger.info("Order created successfully: {}", savedOrder.getOrderNumber());
            return convertToDTO(savedOrder);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid order request for customer {}: {}", 
                        request.getCustomerId(), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Failed to create order for customer {}: {}", 
                        request.getCustomerId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating order for customer {}: {}", 
                        request.getCustomerId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    /**
     * Get order by ID with caching
     */
    @Cacheable(value = "orders", key = "#orderId")
    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId) {
        logger.debug("Fetching order by ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        return convertToDTO(order);
    }

    /**
     * Get order by order number with caching
     */
    @Cacheable(value = "orders", key = "#orderNumber")
    @Transactional(readOnly = true)
    public OrderDTO getOrderByNumber(String orderNumber) {
        logger.debug("Fetching order by number: {}", orderNumber);
        
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        
        return convertToDTO(order);
    }

    /**
     * Update order status with business validation
     */
    @CacheEvict(value = "orders", key = "#orderId")
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        logger.info("Updating order {} status to {} by user {}", 
                   orderId, request.getNewStatus(), request.getUpdatedBy());

        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

            // Validate status transition
            if (!order.canUpdateStatus(request.getNewStatus())) {
                throw new IllegalStateException(
                    String.format("Cannot update order status from %s to %s", 
                                 order.getStatus(), request.getNewStatus()));
            }

            // Update status with history tracking
            order.updateStatus(request.getNewStatus(), request.getUpdatedBy(), request.getNotes());

            // Save updated order
            Order updatedOrder = orderRepository.save(order);

            // TODO: Send notifications based on status
            // TODO: Update payment status if needed
            // TODO: Notify rider service for pickup/delivery

            logger.info("Order {} status updated to {}", orderId, request.getNewStatus());
            return convertToDTO(updatedOrder);

        } catch (IllegalStateException e) {
            logger.error("Invalid status transition for order {}: {}", orderId, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Failed to update order {} status: {}", orderId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating order {} status: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to update order status: " + e.getMessage(), e);
        }
    }

    /**
     * Get customer orders with pagination and caching
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getCustomerOrders(Long customerId, Pageable pageable) {
        logger.debug("Fetching orders for customer: {}", customerId);
        
        Page<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        return orders.map(this::convertToDTO);
    }

    /**
     * Get customer orders by status
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getCustomerOrdersByStatus(Long customerId, OrderStatus status, Pageable pageable) {
        logger.debug("Fetching orders for customer {} with status {}", customerId, status);
        
        Page<Order> orders = orderRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
            customerId, status, pageable);
        return orders.map(this::convertToDTO);
    }

    /**
     * Get restaurant orders with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getRestaurantOrders(Long restaurantId, Pageable pageable) {
        logger.debug("Fetching orders for restaurant: {}", restaurantId);
        
        Page<Order> orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable);
        return orders.map(this::convertToDTO);
    }

    /**
     * Get restaurant orders by status (for restaurant management)
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getRestaurantOrdersByStatus(Long restaurantId, OrderStatus status, Pageable pageable) {
        logger.debug("Fetching orders for restaurant {} with status {}", restaurantId, status);
        
        Page<Order> orders = orderRepository.findByRestaurantIdAndStatusOrderByCreatedAtAsc(
            restaurantId, status, pageable);
        return orders.map(this::convertToDTO);
    }

    /**
     * Cancel order with validation
     */
    @CacheEvict(value = "orders", key = "#orderId")
    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long userId, String reason) {
        logger.info("Cancelling order {} by user {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate cancellation
        if (!canCancelOrder(order)) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        // Update to cancelled status
        order.updateStatus(OrderStatus.CANCELLED, userId, "Cancelled: " + reason);
        Order cancelledOrder = orderRepository.save(order);

        // TODO: Process refund if payment was made
        // TODO: Notify restaurant and customer

        logger.info("Order {} cancelled successfully", orderId);
        return convertToDTO(cancelledOrder);
    }

    // Private helper methods

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        // TODO: Validate restaurant exists and is open
        // TODO: Validate menu items exist and are available
        // TODO: Check delivery area coverage
        
        if (request.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Validate order total is within reasonable limits
        BigDecimal total = calculateItemsTotal(request.getOrderItems());
        if (total.compareTo(new BigDecimal("10000")) > 0) {
            throw new IllegalArgumentException("Order total exceeds maximum limit");
        }
    }

    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "MR" + timestamp.substring(timestamp.length() - 8) + random;
    }

    private BigDecimal calculateItemsTotal(List<CreateOrderRequest.OrderItemRequest> items) {
        return items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDeliveryFee(Long restaurantId, BigDecimal itemsTotal) {
        // TODO: Implement complex delivery fee calculation based on:
        // - Distance between restaurant and delivery address
        // - Restaurant delivery fee settings (using restaurantId)
        // - Time of day (surge pricing)
        // - Order total (free delivery threshold)
        
        // Simple implementation for now - restaurantId will be used in future implementation
        BigDecimal freeDeliveryThreshold = new BigDecimal("300.00");
        if (itemsTotal.compareTo(freeDeliveryThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("35.00"); // Default delivery fee
    }

    private boolean canCancelOrder(Order order) {
        return switch (order.getStatus()) {
            case PENDING, CONFIRMED -> true;
            case PREPARING -> {
                // Can cancel if order was placed less than 5 minutes ago
                LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
                yield order.getCreatedAt().isAfter(fiveMinutesAgo);
            }
            default -> false;
        };
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO(
            order.getId(),
            order.getOrderNumber(),
            order.getCustomerId(),
            order.getRestaurantId(),
            order.getStatus().name(),
            order.getTotalAmount(),
            order.getDeliveryAddress()
        );

        dto.setRiderId(order.getRiderId());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setDeliveryLatitude(order.getDeliveryLatitude());
        dto.setDeliveryLongitude(order.getDeliveryLongitude());
        dto.setSpecialInstructions(order.getSpecialInstructions());
        dto.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        return dto;
    }
}
