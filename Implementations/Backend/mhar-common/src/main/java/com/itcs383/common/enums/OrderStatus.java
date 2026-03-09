package com.itcs383.common.enums;

/**
 * Order status enumeration for tracking order lifecycle
 * Used across Order Service and other services
 */
public enum OrderStatus {
    PENDING("Order received, waiting for restaurant confirmation"),
    CONFIRMED("Restaurant confirmed, preparing food"),
    PREPARING("Food is being prepared"),
    READY_FOR_PICKUP("Food ready, waiting for rider pickup"),
    PICKED_UP("Rider picked up, on the way to delivery"),
    DELIVERED("Order delivered successfully"),
    CANCELLED("Order cancelled"),
    REFUNDED("Order refunded");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
