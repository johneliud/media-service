package io.github.johneliud.media_service.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg", "webp");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/png", "image/jpeg", "image/jpg", "image/webp"
    );
    
    @Value("${file.upload.dir:uploads/media}")
    private String uploadDir;

    public String storeMedia(MultipartFile file) {
        log.info("Attempting to store media file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("Media upload failed: File is empty");
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("Media upload failed: File size {} exceeds 2MB limit", file.getSize());
            throw new IllegalArgumentException("File size exceeds 2MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Media upload failed: Invalid MIME type - {}", contentType);
            throw new IllegalArgumentException("Only PNG, JPG, JPEG, and WEBP files are allowed");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            log.warn("Media upload failed: Invalid file extension - {}", extension);
            throw new IllegalArgumentException("Only PNG, JPG, JPEG, and WEBP files are allowed");
        }

        validateImageIntegrity(file);

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath);
            }

            String filename = UUID.randomUUID() + "." + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Media stored successfully: {}", filename);
            return filename;
        } catch (IOException e) {
            log.error("Failed to store media file", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private void validateImageIntegrity(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < 8) {
                log.warn("Image validation failed: File too small to be a valid image");
                throw new IllegalArgumentException("Invalid image file");
            }

            // Check magic bytes for common image formats
            if (isPNG(bytes) || isJPEG(bytes) || isWEBP(bytes)) {
                return;
            }

            log.warn("Image validation failed: File does not match expected image format");
            throw new IllegalArgumentException("Invalid image file");
        } catch (IOException e) {
            log.error("Failed to validate image integrity", e);
            throw new RuntimeException("Failed to validate image", e);
        }
    }

    private boolean isPNG(byte[] bytes) {
        return bytes.length >= 8 &&
               bytes[0] == (byte) 0x89 && bytes[1] == 0x50 &&
               bytes[2] == 0x4E && bytes[3] == 0x47 &&
               bytes[4] == 0x0D && bytes[5] == 0x0A &&
               bytes[6] == 0x1A && bytes[7] == 0x0A;
    }

    private boolean isJPEG(byte[] bytes) {
        return bytes.length >= 3 &&
               bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 &&
               bytes[2] == (byte) 0xFF;
    }

    private boolean isWEBP(byte[] bytes) {
        return bytes.length >= 12 &&
               bytes[0] == 0x52 && bytes[1] == 0x49 &&
               bytes[2] == 0x46 && bytes[3] == 0x46 &&
               bytes[8] == 0x57 && bytes[9] == 0x45 &&
               bytes[10] == 0x42 && bytes[11] == 0x50;
    }

    public void deleteMedia(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
            log.info("Media deleted: {}", filename);
        } catch (IOException e) {
            log.error("Failed to delete media: {}", filename, e);
        }
    }

    public Path getMediaPath(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}