package com.senhorcafe.queuecart.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class Order {
    private final Long id;
    private final Long userId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Order(Long id, Long userId, OrderStatus status, List<OrderItem> items, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Order create(Long userId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        return new Order(null, userId, OrderStatus.PENDING, items, null, null);
    }

    public static Order restore(Long id, Long userId, OrderStatus status, List<OrderItem> items, Instant createdAt, Instant updatedAt) {
        return new Order(id, userId, status, items, createdAt, updatedAt);
    }

    public void confirm() {
        transitionTo(OrderStatus.PENDING, OrderStatus.CONFIRMED);
    }

    public void ship() {
        transitionTo(OrderStatus.CONFIRMED, OrderStatus.SHIPPED);
    }

    public void deliver() {
        transitionTo(OrderStatus.SHIPPED, OrderStatus.DELIVERED);
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel an order with status " + status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    private void transitionTo(OrderStatus requiredCurrent, OrderStatus target) {
        if (status != requiredCurrent) {
            throw new IllegalStateException("Cannot move order from " + status + " to " + target);
        }
        this.status = target;
    }

    public BigDecimal totalAmount() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
