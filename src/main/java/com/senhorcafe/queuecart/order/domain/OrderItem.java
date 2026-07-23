package com.senhorcafe.queuecart.order.domain;

import java.math.BigDecimal;

public record OrderItem(
    Long productId,
    String productName,
    BigDecimal unitPrice,
    int quantity
) {
    public OrderItem {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
