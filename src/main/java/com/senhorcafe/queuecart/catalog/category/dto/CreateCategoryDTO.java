package com.senhorcafe.queuecart.catalog.category.dto;

public record CreateCategoryDTO(
   String name,
   String slug,
   String description,
   Long parentId
) {}
