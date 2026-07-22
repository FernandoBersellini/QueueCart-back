package com.senhorcafe.queuecart.order.entity;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class OrderItem {
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
}
