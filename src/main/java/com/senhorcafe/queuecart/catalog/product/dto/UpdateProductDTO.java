package com.senhorcafe.queuecart.catalog.product.dto;

import java.math.BigDecimal;

public record UpdateProductDTO(
    String name,
    String description,
    String sku,
    BigDecimal price,
    Long categoryId
) {}
