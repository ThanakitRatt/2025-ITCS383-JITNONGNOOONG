package com.itcs383.order.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.itcs383.common.dto.OrderDTO;
import com.itcs383.common.enums.OrderStatus;
import com.itcs383.order.dto.CreateOrderRequest;
import com.itcs383.order.dto.UpdateOrderStatusRequest;
import com.itcs383.order.entity.Order;
import com.itcs383.order.repository.OrderRepository;
import com.itcs383.order.service.OrderService;

/**
 * Full Integration Test for Order Service
 * Tests complete flow from API to Database with real Spring context
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        orderRepository.deleteAll();
    }

    @Test
    void completeOrderLifecycle_ShouldWorkEndToEnd() {
        // Phase 1: Create Order
        CreateOrderRequest createRequest = createValidOrderRequest();
        OrderDTO createdOrder = orderService.createOrder(createRequest);

        assertNotNull(createdOrder);
        assertNotNull(createdOrder.getId());
        assertNotNull(createdOrder.getOrderNumber());
        assertEquals(createRequest.getCustomerId(), createdOrder.getCustomerId());
        assertEquals(createRequest.getRestaurantId(), createdOrder.getRestaurantId());
        assertEquals(OrderStatus.PENDING.name(), createdOrder.getStatus());

        // Phase 2: Get Order by ID
        OrderDTO retrievedOrder = orderService.getOrder(createdOrder.getId());
        assertEquals(createdOrder.getId(), retrievedOrder.getId());
        assertEquals(createdOrder.getOrderNumber(), retrievedOrder.getOrderNumber());

        // Phase 3: Get Order by Number
        OrderDTO orderByNumber = orderService.getOrderByNumber(createdOrder.getOrderNumber());
        assertEquals(createdOrder.getId(), orderByNumber.getId());

        // Phase 4: Update Status to CONFIRMED
        UpdateOrderStatusRequest confirmRequest = new UpdateOrderStatusRequest();
        confirmRequest.setNewStatus(OrderStatus.CONFIRMED);
        confirmRequest.setUpdatedBy(2L);
        confirmRequest.setNotes("Order confirmed by restaurant");

        OrderDTO confirmedOrder = orderService.updateOrderStatus(createdOrder.getId(), confirmRequest);
        assertEquals(OrderStatus.CONFIRMED.name(), confirmedOrder.getStatus());

        // Phase 5: Update Status to PREPARING
        UpdateOrderStatusRequest prepareRequest = new UpdateOrderStatusRequest();
        prepareRequest.setNewStatus(OrderStatus.PREPARING);
        prepareRequest.setUpdatedBy(2L);
        prepareRequest.setNotes("Order is being prepared");

        OrderDTO preparingOrder = orderService.updateOrderStatus(createdOrder.getId(), prepareRequest);
        assertEquals(OrderStatus.PREPARING.name(), preparingOrder.getStatus());

        // Phase 6: Get Customer Orders
        Page<OrderDTO> customerOrders = orderService.getCustomerOrders(
            createRequest.getCustomerId(),
            PageRequest.of(0, 10)
        );
        assertEquals(1, customerOrders.getTotalElements());
        assertEquals(createdOrder.getId(), customerOrders.getContent().get(0).getId());

        // Phase 7: Get Customer Orders by Status
        Page<OrderDTO> preparingOrders = orderService.getCustomerOrdersByStatus(
            createRequest.getCustomerId(),
            OrderStatus.PREPARING,
            PageRequest.of(0, 10)
        );
        assertEquals(1, preparingOrders.getTotalElements());

        // Phase 8: Get Restaurant Orders
        Page<OrderDTO> restaurantOrders = orderService.getRestaurantOrders(
            createRequest.getRestaurantId(),
            PageRequest.of(0, 10)
        );
        assertEquals(1, restaurantOrders.getTotalElements());

        // Phase 9: Cancel Order (should succeed since within 5-minute window for PREPARING status)
        OrderDTO cancelledOrder = orderService.cancelOrder(createdOrder.getId(), 999L, "Changed mind");
        assertEquals(OrderStatus.CANCELLED.name(), cancelledOrder.getStatus());

        // Verify final state in database
        Order finalOrder = orderRepository.findById(createdOrder.getId()).orElse(null);
        assertNotNull(finalOrder);
        assertEquals(OrderStatus.CANCELLED, finalOrder.getStatus());
        assertTrue(finalOrder.getStatusHistory().size() >= 3); // PENDING -> CONFIRMED -> PREPARING -> CANCELLED
    }

    @Test
    void orderCancellation_WithValidConditions_ShouldSucceed() {
        // Create a new order
        CreateOrderRequest createRequest = createValidOrderRequest();
        OrderDTO createdOrder = orderService.createOrder(createRequest);

        // Cancel the order while it's still PENDING
        OrderDTO cancelledOrder = orderService.cancelOrder(
            createdOrder.getId(), 
            999L, 
            "Customer changed mind"
        );

        assertEquals(OrderStatus.CANCELLED.name(), cancelledOrder.getStatus());

        // Verify in database
        Order dbOrder = orderRepository.findById(createdOrder.getId()).orElse(null);
        assertNotNull(dbOrder);
        assertEquals(OrderStatus.CANCELLED, dbOrder.getStatus());
        assertTrue(dbOrder.getCancellationReason().contains("Customer changed mind"));
    }

    @Test
    void orderValidation_WithInvalidData_ShouldFail() {
        // Test 1: Order with empty items list
        CreateOrderRequest emptyItemsRequest = createValidOrderRequest();
        emptyItemsRequest.getOrderItems().clear();

        RuntimeException exception1 = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(emptyItemsRequest);
        });
        assertNotNull(exception1.getMessage());

        // Test 2: Excessive order total
        CreateOrderRequest expensiveRequest = createValidOrderRequest();
        expensiveRequest.getOrderItems().get(0).setUnitPrice(new BigDecimal("15000.00"));

        RuntimeException exception2 = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(expensiveRequest);
        });
        assertNotNull(exception2.getMessage());

        // Verify no orders were created
        assertEquals(0, orderRepository.count());
    }

    @Test
    void deliveryFeeCalculation_ShouldBeCorrect() {
        // Test 1: Order below free delivery threshold (< 300.00)
        CreateOrderRequest smallOrderRequest = createValidOrderRequest();
        smallOrderRequest.getOrderItems().get(0).setUnitPrice(new BigDecimal("140.00")); // 2 * 140 = 280 total

        OrderDTO smallOrder = orderService.createOrder(smallOrderRequest);
        assertEquals(new BigDecimal("35.00"), smallOrder.getDeliveryFee()); // Delivery fee applied

        // Test 2: Order at/above free delivery threshold (>= 300.00)  
        CreateOrderRequest largeOrderRequest = createValidOrderRequest();
        largeOrderRequest.getOrderItems().get(0).setUnitPrice(new BigDecimal("200.00")); // 400 total
        
        OrderDTO largeOrder = orderService.createOrder(largeOrderRequest);
        assertEquals(0, largeOrder.getDeliveryFee().compareTo(new BigDecimal("0.00"))); // Free delivery
    }

    @Test
    void multipleCustomersAndRestaurants_ShouldBeIsolated() {
        // Create orders for different customers and restaurants
        orderService.createOrder(createOrderRequestForCustomer(1L, 1L));
        orderService.createOrder(createOrderRequestForCustomer(2L, 1L));
        orderService.createOrder(createOrderRequestForCustomer(1L, 2L));

        // Verify customer 1 orders
        Page<OrderDTO> customer1Orders = orderService.getCustomerOrders(1L, PageRequest.of(0, 10));
        assertEquals(2, customer1Orders.getTotalElements());

        // Verify customer 2 orders
        Page<OrderDTO> customer2Orders = orderService.getCustomerOrders(2L, PageRequest.of(0, 10));
        assertEquals(1, customer2Orders.getTotalElements());

        // Verify restaurant 1 orders
        Page<OrderDTO> restaurant1Orders = orderService.getRestaurantOrders(1L, PageRequest.of(0, 10));
        assertEquals(2, restaurant1Orders.getTotalElements());

        // Verify restaurant 2 orders
        Page<OrderDTO> restaurant2Orders = orderService.getRestaurantOrders(2L, PageRequest.of(0, 10));
        assertEquals(1, restaurant2Orders.getTotalElements());
    }

    @Test
    void concurrentOrderCreation_ShouldGenerateUniqueOrderNumbers() {
        // Create multiple orders quickly
        CreateOrderRequest request1 = createValidOrderRequest();
        CreateOrderRequest request2 = createValidOrderRequest();
        request2.setCustomerId(2L);

        OrderDTO order1 = orderService.createOrder(request1);
        OrderDTO order2 = orderService.createOrder(request2);

        assertNotNull(order1.getOrderNumber());
        assertNotNull(order2.getOrderNumber());
        assertNotEquals(order1.getOrderNumber(), order2.getOrderNumber());

        // Verify both orders are in database
        assertEquals(2, orderRepository.count());
    }

    // Helper methods for test data creation
    private CreateOrderRequest createValidOrderRequest() {
        return createOrderRequestForCustomer(1L, 1L);
    }

    private CreateOrderRequest createOrderRequestForCustomer(Long customerId, Long restaurantId) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customerId);
        request.setRestaurantId(restaurantId);
        request.setDeliveryAddress("123 Test Street, Bangkok, Thailand");
        request.setDeliveryLatitude(13.7563);
        request.setDeliveryLongitude(100.5018);
        request.setSpecialInstructions("Test order instructions");

        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setMenuItemId(1L);
        item.setMenuItemName("Test Dish");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("120.00"));
        item.setSpecialRequests("Test special request");

        request.setOrderItems(new ArrayList<>(Arrays.asList(item)));
        return request;
    }
}
