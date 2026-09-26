package com.zam.digitalstore.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 2000)
    private String description;

    private BigDecimal price;

    private String category;

    private String productType;

    private String fileUrl;

    private String imageUrl;

    // ==========================================
    // DEMO / PREVIEW IMAGES
    // ==========================================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> previewImages = new ArrayList<>();

    // ==========================================
    // PRODUCT STATUS
    // ==========================================

    private boolean active = true;

    // ==========================================
    // CREATED DATE
    // ==========================================

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {

        createdAt = LocalDateTime.now();

        // New products should be active by default
        active = true;

        // Prevent null preview list
        if (previewImages == null) {
            previewImages = new ArrayList<>();
        }
    }

    @PreUpdate
    public void onUpdate() {

        // Prevent null preview list
        if (previewImages == null) {
            previewImages = new ArrayList<>();
        }
    }
}