package com.zam.digitalstore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class FileUploadController {

        // =========================================================
        // FILE STORAGE LOCATIONS
        // =========================================================

        private final Path productsPath = Paths.get("uploads/products")
                        .toAbsolutePath()
                        .normalize();

        private final Path previewPath = Paths.get("uploads/previews")
                        .toAbsolutePath()
                        .normalize();

        private final Path thumbnailPath = Paths.get("uploads/thumbnails")
                        .toAbsolutePath()
                        .normalize();

        // =========================================================
        // UPLOAD PRODUCT PDF
        // =========================================================

        @PostMapping(value = "/upload-file", consumes = "multipart/form-data")
        public ResponseEntity<String> uploadFile(
                        @RequestParam("file") MultipartFile file) {

                try {

                        if (file == null || file.isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body("Please select a PDF file");
                        }

                        Files.createDirectories(productsPath);

                        String fileName = file.getOriginalFilename();

                        if (fileName == null || fileName.isBlank()) {
                                return ResponseEntity.badRequest()
                                                .body("Invalid file name");
                        }

                        fileName = Paths.get(fileName)
                                        .getFileName()
                                        .toString();

                        if (!fileName.toLowerCase().endsWith(".pdf")) {
                                return ResponseEntity.badRequest()
                                                .body("Only PDF files are allowed");
                        }

                        Path filePath = productsPath
                                        .resolve(fileName)
                                        .normalize();

                        if (!filePath.startsWith(productsPath)) {
                                return ResponseEntity.badRequest()
                                                .body("Invalid file path");
                        }

                        Files.copy(
                                        file.getInputStream(),
                                        filePath,
                                        StandardCopyOption.REPLACE_EXISTING);

                        System.out.println(
                                        "PDF uploaded successfully: " + fileName);

                        // IMPORTANT:
                        // Return ONLY filename.
                        // Database will store filename.
                        return ResponseEntity.ok(fileName);

                } catch (IOException e) {

                        e.printStackTrace();

                        return ResponseEntity.internalServerError()
                                        .body("PDF upload failed: " + e.getMessage());
                }
        }

        // =========================================================
        // UPLOAD DEMO / PREVIEW IMAGE
        // =========================================================

        @PostMapping(value = "/upload-preview", consumes = "multipart/form-data")
        public ResponseEntity<String> uploadPreview(
                        @RequestParam("file") MultipartFile file) {

                try {

                        if (file == null || file.isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body("Please select a demo image");
                        }

                        Files.createDirectories(previewPath);

                        String fileName = file.getOriginalFilename();

                        if (fileName == null || fileName.isBlank()) {
                                return ResponseEntity.badRequest()
                                                .body("Invalid image name");
                        }

                        fileName = Paths.get(fileName)
                                        .getFileName()
                                        .toString();

                        String lowerName = fileName.toLowerCase();

                        if (!(lowerName.endsWith(".jpg")
                                        || lowerName.endsWith(".jpeg")
                                        || lowerName.endsWith(".png")
                                        || lowerName.endsWith(".webp"))) {

                                return ResponseEntity.badRequest()
                                                .body(
                                                                "Only JPG, JPEG, PNG and WEBP images are allowed");
                        }

                        Path filePath = previewPath
                                        .resolve(fileName)
                                        .normalize();

                        if (!filePath.startsWith(previewPath)) {
                                return ResponseEntity.badRequest()
                                                .body("Invalid file path");
                        }

                        Files.copy(
                                        file.getInputStream(),
                                        filePath,
                                        StandardCopyOption.REPLACE_EXISTING);

                        System.out.println(
                                        "Demo image uploaded successfully: "
                                                        + fileName);

                        return ResponseEntity.ok(
                                        "/previews/" + fileName);

                } catch (IOException e) {

                        e.printStackTrace();

                        return ResponseEntity.internalServerError()
                                        .body(
                                                        "Demo image upload failed: "
                                                                        + e.getMessage());
                }
        }

        // =========================================================
        // UPLOAD PRODUCT THUMBNAIL
        // =========================================================

        @PostMapping(value = "/upload-thumbnail", consumes = "multipart/form-data")
        public ResponseEntity<String> uploadThumbnail(
                        @RequestParam("file") MultipartFile file) {

                try {

                        if (file == null || file.isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body("Please select a thumbnail image");
                        }

                        Files.createDirectories(thumbnailPath);

                        String fileName = file.getOriginalFilename();

                        if (fileName == null || fileName.isBlank()) {
                                return ResponseEntity.badRequest()
                                                .body("Invalid thumbnail name");
                        }

                        fileName = Paths.get(fileName)
                                        .getFileName()
                                        .toString();

                        String lowerName = fileName.toLowerCase();

                        if (!(lowerName.endsWith(".jpg")
                                        || lowerName.endsWith(".jpeg")
                                        || lowerName.endsWith(".png")
                                        || lowerName.endsWith(".webp"))) {

                                return ResponseEntity.badRequest()
                                                .body(
                                                                "Only JPG, JPEG, PNG and WEBP images are allowed");
                        }

                        Path filePath = thumbnailPath
                                        .resolve(fileName)
                                        .normalize();

                        if (!filePath.startsWith(thumbnailPath)) {
                                return ResponseEntity.badRequest()
                                                .body("Invalid file path");
                        }

                        Files.copy(
                                        file.getInputStream(),
                                        filePath,
                                        StandardCopyOption.REPLACE_EXISTING);

                        System.out.println(
                                        "Thumbnail uploaded successfully: "
                                                        + fileName);

                        return ResponseEntity.ok(
                                        "/thumbnails/" + fileName);

                } catch (IOException e) {

                        e.printStackTrace();

                        return ResponseEntity.internalServerError()
                                        .body(
                                                        "Thumbnail upload failed: "
                                                                        + e.getMessage());
                }
        }
}