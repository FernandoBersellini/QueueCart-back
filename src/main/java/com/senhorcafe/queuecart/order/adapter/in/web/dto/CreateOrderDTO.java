package com.senhorcafe.queuecart.order.adapter.in.web.dto;

import java.util.List;

public record CreateOrderDTO(
    Long userId,
    List<OrderItemRequestDTO> items
) {}
