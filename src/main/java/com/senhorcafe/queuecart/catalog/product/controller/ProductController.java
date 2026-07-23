package com.senhorcafe.queuecart.catalog.product.controller;

import com.senhorcafe.queuecart.catalog.product.dto.CreateProductDTO;
import com.senhorcafe.queuecart.catalog.product.dto.ProductDTO;
import com.senhorcafe.queuecart.catalog.product.dto.UpdateProductDTO;
import com.senhorcafe.queuecart.catalog.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("product/")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("all-products")
    public ResponseEntity<List<ProductDTO>> productIndex() {
        return productService.returnAllProducts();
    }

    @GetMapping("product/{id}")
    public ResponseEntity<ProductDTO> productDetail(@PathVariable Long id) {
        return productService.returnProductDetails(id);
    }

    @GetMapping("product/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> productByCategory(@PathVariable Long categoryId) {
        return productService.returnByCategoryId(categoryId);
    }

    @PostMapping("create-product")
    public ResponseEntity<ProductDTO> productCreate(@RequestBody CreateProductDTO createProductDTO) {
        return productService.createProduct(createProductDTO);
    }

    @PatchMapping("update-product/{id}")
    public ResponseEntity<ProductDTO> productUpdate(@PathVariable Long id, @RequestBody UpdateProductDTO updateProductDTO) {
        return productService.updateProduct(id, updateProductDTO);
    }

    @PatchMapping("toggle-product/{id}")
    public ResponseEntity<ProductDTO> toggleProduct(@PathVariable Long id) {
        return productService.toggleProduct(id);
    }

    @DeleteMapping("delete-product/{id}")
    public ResponseEntity<Void> productDelete(@PathVariable Long id) {
        return productService.removeProduct(id);
    }
}
