package com.senhorcafe.queuecart.order.adapter.in.web.dto;

import java.math.BigDecimal;

public record OrderItemRequestDTO(
    Long productId,
    String productName,
    BigDecimal unitPrice,
    int quantity
) {}
