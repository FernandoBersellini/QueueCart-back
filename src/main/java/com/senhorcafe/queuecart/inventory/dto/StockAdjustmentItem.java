package com.senhorcafe.queuecart.inventory.dto;

public record StockAdjustmentItem(
    Long productId,
    int quantity
) {}
