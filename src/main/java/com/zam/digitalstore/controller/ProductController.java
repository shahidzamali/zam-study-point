package com.zam.digitalstore.controller;

import com.zam.digitalstore.model.Product;
import com.zam.digitalstore.service.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ==========================================
    // GET ALL PRODUCTS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {

        return ResponseEntity.ok(
                productService.getAllProducts());
    }

    // ==========================================
    // GET PRODUCTS BY CATEGORY
    // ==========================================

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @PathVariable String category) {

        return ResponseEntity.ok(
                productService.getProductsByCategory(category));
    }

    // ==========================================
    // GET PRODUCT BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productService.getProductById(id));
    }

    // ==========================================
    // CREATE PRODUCT
    // ==========================================

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product) {

        Product savedProduct = productService.createProduct(product);

        return ResponseEntity.ok(savedProduct);
    }

    // ==========================================
    // UPDATE PRODUCT
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        Product updatedProduct = productService.updateProduct(id, product);

        return ResponseEntity.ok(updatedProduct);
    }

    // ==========================================
    // DELETE PRODUCT
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.ok(
                "Product deleted successfully");
    }
}