package com.zam.digitalstore.service;

import com.zam.digitalstore.model.Product;
import com.zam.digitalstore.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ==========================================
    // GET ALL PRODUCTS
    // ==========================================

    public List<Product> getAllProducts() {

        return productRepository.findAll();
    }

    // ==========================================
    // GET PRODUCT BY ID
    // ==========================================

    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found with id: " + id));
    }

    // ==========================================
    // GET PRODUCTS BY CATEGORY
    // ==========================================

    public List<Product> getProductsByCategory(String category) {

        return productRepository
                .findByCategoryIgnoreCaseAndActiveTrue(category);
    }

    // ==========================================
    // CREATE PRODUCT
    // ==========================================

    public Product createProduct(Product product) {

        // Make sure new product is active
        product.setActive(true);

        // Prevent null previewImages
        if (product.getPreviewImages() == null) {
            product.setPreviewImages(new ArrayList<>());
        }

        return productRepository.save(product);
    }

    // ==========================================
    // UPDATE PRODUCT
    // ==========================================

    public Product updateProduct(Long id, Product product) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found with id: " + id));

        existingProduct.setName(product.getName());

        existingProduct.setDescription(
                product.getDescription());

        existingProduct.setPrice(
                product.getPrice());

        existingProduct.setCategory(
                product.getCategory());

        existingProduct.setProductType(
                product.getProductType());

        existingProduct.setFileUrl(
                product.getFileUrl());

        existingProduct.setImageUrl(
                product.getImageUrl());

        // ==========================================
        // PREVIEW IMAGES
        // ==========================================

        if (product.getPreviewImages() != null) {

            existingProduct.setPreviewImages(
                    product.getPreviewImages());

        } else {

            existingProduct.setPreviewImages(
                    new ArrayList<>());
        }

        // ==========================================
        // ACTIVE STATUS
        // ==========================================

        existingProduct.setActive(
                product.isActive());

        return productRepository.save(existingProduct);
    }

    // ==========================================
    // DELETE PRODUCT
    // ==========================================

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found with id: " + id));

        productRepository.delete(product);
    }
}