package com.mharruengsang.dto;

import com.mharruengsang.entity.Order;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private Long riderId;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;
    private String discountCode;
    private String status; // OrderStatus enum as string
    private String deliveryStatus;
    private String deliveryAddress;
    private String customerNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;
    
    public static OrderDTO fromEntity(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .restaurantId(order.getRestaurant().getId())
                .riderId(order.getRider() != null ? order.getRider().getId() : null)
                .totalAmount(order.getTotalAmount())
                .deliveryFee(order.getDeliveryFee())
                .discountAmount(order.getDiscountAmount())
                .discountCode(order.getDiscountCode())
                .status(order.getStatus().name())
                .deliveryStatus(order.getDeliveryStatus().name())
                .deliveryAddress(order.getDeliveryAddress())
                .customerNotes(order.getCustomerNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }
}
