package com.itcs383.order.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itcs383.common.dto.OrderDTO;
import com.itcs383.common.enums.OrderStatus;
import com.itcs383.order.dto.CreateOrderRequest;
import com.itcs383.order.dto.UpdateOrderStatusRequest;
import com.itcs383.order.service.OrderService;

/**
 * Integration Tests for OrderController
 * Tests REST API endpoints, request/response handling, and validation
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateOrderRequest validCreateRequest;
    private OrderDTO validOrderDTO;
    private UpdateOrderStatusRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        validCreateRequest = createValidOrderRequest();
        validOrderDTO = createValidOrderDTO();
        validUpdateRequest = createValidUpdateRequest();
    }

    @Test
    void createOrder_WithValidRequest_ShouldReturn201() throws Exception {
        // Given
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(validOrderDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(validOrderDTO.getId()))
                .andExpect(jsonPath("$.data.orderNumber").value(validOrderDTO.getOrderNumber()))
                .andExpect(jsonPath("$.data.customerId").value(validOrderDTO.getCustomerId()))
                .andExpect(jsonPath("$.data.restaurantId").value(validOrderDTO.getRestaurantId()));

        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void createOrder_WithInvalidRequest_ShouldReturn400() throws Exception {
        // Given
        CreateOrderRequest invalidRequest = new CreateOrderRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void getOrder_WithValidId_ShouldReturn200() throws Exception {
        // Given
        Long orderId = 1L;
        when(orderService.getOrder(orderId)).thenReturn(validOrderDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(validOrderDTO.getId()))
                .andExpect(jsonPath("$.data.orderNumber").value(validOrderDTO.getOrderNumber()));

        verify(orderService).getOrder(orderId);
    }

    @Test
    void getOrder_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        Long orderId = 999L;
        when(orderService.getOrder(orderId)).thenThrow(new RuntimeException("Order not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());

        verify(orderService).getOrder(orderId);
    }

    @Test
    void getOrderByNumber_WithValidNumber_ShouldReturn200() throws Exception {
        // Given
        String orderNumber = "MR12345678ABCD1234";
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(validOrderDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/number/{orderNumber}", orderNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value(orderNumber));

        verify(orderService).getOrderByNumber(orderNumber);
    }

    @Test
    void updateOrderStatus_WithValidRequest_ShouldReturn200() throws Exception {
        // Given
        Long orderId = 1L;
        OrderDTO updatedOrder = createValidOrderDTO();
        updatedOrder.setStatus(OrderStatus.CONFIRMED.name());
        
        when(orderService.updateOrderStatus(eq(orderId), any(UpdateOrderStatusRequest.class)))
            .thenReturn(updatedOrder);

        // When & Then
        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(OrderStatus.CONFIRMED.name()));

        verify(orderService).updateOrderStatus(eq(orderId), any(UpdateOrderStatusRequest.class));
    }

    @Test
    void getCustomerOrders_WithValidCustomerId_ShouldReturn200() throws Exception {
        // Given
        Long customerId = 1L;
        Page<OrderDTO> orderPage = new PageImpl<>(
            Arrays.asList(validOrderDTO), 
            PageRequest.of(0, 10), 
            1
        );

        when(orderService.getCustomerOrders(eq(customerId), any()))
            .thenReturn(orderPage);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/customer/{customerId}", customerId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].customerId").value(customerId));

        verify(orderService).getCustomerOrders(eq(customerId), any());
    }

    @Test
    void getCustomerOrdersByStatus_WithValidParameters_ShouldReturn200() throws Exception {
        // Given
        Long customerId = 1L;
        OrderStatus status = OrderStatus.PENDING;
        Page<OrderDTO> orderPage = new PageImpl<>(
            Arrays.asList(validOrderDTO), 
            PageRequest.of(0, 10), 
            1
        );

        when(orderService.getCustomerOrdersByStatus(eq(customerId), eq(status), any()))
            .thenReturn(orderPage);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/customer/{customerId}/status/{status}", customerId, status)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(orderService).getCustomerOrdersByStatus(eq(customerId), eq(status), any());
    }

    @Test
    void getRestaurantOrders_WithValidRestaurantId_ShouldReturn200() throws Exception {
        // Given
        Long restaurantId = 1L;
        Page<OrderDTO> orderPage = new PageImpl<>(
            Arrays.asList(validOrderDTO), 
            PageRequest.of(0, 10), 
            1
        );

        when(orderService.getRestaurantOrders(eq(restaurantId), any()))
            .thenReturn(orderPage);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/restaurant/{restaurantId}", restaurantId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(orderService).getRestaurantOrders(eq(restaurantId), any());
    }

    @Test
    void cancelOrder_WithValidRequest_ShouldReturn200() throws Exception {
        // Given
        Long orderId = 1L;
        OrderDTO cancelledOrder = createValidOrderDTO();
        cancelledOrder.setStatus(OrderStatus.CANCELLED.name());

        when(orderService.cancelOrder(eq(orderId), any(), any()))
            .thenReturn(cancelledOrder);

        // When & Then
        mockMvc.perform(put("/api/v1/orders/{id}/cancel", orderId)
                .param("userId", "2")
                .param("reason", "Customer requested"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(OrderStatus.CANCELLED.name()));

        verify(orderService).cancelOrder(eq(orderId), eq(2L), eq("Customer requested"));
    }

    @Test
    void createOrder_WithServiceException_ShouldReturn500() throws Exception {
        // Given
        when(orderService.createOrder(any(CreateOrderRequest.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());

        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void updateOrderStatus_WithInvalidTransition_ShouldReturn400() throws Exception {
        // Given
        Long orderId = 1L;
        when(orderService.updateOrderStatus(eq(orderId), any(UpdateOrderStatusRequest.class)))
            .thenThrow(new IllegalStateException("Invalid status transition"));

        // When & Then
        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());

        verify(orderService).updateOrderStatus(eq(orderId), any(UpdateOrderStatusRequest.class));
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

    private OrderDTO createValidOrderDTO() {
        OrderDTO dto = new OrderDTO(
            1L,
            "MR12345678ABCD1234",
            1L,
            1L,
            OrderStatus.PENDING.name(),
            new BigDecimal("275.00"),
            "123 Test Street, Bangkok, Thailand"
        );
        dto.setDeliveryFee(new BigDecimal("35.00"));
        return dto;
    }

    private UpdateOrderStatusRequest createValidUpdateRequest() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setNewStatus(OrderStatus.CONFIRMED);
        request.setUpdatedBy(2L);
        request.setNotes("Order confirmed by restaurant");
        return request;
    }
}
