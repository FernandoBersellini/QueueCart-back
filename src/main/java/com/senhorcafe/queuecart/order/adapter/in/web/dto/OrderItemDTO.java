package com.senhorcafe.queuecart.order.adapter.in.web.dto;

import java.math.BigDecimal;

public record OrderItemDTO(
    Long productId,
    String productName,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal subtotal
) {}
