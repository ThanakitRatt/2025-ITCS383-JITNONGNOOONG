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
import com.itcs383.common.exception.OrderStatusException;
import com.itcs383.common.exception.OrderValidationException;
import com.itcs383.common.exception.ResourceNotFoundException;
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
    private static final String RESOURCE_ORDER = "Order";

    private final OrderRepository orderRepository;
    private final RiderPaymentService riderPaymentService;

    @Autowired
    public OrderService(OrderRepository orderRepository, RiderPaymentService riderPaymentService) {
        this.orderRepository = orderRepository;
        this.riderPaymentService = riderPaymentService;
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
            BigDecimal deliveryFee = calculateDeliveryFee(itemsTotal);
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

            logger.info("Order created successfully: {}", savedOrder.getOrderNumber());
            return convertToDTO(savedOrder);

        } catch (IllegalArgumentException e) {
            throw new OrderValidationException(
                "Invalid order request for customer " + request.getCustomerId() + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new OrderValidationException(
                "Failed to create order for customer " + request.getCustomerId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get order by ID with caching
     */
    @SuppressWarnings("null")
    @Cacheable(value = "orders", key = "#orderId")
    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId) {
        logger.debug("Fetching order by ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_ORDER, orderId));
        
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
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_ORDER, orderNumber));
        
        return convertToDTO(order);
    }

    /**
     * Update order status with business validation
     */
    @SuppressWarnings("null")
    @CacheEvict(value = "orders", key = "#orderId")
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        logger.info("Updating order {} status to {} by user {}", 
                   orderId, request.getNewStatus(), request.getUpdatedBy());

        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_ORDER, orderId));

            // Validate status transition
            if (!order.canUpdateStatus(request.getNewStatus())) {
                throw new OrderStatusException(order.getStatus(), request.getNewStatus());
            }

            // Assign rider when they pick up the order
            if (request.getNewStatus() == OrderStatus.PICKED_UP && order.getRiderId() == null) {
                order.setRiderId(request.getUpdatedBy());
            }

            // Update status with history tracking
            order.updateStatus(request.getNewStatus(), request.getUpdatedBy(), request.getNotes());

            // Save updated order
            Order updatedOrder = orderRepository.save(order);

            // Credit rider earnings when order is delivered
            if (request.getNewStatus() == OrderStatus.DELIVERED && updatedOrder.getRiderId() != null) {
                boolean paymentSuccess = riderPaymentService.creditRiderEarnings(
                    updatedOrder.getRiderId(), 
                    updatedOrder.getDeliveryFee()
                );
                if (paymentSuccess) {
                    logger.info("Rider {} credited with delivery fee {} for order {}", 
                               updatedOrder.getRiderId(), updatedOrder.getDeliveryFee(), orderId);
                } else {
                    logger.warn("Failed to credit rider {} for order {}", 
                               updatedOrder.getRiderId(), orderId);
                }
            }

            logger.info("Order {} status updated to {}", orderId, request.getNewStatus());
            return convertToDTO(updatedOrder);

        } catch (OrderStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderStatusException(
                "Failed to update order " + orderId + " status: " + e.getMessage(), e);
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
     * Get orders assigned to a specific rider
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getRiderOrders(Long riderId, Pageable pageable) {
        logger.debug("Fetching orders for rider: {}", riderId);
        Page<Order> orders = orderRepository.findByRiderIdOrderByCreatedAtDesc(riderId, pageable);
        return orders.map(this::convertToDTO);
    }

    /**
     * Get available orders for riders (READY_FOR_PICKUP and no assigned rider)
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAvailableOrdersForRiders(Pageable pageable) {
        logger.debug("Fetching available orders for riders");
        
        Page<Order> orders = orderRepository.findAvailableOrdersForRiders(
            OrderStatus.READY_FOR_PICKUP, pageable);
        return orders.map(this::convertToDTO);
    }

    /**
     * Cancel order with validation
     */
    @SuppressWarnings("null")
    @CacheEvict(value = "orders", key = "#orderId")
    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long userId, String reason) {
        logger.info("Cancelling order {} by user {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_ORDER, orderId));

        // Validate cancellation
        if (!canCancelOrder(order)) {
            throw new OrderStatusException(
                "Order " + orderId + " cannot be cancelled in current status: " + order.getStatus());
        }

        // Update to cancelled status
        order.setCancellationReason(reason);
        order.updateStatus(OrderStatus.CANCELLED, userId, "Cancelled: " + reason);
        Order cancelledOrder = orderRepository.save(order);

        logger.info("Order {} cancelled successfully", orderId);
        return convertToDTO(cancelledOrder);
    }

    /**
     * Get admin statistics for the dashboard
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getAdminStats() {
        LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
        LocalDateTime monthStart = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long todayOrderCount = orderRepository.countAllSince(todayStart);
        long monthOrderCount = orderRepository.countAllSince(monthStart);
        long totalOrderCount = orderRepository.count();

        BigDecimal todayRevenue = orderRepository.sumTotalAmountByStatusSince(OrderStatus.DELIVERED, todayStart);
        BigDecimal monthRevenue = orderRepository.sumTotalAmountByStatusSince(OrderStatus.DELIVERED, monthStart);

        // Platform commission is 10%
        BigDecimal commissionRate = new BigDecimal("0.10");

        return java.util.Map.of(
            "todayOrders", todayOrderCount,
            "monthOrders", monthOrderCount,
            "totalOrders", totalOrderCount,
            "todayRevenue", todayRevenue.multiply(commissionRate),
            "monthRevenue", monthRevenue.multiply(commissionRate)
        );
    }

    // Private helper methods

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        
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

    private BigDecimal calculateDeliveryFee(BigDecimal itemsTotal) {
        
        // Simple implementation for now
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
