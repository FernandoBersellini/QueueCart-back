package com.senhorcafe.queuecart.order.adapter.in.web.dto;

import com.senhorcafe.queuecart.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDTO(
    Long id,
    Long userId,
    OrderStatus status,
    List<OrderItemDTO> items,
    BigDecimal totalAmount,
    Instant createdAt,
    Instant updatedAt
) {}
