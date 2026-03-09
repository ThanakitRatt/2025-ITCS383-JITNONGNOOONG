package com.itcs383.order.dto;

import com.itcs383.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Update Order Status Request DTO
 * Used for updating order status during lifecycle management
 */
public class UpdateOrderStatusRequest {

    @NotNull(message = "New status is required")
    private OrderStatus newStatus;

    @NotNull(message = "Updated by user ID is required")
    private Long updatedBy;

    private String notes;

    // Constructors
    public UpdateOrderStatusRequest() {}

    public UpdateOrderStatusRequest(OrderStatus newStatus, Long updatedBy, String notes) {
        this.newStatus = newStatus;
        this.updatedBy = updatedBy;
        this.notes = notes;
    }

    // Getters and Setters
    public OrderStatus getNewStatus() { return newStatus; }
    public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }

    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "UpdateOrderStatusRequest{" +
                "newStatus=" + newStatus +
                ", updatedBy=" + updatedBy +
                ", notes='" + notes + '\'' +
                '}';
    }
}
