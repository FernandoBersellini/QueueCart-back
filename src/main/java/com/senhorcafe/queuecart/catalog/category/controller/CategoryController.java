package com.senhorcafe.queuecart.catalog.category.controller;

import com.senhorcafe.queuecart.catalog.category.dto.CategoryDTO;
import com.senhorcafe.queuecart.catalog.category.dto.CreateCategoryDTO;
import com.senhorcafe.queuecart.catalog.category.dto.UpdateCategoryDTO;
import com.senhorcafe.queuecart.catalog.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category/")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("all-categories")
    public ResponseEntity<List<CategoryDTO>> categoryIndex() {
        return categoryService.returnAllCategories();
    }

    @GetMapping("category/{id}")
    public ResponseEntity<CategoryDTO> categoryDetail(@PathVariable Long id) {
        return categoryService.returnCategoryDetails(id);
    }

    @GetMapping("category/parent/{parentId}")
    public ResponseEntity<List<CategoryDTO>> categoryByParent(@PathVariable Long parentId) {
        return categoryService.returnByParentId(parentId);
    }

    @PostMapping("create-category")
    public ResponseEntity<CategoryDTO> categoryCreate(@RequestBody CreateCategoryDTO createCategoryDTO) {
        return categoryService.createCategory(createCategoryDTO);
    }

    @PatchMapping("update-category/{id}")
    public ResponseEntity<CategoryDTO> categoryUpdate(@PathVariable Long id, @RequestBody UpdateCategoryDTO updateCategoryDTO) {
        return categoryService.updateCategory(id, updateCategoryDTO);
    }

    @PatchMapping("toggle-category/{id}")
    public ResponseEntity<CategoryDTO> toggleCategory(@PathVariable Long id) {
        return categoryService.toggleCategory(id);
    }

    @DeleteMapping("delete-category/{id}")
    public ResponseEntity<Void> categoryDelete(@PathVariable Long id) {
        return categoryService.removeCategory(id);
    }
}
