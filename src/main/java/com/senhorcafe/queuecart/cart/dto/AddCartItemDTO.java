package com.senhorcafe.queuecart.cart.dto;

public record AddCartItemDTO(
    Long productId,
    int quantity
) {}
