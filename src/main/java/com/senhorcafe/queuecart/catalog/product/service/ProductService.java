package com.senhorcafe.queuecart.catalog.product.service;

import com.senhorcafe.queuecart.catalog.category.entity.Category;
import com.senhorcafe.queuecart.catalog.category.repository.CategoryRepository;
import com.senhorcafe.queuecart.catalog.product.dto.CreateProductDTO;
import com.senhorcafe.queuecart.catalog.product.dto.ProductDTO;
import com.senhorcafe.queuecart.catalog.product.dto.UpdateProductDTO;
import com.senhorcafe.queuecart.catalog.product.entity.Product;
import com.senhorcafe.queuecart.catalog.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ResponseEntity<List<ProductDTO>> returnAllProducts() {
        List<ProductDTO> products = productRepository.findAll().stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok().body(products);
    }

    public ResponseEntity<ProductDTO> returnProductDetails(Long productId) {
        Product product = findProductOrThrow(productId);
        return ResponseEntity.ok().body(toDTO(product));
    }

    public ResponseEntity<List<ProductDTO>> returnByCategoryId(Long categoryId) {
        List<ProductDTO> products = productRepository.findByCategoryId(categoryId).stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok().body(products);
    }

    public ResponseEntity<ProductDTO> createProduct(CreateProductDTO createProductDTO) {
        Product product = new Product();
        product.setName(createProductDTO.name());
        product.setDescription(createProductDTO.description());
        product.setSku(createProductDTO.sku());
        product.setPrice(createProductDTO.price());
        product.setCategory(resolveCategory(createProductDTO.categoryId()));

        Product saved = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    public ResponseEntity<ProductDTO> updateProduct(Long productId, UpdateProductDTO updateProductDTO) {
        Product product = findProductOrThrow(productId);
        product.setName(updateProductDTO.name());
        product.setDescription(updateProductDTO.description());
        product.setSku(updateProductDTO.sku());
        product.setPrice(updateProductDTO.price());
        product.setCategory(resolveCategory(updateProductDTO.categoryId()));

        Product updated = productRepository.save(product);
        return ResponseEntity.ok().body(toDTO(updated));
    }

    public ResponseEntity<ProductDTO> toggleProduct(Long productId) {
        Product product = findProductOrThrow(productId);
        product.setActive(!product.isActive());

        Product updated = productRepository.save(product);
        return ResponseEntity.ok().body(toDTO(updated));
    }

    public ResponseEntity<Void> removeProduct(Long productId) {
        Product product = findProductOrThrow(productId);
        productRepository.delete(product);
        return ResponseEntity.noContent().build();
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private ProductDTO toDTO(Product product) {
        return new ProductDTO(
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice(),
                product.isActive(),
                product.getCategory() != null ? product.getCategory().getId() : null
        );
    }
}
