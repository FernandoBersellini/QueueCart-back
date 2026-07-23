package com.senhorcafe.queuecart.catalog.category.service;

import com.senhorcafe.queuecart.catalog.category.dto.CategoryDTO;
import com.senhorcafe.queuecart.catalog.category.dto.CreateCategoryDTO;
import com.senhorcafe.queuecart.catalog.category.dto.UpdateCategoryDTO;
import com.senhorcafe.queuecart.catalog.category.entity.Category;
import com.senhorcafe.queuecart.catalog.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public ResponseEntity<List<CategoryDTO>> returnAllCategories() {
        List<CategoryDTO> categories = categoryRepository.findAll().stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok().body(categories);
    }

    public ResponseEntity<CategoryDTO> returnCategoryDetails(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        return ResponseEntity.ok().body(toDTO(category));
    }

    public ResponseEntity<List<CategoryDTO>> returnByParentId(Long parentId) {
        List<CategoryDTO> categories = categoryRepository.findByParentId(parentId).stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok().body(categories);
    }

    public ResponseEntity<CategoryDTO> createCategory(CreateCategoryDTO createCategoryDTO) {
        Category category = new Category();
        category.setName(createCategoryDTO.name());
        category.setSlug(createCategoryDTO.slug());
        category.setDescription(createCategoryDTO.description());
        category.setParent(resolveParent(createCategoryDTO.parentId()));

        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    public ResponseEntity<CategoryDTO> updateCategory(Long categoryId, UpdateCategoryDTO updateCategoryDTO) {
        Category category = findCategoryOrThrow(categoryId);
        category.setName(updateCategoryDTO.name());
        category.setSlug(updateCategoryDTO.slug());
        category.setDescription(updateCategoryDTO.description());
        category.setParent(resolveParent(updateCategoryDTO.parentId()));

        Category updated = categoryRepository.save(category);
        return ResponseEntity.ok().body(toDTO(updated));
    }

    public ResponseEntity<CategoryDTO> toggleCategory(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        category.setActive(!category.isActive());

        Category updated = categoryRepository.save(category);
        return ResponseEntity.ok().body(toDTO(updated));
    }

    public ResponseEntity<Void> removeCategory(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        categoryRepository.delete(category);
        return ResponseEntity.noContent().build();
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private Category resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return findCategoryOrThrow(parentId);
    }

    private CategoryDTO toDTO(Category category) {
        return new CategoryDTO(
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.isActive(),
                category.getParent() != null ? category.getParent().getId() : null
        );
    }
}
