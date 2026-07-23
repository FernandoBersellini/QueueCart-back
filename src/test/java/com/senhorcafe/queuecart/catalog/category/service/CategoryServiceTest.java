package com.senhorcafe.queuecart.catalog.category.service;

import com.senhorcafe.queuecart.catalog.category.dto.CategoryDTO;
import com.senhorcafe.queuecart.catalog.category.dto.CreateCategoryDTO;
import com.senhorcafe.queuecart.catalog.category.dto.UpdateCategoryDTO;
import com.senhorcafe.queuecart.catalog.category.entity.Category;
import com.senhorcafe.queuecart.catalog.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository);
    }

    private Category buildCategory(Long id, String name, Category parent) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setSlug(name.toLowerCase());
        category.setDescription("description");
        category.setActive(true);
        category.setParent(parent);
        return category;
    }

    @Test
    void returnAllCategoriesShouldMapEntitiesToDTOs() {
        Category parent = buildCategory(1L, "Coffee", null);
        Category child = buildCategory(2L, "Espresso", parent);
        when(categoryRepository.findAll()).thenReturn(List.of(parent, child));

        ResponseEntity<List<CategoryDTO>> response = categoryService.returnAllCategories();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).parentId()).isNull();
        assertThat(response.getBody().get(1).parentId()).isEqualTo(1L);
    }

    @Test
    void returnCategoryDetailsShouldReturnDtoWhenFound() {
        Category category = buildCategory(1L, "Coffee", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        ResponseEntity<CategoryDTO> response = categoryService.returnCategoryDetails(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Coffee");
    }

    @Test
    void returnCategoryDetailsShouldThrowWhenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.returnCategoryDetails(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void returnByParentIdShouldMapEntitiesToDTOs() {
        Category parent = buildCategory(1L, "Coffee", null);
        Category child = buildCategory(2L, "Espresso", parent);
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(child));

        ResponseEntity<List<CategoryDTO>> response = categoryService.returnByParentId(1L);

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("Espresso");
    }

    @Test
    void createCategoryShouldSaveAndReturnCreated() {
        CreateCategoryDTO createCategoryDTO = new CreateCategoryDTO("Coffee", "coffee", "description", null);
        Category saved = buildCategory(1L, "Coffee", null);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        ResponseEntity<CategoryDTO> response = categoryService.createCategory(createCategoryDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().name()).isEqualTo("Coffee");
    }

    @Test
    void createCategoryShouldResolveParentWhenParentIdProvided() {
        Category parent = buildCategory(1L, "Coffee", null);
        CreateCategoryDTO createCategoryDTO = new CreateCategoryDTO("Espresso", "espresso", "description", 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CategoryDTO> response = categoryService.createCategory(createCategoryDTO);

        assertThat(response.getBody().parentId()).isEqualTo(1L);
    }

    @Test
    void createCategoryShouldThrowWhenParentNotFound() {
        CreateCategoryDTO createCategoryDTO = new CreateCategoryDTO("Espresso", "espresso", "description", 99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory(createCategoryDTO))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateCategoryShouldModifyExistingCategory() {
        Category category = buildCategory(1L, "Coffee", null);
        UpdateCategoryDTO updateCategoryDTO = new UpdateCategoryDTO("Coffee Beans", "coffee-beans", "new description", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CategoryDTO> response = categoryService.updateCategory(1L, updateCategoryDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Coffee Beans");
        assertThat(response.getBody().slug()).isEqualTo("coffee-beans");
    }

    @Test
    void updateCategoryShouldThrowWhenNotFound() {
        UpdateCategoryDTO updateCategoryDTO = new UpdateCategoryDTO("Coffee Beans", "coffee-beans", "new description", null);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, updateCategoryDTO))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void toggleCategoryShouldFlipActiveFlag() {
        Category category = buildCategory(1L, "Coffee", null);
        category.setActive(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CategoryDTO> response = categoryService.toggleCategory(1L);

        assertThat(response.getBody().active()).isFalse();
    }

    @Test
    void removeCategoryShouldDeleteAndReturnNoContent() {
        Category category = buildCategory(1L, "Coffee", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        ResponseEntity<Void> response = categoryService.removeCategory(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(categoryRepository).delete(category);
    }

    @Test
    void removeCategoryShouldThrowWhenNotFoundAndNeverDelete() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.removeCategory(99L))
                .isInstanceOf(ResponseStatusException.class);

        verify(categoryRepository, never()).delete(any());
    }
}
