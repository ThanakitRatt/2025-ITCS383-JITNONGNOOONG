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
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> orderService.updateOrderStatus(orderId, updateRequest));
        
        assertTrue(exception.getMessage().contains("Cannot update order status"));
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
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> orderService.cancelOrder(orderId, userId, reason));
        
        assertTrue(exception.getMessage().contains("Order cannot be cancelled"));
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
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

    private OrderDTO createValidOrderDTO() {
        return new OrderDTO(
            1L,
            "MR12345678ABCD1234",
            1L,
            1L,
            OrderStatus.PENDING.name(),
            new BigDecimal("275.00"),
            "123 Test Street, Bangkok, Thailand"
        );
    }
}
