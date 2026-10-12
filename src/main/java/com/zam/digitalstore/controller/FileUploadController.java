package com.zam.digitalstore.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class FileUploadController {

        private static final String DOSYA_API = "https://api.dosya.dev/api";

        private final Cloudinary cloudinary;
        private final String uploadKey;
        private final String readKey;
        private final ObjectMapper mapper = new ObjectMapper();

        public FileUploadController(
                        @Value("${CLOUDINARY_CLOUD_NAME}") String cloudName,
                        @Value("${CLOUDINARY_API_KEY}") String apiKey,
                        @Value("${CLOUDINARY_API_SECRET}") String apiSecret,
                        @Value("${DOSYA_UPLOAD_API_KEY:}") String uploadKey,
                        @Value("${DOSYA_READ_API_KEY:}") String readKey) {

                this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                                "cloud_name", cloudName,
                                "api_key", apiKey,
                                "api_secret", apiSecret,
                                "secure", true));

                this.uploadKey = uploadKey;
                this.readKey = readKey;
        }

        @PostMapping(value = "/upload-file", consumes = "multipart/form-data")
        public ResponseEntity<String> uploadFile(
                        @RequestParam("file") MultipartFile file) {

                try {
                        if (file == null || file.isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body("Please select a PDF file");
                        }

                        String originalName = file.getOriginalFilename();

                        if (originalName == null) {
                                return ResponseEntity.badRequest()
                                                .body("Invalid filename");
                        }

                        String name = Paths.get(
                                        originalName.replace("\\", "/"))
                                        .getFileName().toString();

                        if (!name.toLowerCase().endsWith(".pdf")) {
                                return ResponseEntity.badRequest()
                                                .body("Only PDF files are allowed");
                        }

                        // Simple upload flow supports files up to 50 MB.
                        if (file.getSize() > 50L * 1024 * 1024) {
                                return ResponseEntity.badRequest()
                                                .body("PDF exceeds 50 MB. Resumable upload is required.");
                        }

                        if (uploadKey.isBlank() || readKey.isBlank()) {
                                return ResponseEntity.internalServerError()
                                                .body("Dosya API keys are not configured in Render.");
                        }

                        // Find the real workspace ID using the read-only API key.
                        HttpURLConnection wsConnection = openConnection(
                                        DOSYA_API + "/workspaces", "GET", readKey);

                        int wsStatus = wsConnection.getResponseCode();

                        if (wsStatus < 200 || wsStatus >= 300) {
                                wsConnection.disconnect();
                                return ResponseEntity.internalServerError()
                                                .body("Could not retrieve dosya workspace. HTTP "
                                                                + wsStatus);
                        }

                        JsonNode wsJson;
                        try (InputStream in = wsConnection.getInputStream()) {
                                wsJson = mapper.readTree(in);
                        } finally {
                                wsConnection.disconnect();
                        }

                        JsonNode workspaces = wsJson.path("workspaces");

                        if (!workspaces.isArray() || workspaces.isEmpty()) {
                                return ResponseEntity.internalServerError()
                                                .body("No dosya workspace was returned by the API.");
                        }

                        String workspaceId = workspaces.get(0).path("id").asText("");

                        if (workspaceId.isBlank()) {
                                return ResponseEntity.internalServerError()
                                                .body("Workspace ID was missing in the API response.");
                        }

                        // Initialize upload.
                        String requestJson = mapper.createObjectNode()
                                        .put("file_name", name)
                                        .put("file_size", file.getSize())
                                        .put("workspace_id", workspaceId)
                                        .put("mime_type", "application/pdf")
                                        .toString();

                        HttpURLConnection initConnection = openConnection(
                                        DOSYA_API + "/upload/init", "POST", uploadKey);

                        initConnection.setDoOutput(true);
                        initConnection.setRequestProperty(
                                        "Content-Type", "application/json");

                        try (OutputStream out = initConnection.getOutputStream()) {
                                out.write(requestJson.getBytes(StandardCharsets.UTF_8));
                        }

                        int initStatus = initConnection.getResponseCode();

                        if (initStatus < 200 || initStatus >= 300) {
                                String error = readResponse(initConnection);
                                initConnection.disconnect();

                                return ResponseEntity.status(502)
                                                .body("Dosya upload initialization failed. HTTP "
                                                                + initStatus + ": " + error);
                        }

                        JsonNode initJson;
                        try (InputStream in = initConnection.getInputStream()) {
                                initJson = mapper.readTree(in);
                        } finally {
                                initConnection.disconnect();
                        }

                        String uploadUrl = initJson.path("upload_url").asText("");

                        if (uploadUrl.isBlank()) {
                                return ResponseEntity.status(502)
                                                .body("Dosya API did not return upload_url.");
                        }

                        if (uploadUrl.startsWith("/")) {
                                uploadUrl = DOSYA_API + uploadUrl.substring(4);
                        }

                        // Upload the raw PDF bytes.
                        URI uploadUri = URI.create(uploadUrl);

                        if (!"https".equalsIgnoreCase(uploadUri.getScheme())
                                        || !"api.dosya.dev".equalsIgnoreCase(
                                                        uploadUri.getHost())) {
                                return ResponseEntity.status(502)
                                                .body("Unexpected dosya upload URL.");
                        }

                        HttpURLConnection putConnection = openConnection(
                                        uploadUrl, "PUT", uploadKey);

                        putConnection.setDoOutput(true);
                        putConnection.setRequestProperty(
                                        "Content-Type", "application/octet-stream");

                        try (OutputStream out = putConnection.getOutputStream()) {
                                out.write(file.getBytes());
                        }

                        int putStatus = putConnection.getResponseCode();

                        if (putStatus < 200 || putStatus >= 300) {
                                String error = readResponse(putConnection);
                                putConnection.disconnect();

                                return ResponseEntity.status(502)
                                                .body("Dosya PDF upload failed. HTTP "
                                                                + putStatus + ": " + error);
                        }

                        JsonNode putJson;
                        try (InputStream in = putConnection.getInputStream()) {
                                putJson = mapper.readTree(in);
                        } finally {
                                putConnection.disconnect();
                        }

                        String fileId = putJson.path("file").path("id").asText("");

                        if (fileId.isBlank()) {
                                return ResponseEntity.status(502)
                                                .body("Upload succeeded but the file ID was missing.");
                        }

                        System.out.println("PDF uploaded to dosya.dev: " + fileId);

                        // SecureFileController recognizes this prefix.
                        return ResponseEntity.ok("DOSYA_FILE_ID:" + fileId);

                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.internalServerError()
                                        .body("PDF upload failed. Check Render logs.");
                }
        }

        private HttpURLConnection openConnection(
                        String url, String method, String key) throws Exception {

                HttpURLConnection connection = (HttpURLConnection) URI.create(url)
                                .toURL().openConnection();

                connection.setRequestMethod(method);
                connection.setRequestProperty(
                                "Authorization", "Bearer " + key);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(60000);
                connection.setInstanceFollowRedirects(false);

                return connection;
        }

        private String readResponse(HttpURLConnection connection) {
                try {
                        InputStream stream = connection.getErrorStream();

                        if (stream == null) {
                                stream = connection.getInputStream();
                        }

                        try (InputStream in = stream) {
                                return new String(
                                                in.readAllBytes(), StandardCharsets.UTF_8);
                        }
                } catch (Exception ignored) {
                        return "No error details returned.";
                }
        }

        @PostMapping(value = "/upload-preview", consumes = "multipart/form-data")
        public ResponseEntity<String> uploadPreview(
                        @RequestParam("file") MultipartFile file) {
                return uploadImage(file, "previews");
        }

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

                        String originalName = file.getOriginalFilename();

                        if (originalName == null) {
                                return ResponseEntity.badRequest()
                                                .body("Invalid filename");
                        }

                        String name = Paths.get(
                                        originalName.replace("\\", "/"))
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

                        return ResponseEntity.ok(
                                        result.get("secure_url").toString());

                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.internalServerError()
                                        .body("Image upload failed. Check server logs.");
                }
        }
}