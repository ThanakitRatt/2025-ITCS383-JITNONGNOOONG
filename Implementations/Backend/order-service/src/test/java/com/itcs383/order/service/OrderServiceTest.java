package com.itcs383.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.itcs383.common.dto.OrderDTO;
import com.itcs383.common.enums.OrderStatus;
import com.itcs383.common.exception.OrderStatusException;
import com.itcs383.common.exception.ResourceNotFoundException;
import com.itcs383.order.dto.CreateOrderRequest;
import com.itcs383.order.dto.UpdateOrderStatusRequest;
import com.itcs383.order.entity.Order;
import com.itcs383.order.entity.OrderItem;
import com.itcs383.order.repository.OrderRepository;

/**
 * Comprehensive Unit Tests for OrderService
 * Tests all business logic, validation, and edge cases
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest validCreateRequest;
    private Order validOrder;
    // OrderDTO will be created in individual test methods as needed

    @BeforeEach
    void setUp() {
        // Set up valid test data
        validCreateRequest = createValidOrderRequest();
        validOrder = createValidOrder();
    }

    @Test
    void createOrder_WithValidRequest_ShouldCreateOrderSuccessfully() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder);

        // When
        OrderDTO result = orderService.createOrder(validCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals(validOrder.getId(), result.getId());
        assertEquals(validOrder.getCustomerId(), result.getCustomerId());
        assertEquals(validOrder.getRestaurantId(), result.getRestaurantId());
        assertEquals(validOrder.getTotalAmount(), result.getTotalAmount());
        assertEquals(OrderStatus.PENDING.name(), result.getStatus());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_WithEmptyOrderItems_ShouldThrowException() {
        // Given
        CreateOrderRequest requestWithEmptyItems = createValidOrderRequest();
        requestWithEmptyItems.getOrderItems().clear();

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.createOrder(requestWithEmptyItems));
        
        assertTrue(exception.getMessage().contains("Order must contain at least one item"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithExcessiveTotal_ShouldThrowException() {
        // Given
        CreateOrderRequest expensiveRequest = createValidOrderRequest();
        expensiveRequest.getOrderItems().get(0).setUnitPrice(new BigDecimal("15000.00"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.createOrder(expensiveRequest));
        
        assertTrue(exception.getMessage().contains("Order total exceeds maximum limit"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrder_WithValidId_ShouldReturnOrder() {
        // Given
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(validOrder));

        // When
        OrderDTO result = orderService.getOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(validOrder.getId(), result.getId());
        assertEquals(validOrder.getOrderNumber(), result.getOrderNumber());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrder_WithInvalidId_ShouldThrowException() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.getOrder(orderId));
        
        assertTrue(exception.getMessage().contains("Order not found"));
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderByNumber_WithValidNumber_ShouldReturnOrder() {
        // Given
        String orderNumber = "MR12345678ABCD1234";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(validOrder));

        // When
        OrderDTO result = orderService.getOrderByNumber(orderNumber);

        // Then
        assertNotNull(result);
        assertEquals(validOrder.getOrderNumber(), result.getOrderNumber());
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    void updateOrderStatus_WithValidTransition_ShouldUpdateSuccessfully() {
        // Given
        Long orderId = 1L;
        UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest();
        updateRequest.setNewStatus(OrderStatus.CONFIRMED);
        updateRequest.setUpdatedBy(2L);
        updateRequest.setNotes("Order confirmed by restaurant");

        Order orderToUpdate = createValidOrder();
        orderToUpdate.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderToUpdate));
        when(orderRepository.save(any(Order.class))).thenReturn(orderToUpdate);

        // When
        OrderDTO result = orderService.updateOrderStatus(orderId, updateRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithInvalidTransition_ShouldThrowException() {
        // Given
        Long orderId = 1L;
        UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest();
        updateRequest.setNewStatus(OrderStatus.PENDING);
        updateRequest.setUpdatedBy(2L);

        Order completedOrder = createValidOrder();
        completedOrder.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(completedOrder));

        // When & Then
        OrderStatusException exception = assertThrows(OrderStatusException.class, 
            () -> orderService.updateOrderStatus(orderId, updateRequest));
        
        assertTrue(exception.getMessage().contains("Cannot transition order"));
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getCustomerOrders_ShouldReturnPagedResults() {
        // Given
        Long customerId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(validOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable))
            .thenReturn(orderPage);

        // When
        Page<OrderDTO> result = orderService.getCustomerOrders(customerId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(orderRepository).findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    }

    @Test
    void getCustomerOrdersByStatus_ShouldReturnFilteredResults() {
        // Given
        Long customerId = 1L;
        OrderStatus status = OrderStatus.PENDING;
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(validOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status, pageable))
            .thenReturn(orderPage);

        // When
        Page<OrderDTO> result = orderService.getCustomerOrdersByStatus(customerId, status, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status, pageable);
    }

    @Test
    void getRestaurantOrders_ShouldReturnPagedResults() {
        // Given
        Long restaurantId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(validOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable))
            .thenReturn(orderPage);

        // When
        Page<OrderDTO> result = orderService.getRestaurantOrders(restaurantId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable);
    }

    @Test
    void cancelOrder_WithValidOrder_ShouldCancelSuccessfully() {
        // Given
        Long orderId = 1L;
        Long userId = 2L;
        String reason = "Customer requested cancellation";

        Order orderToCancel = createValidOrder();
        orderToCancel.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderToCancel));
        when(orderRepository.save(any(Order.class))).thenReturn(orderToCancel);

        // When
        OrderDTO result = orderService.cancelOrder(orderId, userId, reason);

        // Then
        assertNotNull(result);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void cancelOrder_WithNonCancellableOrder_ShouldThrowException() {
        // Given
        Long orderId = 1L;
        Long userId = 2L;
        String reason = "Customer requested cancellation";

        Order deliveredOrder = createValidOrder();
        deliveredOrder.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(deliveredOrder));

        // When & Then
        OrderStatusException exception = assertThrows(OrderStatusException.class, 
            () -> orderService.cancelOrder(orderId, userId, reason));
        
        assertTrue(exception.getMessage().contains("cannot be cancelled"));
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenOrderNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
            () -> orderService.cancelOrder(orderId, 1L, "reason"));
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getRestaurantOrdersByStatus_ShouldReturnFilteredOrders() {
        // Given
        Long restaurantId = 1L;
        OrderStatus status = OrderStatus.PENDING;
        Pageable pageable = PageRequest.of(0, 20);

        List<Order> orders = Arrays.asList(createValidOrder());
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findByRestaurantIdAndStatusOrderByCreatedAtAsc(restaurantId, status, pageable))
            .thenReturn(orderPage);

        // When
        Page<OrderDTO> result = orderService.getRestaurantOrdersByStatus(restaurantId, status, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findByRestaurantIdAndStatusOrderByCreatedAtAsc(restaurantId, status, pageable);
    }

    @Test
    void getRiderOrders_WithValidRiderId_ShouldReturnPagedOrders() {
        // Given
        Long riderId = 200L;
        Pageable pageable = PageRequest.of(0, 50);
        List<Order> orders = Arrays.asList(validOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findByRiderIdOrderByCreatedAtDesc(riderId, pageable))
            .thenReturn(orderPage);

        // When
        Page<OrderDTO> result = orderService.getRiderOrders(riderId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(validOrder.getId(), result.getContent().get(0).getId());
        verify(orderRepository).findByRiderIdOrderByCreatedAtDesc(riderId, pageable);
    }

    @Test
    void getRiderOrders_WithNoOrders_ShouldReturnEmptyPage() {
        // Given
        Long riderId = 999L;
        Pageable pageable = PageRequest.of(0, 50);
        Page<Order> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(orderRepository.findByRiderIdOrderByCreatedAtDesc(riderId, pageable))
            .thenReturn(emptyPage);

        // When
        Page<OrderDTO> result = orderService.getRiderOrders(riderId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(orderRepository).findByRiderIdOrderByCreatedAtDesc(riderId, pageable);
    }

    @Test
    void getAvailableOrdersForRiders_ShouldReturnReadyForPickupOrders() {
        // Given
        Pageable pageable = PageRequest.of(0, 50);
        Order readyOrder = createValidOrder();
        readyOrder.setStatus(OrderStatus.READY_FOR_PICKUP);
        List<Order> orders = Arrays.asList(readyOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findAvailableOrdersForRiders(eq(OrderStatus.READY_FOR_PICKUP), eq(pageable)))
            .thenReturn(orderPage);

        // When
        Page<OrderDTO> result = orderService.getAvailableOrdersForRiders(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findAvailableOrdersForRiders(eq(OrderStatus.READY_FOR_PICKUP), eq(pageable));
    }

    @Test
    void getAvailableOrdersForRiders_WithNoAvailableOrders_ShouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 50);
        Page<Order> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(orderRepository.findAvailableOrdersForRiders(eq(OrderStatus.READY_FOR_PICKUP), eq(pageable)))
            .thenReturn(emptyPage);

        // When
        Page<OrderDTO> result = orderService.getAvailableOrdersForRiders(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(orderRepository).findAvailableOrdersForRiders(eq(OrderStatus.READY_FOR_PICKUP), eq(pageable));
    }

    // Helper methods for test data creation
    private CreateOrderRequest createValidOrderRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setRestaurantId(1L);
        request.setDeliveryAddress("123 Test Street, Bangkok, Thailand");
        request.setDeliveryLatitude(13.7563);
        request.setDeliveryLongitude(100.5018);
        request.setSpecialInstructions("No spicy food please");

        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setMenuItemId(1L);
        item.setMenuItemName("Pad Thai");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("120.00"));
        item.setSpecialRequests("Extra vegetables");

        request.setOrderItems(new ArrayList<>(Arrays.asList(item)));
        return request;
    }

    private Order createValidOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("MR12345678ABCD1234");
        order.setCustomerId(1L);
        order.setRestaurantId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("275.00"));
        order.setDeliveryAddress("123 Test Street, Bangkok, Thailand");
        order.setDeliveryFee(new BigDecimal("35.00"));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setMenuItemId(1L);
        item.setMenuItemName("Pad Thai");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("120.00"));
        item.setOrder(order);

        order.getOrderItems().add(item);
        return order;
    }

    // ========== getAdminStats tests ==========

    @Test
    void getAdminStats_ShouldReturnAllCountsAndRevenue() {
        when(orderRepository.countAllSince(any(LocalDateTime.class)))
                .thenReturn(5L)    // todayOrderCount
                .thenReturn(50L);  // monthOrderCount
        when(orderRepository.count()).thenReturn(200L);
        when(orderRepository.sumTotalAmountByStatusSince(eq(OrderStatus.DELIVERED), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("1200.00"))   // todayRevenue
                .thenReturn(new BigDecimal("12000.00")); // monthRevenue

        java.util.Map<String, Object> result = orderService.getAdminStats();

        assertNotNull(result);
        assertEquals(5L, result.get("todayOrders"));
        assertEquals(50L, result.get("monthOrders"));
        assertEquals(200L, result.get("totalOrders"));
        // Commission is 10% of revenue — compare numerically, ignoring scale
        assertTrue(new BigDecimal("120.00").compareTo((BigDecimal) result.get("todayRevenue")) == 0);
        assertTrue(new BigDecimal("1200.00").compareTo((BigDecimal) result.get("monthRevenue")) == 0);
    }
}
