package com.mharruengsang.service;

import com.mharruengsang.entity.Order;
import com.mharruengsang.entity.Payment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Publishes order events to Kafka for async processing
 * Other services (Order Service, Rider Service) subscribe to these events
 */
@Service
@Slf4j
public class OrderEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publish ORDER_PAID event when payment is completed
     * Subscribed by: Order Service, Rider Service
     */
    public void publishOrderPaidEvent(Order order, Payment payment) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "ORDER_PAID");
            event.put("orderId", order.getId());
            event.put("customerId", order.getCustomer().getId());
            event.put("restaurantId", order.getRestaurant().getId());
            event.put("amount", order.getTotalAmount());
            event.put("paymentId", payment.getId());
            event.put("paymentMethod", payment.getPaymentMethod().name());
            event.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("order-paid-topic", String.valueOf(order.getId()), event);
            log.info("ORDER_PAID event published for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing ORDER_PAID event: {}", e.getMessage());
        }
    }
    
    /**
     * Publish ORDER_DELIVERY_ASSIGNED event when rider is assigned
     */
    public void publishDeliveryAssignedEvent(Order order, Long riderId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "DELIVERY_ASSIGNED");
            event.put("orderId", order.getId());
            event.put("riderId", riderId);
            event.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("delivery-assigned-topic", String.valueOf(order.getId()), event);
            log.info("DELIVERY_ASSIGNED event published for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing DELIVERY_ASSIGNED event: {}", e.getMessage());
        }
    }
    
    /**
     * Publish ORDER_DELIVERED event when delivery is completed
     */
    public void publishOrderDeliveredEvent(Order order) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "ORDER_DELIVERED");
            event.put("orderId", order.getId());
            event.put("riderId", order.getRider().getId());
            event.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("order-delivered-topic", String.valueOf(order.getId()), event);
            log.info("ORDER_DELIVERED event published for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing ORDER_DELIVERED event: {}", e.getMessage());
        }
    }
}
