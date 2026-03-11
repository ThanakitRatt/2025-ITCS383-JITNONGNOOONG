package com.itcs383.common.exception;

import com.itcs383.common.enums.OrderStatus;

/**
 * Exception thrown when an invalid order status transition is attempted
 */
public class OrderStatusException extends RuntimeException {

    private final OrderStatus currentStatus;
    private final OrderStatus requestedStatus;

    public OrderStatusException(OrderStatus currentStatus, OrderStatus requestedStatus) {
        super(String.format("Cannot transition order from %s to %s", currentStatus, requestedStatus));
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public OrderStatusException(String message) {
        super(message);
        this.currentStatus = null;
        this.requestedStatus = null;
    }

    public OrderStatusException(String message, Throwable cause) {
        super(message, cause);
        this.currentStatus = null;
        this.requestedStatus = null;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public OrderStatus getRequestedStatus() {
        return requestedStatus;
    }
}
