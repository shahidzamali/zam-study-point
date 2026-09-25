package com.zam.digitalstore.controller;

import com.zam.digitalstore.model.Payment;
import com.zam.digitalstore.model.Product;
import com.zam.digitalstore.repository.PaymentRepository;
import com.zam.digitalstore.repository.ProductRepository;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/secure")
@CrossOrigin("*")
public class SecureFileController {

        private final PaymentRepository paymentRepository;
        private final ProductRepository productRepository;

        public SecureFileController(
                        PaymentRepository paymentRepository,
                        ProductRepository productRepository) {

                this.paymentRepository = paymentRepository;
                this.productRepository = productRepository;
        }

        @GetMapping("/download/{productId}")
        public ResponseEntity<Resource> downloadProduct(
                @PathVariable Long productId,
                @RequestParam String email) {

                try {

                        System.out.println("========== DOWNLOAD DEBUG ==========");

                        email = email.trim()
                                        .replace("\"", "")
                                        .replace("'", "");

                        System.out.println("Customer Email: [" + email + "]");
                        System.out.println("Product ID: [" + productId + "]");

                        Optional<Payment> payment = paymentRepository
                                        .findByCustomerEmailAndProductIdAndStatus(
                                                        email,
                                                        productId,
                                                        "SUCCESS");

                        System.out.println(
                                        "Payment Found: " + payment.isPresent());

                        if (payment.isEmpty()) {

                                System.out.println(
                                                "❌ PAYMENT NOT FOUND");

                                return ResponseEntity.status(403).build();
                        }

                        Payment p = payment.get();

                        System.out.println(
                                        "DB Payment ID: [" +
                                                        p.getRazorpayPaymentId() +
                                                        "]");

                        System.out.println(
                                        "DB Product ID: [" +
                                                        p.getProductId() +
                                                        "]");

                        System.out.println(
                                        "DB Status: [" +
                                                        p.getStatus() +
                                                        "]");

                        // ==========================================
                        // STEP 2: CHECK PRODUCT ID
                        // ==========================================

                        if (!p.getProductId().equals(productId)) {

                                System.out.println(
                                                "❌ PRODUCT ID DOES NOT MATCH");

                                return ResponseEntity.status(403).build();
                        }

                        // ==========================================
                        // STEP 3: CHECK PAYMENT STATUS
                        // ==========================================

                        if (!"SUCCESS".equalsIgnoreCase(p.getStatus())) {

                                System.out.println(
                                                "❌ PAYMENT STATUS IS NOT SUCCESS");

                                return ResponseEntity.status(403).build();
                        }

                        // ==========================================
                        // STEP 4: FIND PRODUCT
                        // ==========================================

                        Optional<Product> product = productRepository.findById(productId);

                        if (product.isEmpty()) {

                                System.out.println(
                                                "❌ PRODUCT NOT FOUND");

                                return ResponseEntity.notFound().build();
                        }

                        // ==========================================
                        // STEP 5: GET FILE
                        // ==========================================

                        String fileName = product.get().getFileUrl();

                        fileName = Paths.get(fileName)
                                        .getFileName()
                                        .toString();

                        Path filePath = Paths.get(
                                        "uploads/products",
                                        fileName).toAbsolutePath().normalize();

                        Resource resource = new UrlResource(filePath.toUri());

                        if (!resource.exists() ||
                                        !resource.isReadable()) {

                                System.out.println(
                                                "❌ PDF FILE NOT FOUND");

                                return ResponseEntity.notFound().build();
                        }

                        // ==========================================
                        // STEP 6: DOWNLOAD
                        // ==========================================

                        System.out.println(
                                        "✅ PAYMENT VERIFIED");

                        System.out.println(
                                        "✅ DOWNLOAD ALLOWED");

                        return ResponseEntity.ok()
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .header(
                                                        HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\"" +
                                                                        resource.getFilename() +
                                                                        "\"")
                                        .body(resource);

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .internalServerError()
                                        .build();
                }
        }
}