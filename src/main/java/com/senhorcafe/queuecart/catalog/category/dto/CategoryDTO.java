package com.senhorcafe.queuecart.catalog.category.dto;

public record CategoryDTO(
   String name,
   String slug,
   String description,
   boolean active,
   Long parentId
) {}
