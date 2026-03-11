package com.itcs383.order.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itcs383.common.dto.OrderDTO;
import com.itcs383.common.enums.OrderStatus;
import com.itcs383.order.dto.CreateOrderRequest;
import com.itcs383.order.dto.UpdateOrderStatusRequest;
import com.itcs383.order.service.OrderService;

import jakarta.validation.Valid;

/**
 * Order Controller - REST API endpoints for order management
 * Handles all order-related HTTP requests
 * 
 * API Endpoints:
 * POST   /orders                     - Create new order
 * GET    /orders/{id}                - Get order by ID
 * GET    /orders/number/{orderNumber} - Get order by number
 * PUT    /orders/{id}/status         - Update order status
 * DELETE /orders/{id}                - Cancel order
 * GET    /orders/customer/{id}       - Get customer orders
 * GET    /orders/restaurant/{id}     - Get restaurant orders
 */
@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    // Constants for response keys
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ERROR = "error";
    private static final String KEY_DATA = "data";
    
    // Constants for log messages
    private static final String LOG_ORDER_NOT_FOUND = "Order not found: {}";
    private static final String MSG_ORDER_NOT_FOUND_ID = "Order not found with ID: ";
    private static final String MSG_ORDER_CREATED = "Order created successfully";
    private static final String MSG_ORDER_UPDATED = "Order status updated successfully";
    private static final String MSG_ORDER_CANCELLED = "Order cancelled successfully";
    private static final String MSG_FAILED_CREATE = "Failed to create order";
    private static final String MSG_FAILED_FETCH = "Failed to fetch order";
    private static final String MSG_FAILED_UPDATE = "Failed to update order status";
    private static final String MSG_FAILED_CANCEL = "Failed to cancel order";
    private static final String MSG_FAILED_FETCH_ORDERS = "Failed to fetch orders";
    private static final String MSG_INVALID_STATUS = "Invalid status: ";

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create new order
     * POST /orders
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        logger.info("Creating order for customer {} at restaurant {}", 
                   request.getCustomerId(), request.getRestaurantId());
        
        try {
            OrderDTO order = orderService.createOrder(request);
            
            logger.info("Order created successfully: {}", order.getOrderNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                KEY_SUCCESS, true,
                KEY_MESSAGE, MSG_ORDER_CREATED,
                KEY_DATA, order
            ));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid order request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                KEY_SUCCESS, false,
                KEY_MESSAGE, e.getMessage()
            ));
            
        } catch (Exception e) {
            logger.error("Failed to create order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                KEY_SUCCESS, false,
                KEY_ERROR, MSG_FAILED_CREATE
            ));
        }
    }

    /**
     * Get order by ID
     * GET /orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long id) {
        logger.debug("Fetching order by ID: {}", id);
        
        try {
            OrderDTO order = orderService.getOrder(id);
            
            return ResponseEntity.ok(Map.of(
                KEY_SUCCESS, true,
                KEY_DATA, order
            ));
            
        } catch (RuntimeException e) {
            logger.warn(LOG_ORDER_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                KEY_SUCCESS, false,
                KEY_ERROR, MSG_ORDER_NOT_FOUND_ID + id
            ));
            
        } catch (Exception e) {
            logger.error("Failed to fetch order {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                KEY_SUCCESS, false,
                KEY_ERROR, MSG_FAILED_FETCH
            ));
        }
    }

    /**
     * Get order by order number
     * GET /orders/number/{orderNumber}
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderByNumber(@PathVariable String orderNumber) {
        logger.debug("Fetching order by number: {}", orderNumber);
        
        try {
            OrderDTO order = orderService.getOrderByNumber(orderNumber);
            
            return ResponseEntity.ok(Map.of(
                KEY_SUCCESS, true,
                KEY_DATA, order
            ));
            
        } catch (RuntimeException e) {
            logger.warn(LOG_ORDER_NOT_FOUND, orderNumber);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Failed to fetch order {}: {}", orderNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                KEY_SUCCESS, false,
                KEY_MESSAGE, MSG_FAILED_FETCH
            ));
        }
    }

    /**
     * Update order status
     * PUT /orders/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id,
                                              @Valid @RequestBody UpdateOrderStatusRequest request) {
        logger.info("Updating order {} status to {} by user {}", 
                   id, request.getNewStatus(), request.getUpdatedBy());
        
        try {
            OrderDTO order = orderService.updateOrderStatus(id, request);
            
            return ResponseEntity.ok(Map.of(
                KEY_SUCCESS, true,
                KEY_MESSAGE, MSG_ORDER_UPDATED,
                KEY_DATA, order
            ));
            
        } catch (IllegalStateException e) {
            logger.warn("Invalid status transition for order {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                KEY_SUCCESS, false,
                KEY_ERROR, e.getMessage()
            ));
            
        } catch (RuntimeException e) {
            logger.warn(LOG_ORDER_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                KEY_SUCCESS, false,
                KEY_ERROR, MSG_ORDER_NOT_FOUND_ID + id
            ));
            
        } catch (Exception e) {
            logger.error("Failed to update order {} status: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                KEY_SUCCESS, false,
                KEY_ERROR, MSG_FAILED_UPDATE
            ));
        }
    }

    /**
     * Cancel order
     * PUT /orders/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long id,
                                        @RequestParam Long userId,
                                        @RequestParam(required = false, defaultValue = "Cancelled by user") String reason) {
        logger.info("Cancelling order {} by user {}", id, userId);
        
        try {
            OrderDTO order = orderService.cancelOrder(id, userId, reason);
            
            return ResponseEntity.ok(Map.of(
                KEY_SUCCESS, true,
                KEY_MESSAGE, MSG_ORDER_CANCELLED,
                KEY_DATA, order
            ));
            
        } catch (IllegalStateException e) {
            logger.warn("Cannot cancel order {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                KEY_SUCCESS, false,
                KEY_MESSAGE, e.getMessage()
            ));
            
        } catch (RuntimeException e) {
            logger.warn(LOG_ORDER_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                KEY_SUCCESS, false,
                KEY_ERROR, MSG_ORDER_NOT_FOUND_ID + id
            ));
            
        } catch (Exception e) {
            logger.error("Failed to cancel order {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                KEY_SUCCESS, false,
                KEY_ERROR, MSG_FAILED_CANCEL
            ));
        }
    }

    /**
     * Cancel order (DELETE version for backwards compatibility)
     * DELETE /orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> cancelOrderDelete(@PathVariable Long id,
                                        @RequestParam Long userId,
                                        @RequestParam(required = false, defaultValue = "Cancelled by user") String reason) {
        return cancelOrder(id, userId, reason);
    }

    /**
     * Get customer orders with pagination
     * GET /orders/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomerOrders(@PathVariable Long customerId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) String status) {
        logger.debug("Fetching orders for customer {} (page={}, size={}, status={})", 
                    customerId, page, size, status);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<OrderDTO> orders;
            
            if (status != null && !status.isEmpty()) {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.getCustomerOrdersByStatus(customerId, orderStatus, pageable);
            } else {
                orders = orderService.getCustomerOrders(customerId, pageable);
            }
            
            return ResponseEntity.ok(Map.of(
                KEY_SUCCESS, true,
                KEY_DATA, Map.of(
                    "content", orders.getContent(),
                    "page", orders.getNumber(),
                    "size", orders.getSize(),
                    "totalElements", orders.getTotalElements(),
                    "totalPages", orders.getTotalPages(),
                    "isFirst", orders.isFirst(),
                    "isLast", orders.isLast()
                )
            ));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid status parameter: {}", status);
            return ResponseEntity.badRequest().body(Map.of(
                KEY_SUCCESS, false,
                KEY_MESSAGE, MSG_INVALID_STATUS + status
            ));
            
        } catch (Exception e) {
            logger.error("Failed to fetch customer {} orders: {}", customerId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                KEY_SUCCESS, false,
                KEY_MESSAGE, MSG_FAILED_FETCH_ORDERS
            ));
        }
    }

    /**
     * Get restaurant orders with pagination
     * GET /orders/restaurant/{restaurantId}
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Map<String, Object>> getRestaurantOrders(@PathVariable Long restaurantId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) String status) {
        logger.debug("Fetching orders for restaurant {} (page={}, size={}, status={})", 
                    restaurantId, page, size, status);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
            Page<OrderDTO> orders;
            
            if (status != null && !status.isEmpty()) {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.getRestaurantOrdersByStatus(restaurantId, orderStatus, pageable);
            } else {
                orders = orderService.getRestaurantOrders(restaurantId, pageable);
            }
            
            return ResponseEntity.ok(Map.of(
                KEY_SUCCESS, true,
                KEY_DATA, Map.of(
                    "content", orders.getContent(),
                    "page", orders.getNumber(),
                    "size", orders.getSize(),
                    "totalElements", orders.getTotalElements(),
                    "totalPages", orders.getTotalPages(),
                    "isFirst", orders.isFirst(),
                    "isLast", orders.isLast()
                )
            ));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid status parameter: {}", status);
            return ResponseEntity.badRequest().body(Map.of(
                KEY_SUCCESS, false,
                KEY_MESSAGE, MSG_INVALID_STATUS + status
            ));
            
        } catch (Exception e) {
            logger.error("Failed to fetch restaurant {} orders: {}", restaurantId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                KEY_SUCCESS, false,
                KEY_MESSAGE, MSG_FAILED_FETCH_ORDERS
            ));
        }
    }

    /**
     * Health check endpoint
     * GET /orders/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Order Service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
