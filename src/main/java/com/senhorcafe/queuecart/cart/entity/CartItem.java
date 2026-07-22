package com.senhorcafe.queuecart.cart.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class CartItem {
    private Long productId;
    private int quantity;
}
