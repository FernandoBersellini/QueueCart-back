package com.senhorcafe.queuecart.cart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class CartItem {
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity")
    private int quantity;
}
