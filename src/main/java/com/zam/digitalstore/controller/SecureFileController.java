package com.zam.digitalstore.controller;

import com.zam.digitalstore.model.Payment;
import com.zam.digitalstore.model.Product;
import com.zam.digitalstore.repository.PaymentRepository;
import com.zam.digitalstore.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/secure")
@CrossOrigin("*")
public class SecureFileController {

    private static final String DOSYA_PREFIX = "DOSYA_FILE_ID:";

    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final String dosyaReadApiKey;

    public SecureFileController(
            PaymentRepository paymentRepository,
            ProductRepository productRepository,
            @Value("${DOSYA_READ_API_KEY:}") String dosyaReadApiKey) {

        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.dosyaReadApiKey = dosyaReadApiKey;
    }

    @GetMapping("/download/{productId}")
    public ResponseEntity<Resource> downloadProduct(
            @PathVariable Long productId,
            @RequestParam String email) {

        try {
            email = email.trim().replace("\"", "").replace("'", "");

            Optional<Payment> payment = paymentRepository
                    .findByCustomerEmailAndProductIdAndStatus(
                            email, productId, "SUCCESS");

            if (payment.isEmpty()) {
                return ResponseEntity.status(403).build();
            }

            Payment p = payment.get();

            if (p.getProductId() == null
                    || !p.getProductId().equals(productId)
                    || !"SUCCESS".equalsIgnoreCase(p.getStatus())) {
                return ResponseEntity.status(403).build();
            }

            Optional<Product> product = productRepository.findById(productId);

            if (product.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String storedFile = product.get().getFileUrl();

            if (storedFile == null || storedFile.isBlank()) {
                return ResponseEntity.notFound().build();
            }

            // dosya.dev file
            if (storedFile.startsWith(DOSYA_PREFIX)) {
                String fileId = storedFile.substring(DOSYA_PREFIX.length()).trim();

                if (fileId.isBlank() || dosyaReadApiKey.isBlank()) {
                    return ResponseEntity.internalServerError().build();
                }

                String apiUrl = "https://api.dosya.dev/api/files/"
                        + fileId + "/download";

                HttpURLConnection apiConnection = (HttpURLConnection) URI.create(apiUrl)
                        .toURL().openConnection();

                apiConnection.setRequestMethod("GET");
                apiConnection.setRequestProperty(
                        "Authorization", "Bearer " + dosyaReadApiKey);
                apiConnection.setConnectTimeout(15000);
                apiConnection.setReadTimeout(60000);
                apiConnection.setInstanceFollowRedirects(false);

                int status = apiConnection.getResponseCode();

                if (status != HttpURLConnection.HTTP_MOVED_TEMP
                        && status != 302) {
                    apiConnection.disconnect();
                    return ResponseEntity.status(502).build();
                }

                String downloadUrl = apiConnection.getHeaderField("Location");
                apiConnection.disconnect();

                if (downloadUrl == null || downloadUrl.isBlank()) {
                    return ResponseEntity.status(502).build();
                }

                URI downloadUri = URI.create(downloadUrl);

                // Accept only HTTPS redirects to Cloudflare R2 storage.
                if (!"https".equalsIgnoreCase(downloadUri.getScheme())
                        || downloadUri.getHost() == null
                        || !downloadUri.getHost()
                                .toLowerCase()
                                .endsWith(".r2.cloudflarestorage.com")) {
                    return ResponseEntity.status(502).build();
                }

                URLConnection fileConnection = downloadUri.toURL().openConnection();
                fileConnection.setConnectTimeout(15000);
                fileConnection.setReadTimeout(60000);

                InputStream input = fileConnection.getInputStream();
                InputStreamResource resource = new InputStreamResource(input);

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(
                                HttpHeaders.CONTENT_DISPOSITION,
                                ContentDisposition.attachment()
                                        .filename(
                                                "study-notes.pdf",
                                                StandardCharsets.UTF_8)
                                        .build().toString())
                        .body(resource);
            }

            // Existing Cloudinary PDF support
            if (storedFile.startsWith("https://")) {
                URI uri = URI.create(storedFile);

                if (!"res.cloudinary.com".equalsIgnoreCase(uri.getHost())
                        || uri.getPath() == null
                        || !uri.getPath().contains("/raw/upload/")) {
                    return ResponseEntity.badRequest().build();
                }

                String fileName = Paths.get(uri.getPath()).getFileName().toString();

                URLConnection connection = uri.toURL().openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(60000);

                InputStream input = connection.getInputStream();

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(
                                HttpHeaders.CONTENT_DISPOSITION,
                                ContentDisposition.attachment()
                                        .filename(
                                                fileName,
                                                StandardCharsets.UTF_8)
                                        .build().toString())
                        .body(new InputStreamResource(input));
            }

            // Existing legacy local-file support
            String fileName = Paths.get(storedFile).getFileName().toString();

            Path filePath = Paths.get(
                    "uploads", "products", fileName)
                    .toAbsolutePath().normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                System.out.println("PDF FILE NOT FOUND: " + fileName);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(
                                            fileName,
                                            StandardCharsets.UTF_8)
                                    .build().toString())
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}