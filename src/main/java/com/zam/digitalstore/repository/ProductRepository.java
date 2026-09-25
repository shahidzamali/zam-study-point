package com.zam.digitalstore.repository;

import com.zam.digitalstore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Get all active products of a category
    List<Product> findByCategoryIgnoreCaseAndActiveTrue(String category);

}