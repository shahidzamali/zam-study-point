package com.zam.digitalstore.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class FileUploadController {

        private final Cloudinary cloudinary;

        private final Path productsPath = Paths.get("uploads/products")
                        .toAbsolutePath().normalize();

        public FileUploadController(
                        @Value("${CLOUDINARY_CLOUD_NAME}") String cloudName,
                        @Value("${CLOUDINARY_API_KEY}") String apiKey,
                        @Value("${CLOUDINARY_API_SECRET}") String apiSecret) {

                this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                                "cloud_name", cloudName,
                                "api_key", apiKey,
                                "api_secret", apiSecret,
                                "secure", true));
        }

        // PDF: temporary existing local storage
        @PostMapping(value = "/upload-file", consumes = "multipart/form-data")
        public ResponseEntity<String> uploadFile(
                        @RequestParam("file") MultipartFile file) {

                try {
                        if (file == null || file.isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body("Please select a PDF file");
                        }

                        String name = Paths.get(file.getOriginalFilename())
                                        .getFileName().toString();

                        if (!name.toLowerCase().endsWith(".pdf")) {
                                return ResponseEntity.badRequest()
                                                .body("Only PDF files are allowed");
                        }

                        Files.createDirectories(productsPath);
                        Files.copy(file.getInputStream(),
                                        productsPath.resolve(name).normalize(),
                                        StandardCopyOption.REPLACE_EXISTING);

                        return ResponseEntity.ok(name);

                } catch (IOException e) {
                        return ResponseEntity.internalServerError()
                                        .body("PDF upload failed");
                }
        }

        // Demo / preview images: Cloudinary
        @PostMapping(value = "/upload-preview", consumes = "multipart/form-data")
        public ResponseEntity<String> uploadPreview(
                        @RequestParam("file") MultipartFile file) {
                return uploadImage(file, "previews");
        }

        // Product thumbnail: Cloudinary
        @PostMapping(value = "/upload-thumbnail", consumes = "multipart/form-data")
        public ResponseEntity<String> uploadThumbnail(
                        @RequestParam("file") MultipartFile file) {
                return uploadImage(file, "thumbnails");
        }

        private ResponseEntity<String> uploadImage(
                        MultipartFile file, String folder) {

                try {
                        if (file == null || file.isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body("Please select an image");
                        }

                        String name = Paths.get(file.getOriginalFilename())
                                        .getFileName().toString().toLowerCase();

                        if (!(name.endsWith(".jpg")
                                        || name.endsWith(".jpeg")
                                        || name.endsWith(".png")
                                        || name.endsWith(".webp"))) {
                                return ResponseEntity.badRequest()
                                                .body("Only JPG, JPEG, PNG and WEBP images are allowed");
                        }

                        Map<?, ?> result = cloudinary.uploader().upload(
                                        file.getBytes(),
                                        ObjectUtils.asMap(
                                                        "resource_type", "image",
                                                        "folder", "zam-study-point/" + folder,
                                                        "public_id", UUID.randomUUID().toString()));

                        String url = result.get("secure_url").toString();
                        return ResponseEntity.ok(url);

                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.internalServerError()
                                        .body("Image upload failed. Check server logs.");
                }
        }
}