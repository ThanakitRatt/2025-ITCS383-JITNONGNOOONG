package com.itcs383.order.entity;

import com.itcs383.common.enums.OrderStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * OrderStatusHistory Entity - Tracks all status changes for an order
 * Provides audit trail for order lifecycle management
 */
@Entity
@Table(name = "order_status_history", indexes = {
    @Index(name = "idx_status_history_order_id", columnList = "order_id"),
    @Index(name = "idx_status_history_changed_at", columnList = "changed_at")
})
@EntityListeners(AuditingEntityListener.class)
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private OrderStatus newStatus;

    @Column(name = "changed_by", nullable = false)
    private Long changedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    // Constructors
    public OrderStatusHistory() {}

    public OrderStatusHistory(Order order, OrderStatus previousStatus, OrderStatus newStatus, 
                             Long changedBy, String notes) {
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.notes = notes;
        this.changedAt = LocalDateTime.now();
    }

    // Business Methods
    public String getStatusChangeDescription() {
        String fromStatus = previousStatus != null ? previousStatus.name() : "NEW";
        return String.format("Order status changed from %s to %s", fromStatus, newStatus.name());
    }

    public boolean isStatusProgression() {
        if (previousStatus == null) return true;
        
        // Define valid progressions
        return switch (previousStatus) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.PREPARING || newStatus == OrderStatus.CANCELLED;
            case PREPARING -> newStatus == OrderStatus.READY_FOR_PICKUP;
            case READY_FOR_PICKUP -> newStatus == OrderStatus.PICKED_UP;
            case PICKED_UP -> newStatus == OrderStatus.DELIVERED;
            default -> false;
        };
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public OrderStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(OrderStatus previousStatus) { this.previousStatus = previousStatus; }

    public OrderStatus getNewStatus() { return newStatus; }
    public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }

    public Long getChangedBy() { return changedBy; }
    public void setChangedBy(Long changedBy) { this.changedBy = changedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    @Override
    public String toString() {
        return "OrderStatusHistory{" +
                "id=" + id +
                ", previousStatus=" + previousStatus +
                ", newStatus=" + newStatus +
                ", changedBy=" + changedBy +
                ", changedAt=" + changedAt +
                '}';
    }
}
