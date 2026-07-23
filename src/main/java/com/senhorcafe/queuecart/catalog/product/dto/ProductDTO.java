package com.senhorcafe.queuecart.catalog.product.dto;

import java.math.BigDecimal;

public record ProductDTO(
    String name,
    String description,
    String sku,
    BigDecimal price,
    boolean active,
    Long categoryId
) {}
