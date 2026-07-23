package com.senhorcafe.queuecart.inventory.dto;

public record StockItemDTO(
    Long productId,
    int quantityAvailable
) {}
