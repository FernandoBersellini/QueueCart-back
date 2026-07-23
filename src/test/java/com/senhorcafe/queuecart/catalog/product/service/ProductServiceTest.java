package com.senhorcafe.queuecart.catalog.product.service;

import com.senhorcafe.queuecart.catalog.category.entity.Category;
import com.senhorcafe.queuecart.catalog.category.repository.CategoryRepository;
import com.senhorcafe.queuecart.catalog.product.dto.CreateProductDTO;
import com.senhorcafe.queuecart.catalog.product.dto.ProductDTO;
import com.senhorcafe.queuecart.catalog.product.dto.UpdateProductDTO;
import com.senhorcafe.queuecart.catalog.product.entity.Product;
import com.senhorcafe.queuecart.catalog.product.repository.ProductRepository;
import com.senhorcafe.queuecart.config.web.PageResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository);
    }

    private Category buildCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setSlug(name.toLowerCase());
        category.setActive(true);
        return category;
    }

    private Product buildProduct(Long id, String name, Category category) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("description");
        product.setSku(name.toLowerCase() + "-sku");
        product.setPrice(BigDecimal.valueOf(10));
        product.setActive(true);
        product.setCategory(category);
        return product;
    }

    @Test
    void returnAllProductsShouldMapEntitiesToDTOs() {
        Category category = buildCategory(1L, "Coffee");
        Product product = buildProduct(1L, "Espresso", category);
        Pageable pageable = PageRequest.of(0, 20);
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        ResponseEntity<PageResponseDTO<ProductDTO>> response = productService.returnAllProducts(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).hasSize(1);
        assertThat(response.getBody().content().get(0).categoryId()).isEqualTo(1L);
    }

    @Test
    void returnProductDetailsShouldReturnDtoWhenFound() {
        Product product = buildProduct(1L, "Espresso", null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ResponseEntity<ProductDTO> response = productService.returnProductDetails(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Espresso");
    }

    @Test
    void returnProductDetailsShouldThrowWhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.returnProductDetails(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void returnByCategoryIdShouldMapEntitiesToDTOs() {
        Category category = buildCategory(1L, "Coffee");
        Product product = buildProduct(1L, "Espresso", category);
        Pageable pageable = PageRequest.of(0, 20);
        when(productRepository.findByCategoryId(1L, pageable)).thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        ResponseEntity<PageResponseDTO<ProductDTO>> response = productService.returnByCategoryId(1L, pageable);

        assertThat(response.getBody().content()).hasSize(1);
        assertThat(response.getBody().content().get(0).name()).isEqualTo("Espresso");
    }

    @Test
    void createProductShouldSaveAndReturnCreated() {
        CreateProductDTO createProductDTO = new CreateProductDTO("Espresso", "description", "espresso-sku", BigDecimal.TEN, null);
        Product saved = buildProduct(1L, "Espresso", null);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ResponseEntity<ProductDTO> response = productService.createProduct(createProductDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().name()).isEqualTo("Espresso");
    }

    @Test
    void createProductShouldResolveCategoryWhenCategoryIdProvided() {
        Category category = buildCategory(1L, "Coffee");
        CreateProductDTO createProductDTO = new CreateProductDTO("Espresso", "description", "espresso-sku", BigDecimal.TEN, 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<ProductDTO> response = productService.createProduct(createProductDTO);

        assertThat(response.getBody().categoryId()).isEqualTo(1L);
    }

    @Test
    void createProductShouldThrowWhenCategoryNotFound() {
        CreateProductDTO createProductDTO = new CreateProductDTO("Espresso", "description", "espresso-sku", BigDecimal.TEN, 99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(createProductDTO))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateProductShouldModifyExistingProduct() {
        Product product = buildProduct(1L, "Espresso", null);
        UpdateProductDTO updateProductDTO = new UpdateProductDTO("Double Espresso", "new description", "double-espresso-sku", BigDecimal.valueOf(15), null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<ProductDTO> response = productService.updateProduct(1L, updateProductDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Double Espresso");
        assertThat(response.getBody().price()).isEqualByComparingTo(BigDecimal.valueOf(15));
    }

    @Test
    void updateProductShouldThrowWhenNotFound() {
        UpdateProductDTO updateProductDTO = new UpdateProductDTO("Double Espresso", "new description", "double-espresso-sku", BigDecimal.valueOf(15), null);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, updateProductDTO))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void toggleProductShouldFlipActiveFlag() {
        Product product = buildProduct(1L, "Espresso", null);
        product.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<ProductDTO> response = productService.toggleProduct(1L);

        assertThat(response.getBody().active()).isFalse();
    }

    @Test
    void removeProductShouldDeleteAndReturnNoContent() {
        Product product = buildProduct(1L, "Espresso", null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ResponseEntity<Void> response = productService.removeProduct(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(productRepository).delete(product);
    }

    @Test
    void removeProductShouldThrowWhenNotFoundAndNeverDelete() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.removeProduct(99L))
                .isInstanceOf(ResponseStatusException.class);

        verify(productRepository, never()).delete(any());
    }
}
