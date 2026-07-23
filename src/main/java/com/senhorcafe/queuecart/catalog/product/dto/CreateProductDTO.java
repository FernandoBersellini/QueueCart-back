package com.senhorcafe.queuecart.catalog.product.dto;

import java.math.BigDecimal;

public record CreateProductDTO(
    String name,
    String description,
    String sku,
    BigDecimal price,
    Long categoryId
) {}
