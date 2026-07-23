package com.senhorcafe.queuecart.catalog.category.dto;

public record UpdateCategoryDTO(
   String name,
   String slug,
   String description,
   Long parentId
) {}
