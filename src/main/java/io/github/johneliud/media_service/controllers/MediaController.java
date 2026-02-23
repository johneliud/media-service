package io.github.johneliud.media_service.controllers;

import io.github.johneliud.media_service.dto.ApiResponse;
import io.github.johneliud.media_service.dto.MediaResponse;
import io.github.johneliud.media_service.services.FileStorageService;
import io.github.johneliud.media_service.services.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {
    private final MediaService mediaService;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<MediaResponse>> uploadMedia(
            @RequestPart("image") MultipartFile image,
            @RequestParam("productId") String productId,
            @RequestHeader("X-User-Id") String sellerId,
            @RequestHeader("X-User-Role") String role) {
        
        if (!role.equals("SELLER")) {
            throw new IllegalArgumentException("Only sellers can upload media");
        }
        
        log.info("POST /api/media/upload - Media upload request for productId: {} by seller: {}", productId, sellerId);
        
        MediaResponse mediaResponse = mediaService.uploadMedia(image, productId, sellerId);
        
        log.info("POST /api/media/upload - Media uploaded successfully: {}", mediaResponse.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, "Media uploaded successfully", mediaResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getMedia(@PathVariable String id) {
        log.info("GET /api/media/{} - Media retrieval request", id);
        
        MediaResponse media = mediaService.getMediaById(id);
        Path filePath = fileStorageService.getMediaPath(media.getImagePath());
        
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                log.warn("GET /api/media/{} - File not found", id);
                return ResponseEntity.notFound().build();
            }
            
            String contentType = determineContentType(media.getImagePath());
            
            log.info("GET /api/media/{} - Media retrieved successfully", id);
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                .body(resource);
        } catch (Exception e) {
            log.error("GET /api/media/{} - Error retrieving media", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<MediaResponse>>> getMediaByProduct(@PathVariable String productId) {
        log.info("GET /api/media/product/{} - Media retrieval request", productId);
        
        List<MediaResponse> mediaList = mediaService.getMediaByProductId(productId);
        
        log.info("GET /api/media/product/{} - Retrieved {} media items", productId, mediaList.size());
        return ResponseEntity.ok(new ApiResponse<>(true, "Media retrieved successfully", mediaList));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String sellerId,
            @RequestHeader("X-User-Role") String role) {
        
        if (!role.equals("SELLER")) {
            throw new IllegalArgumentException("Only sellers can delete media");
        }
        
        log.info("DELETE /api/media/{} - Media deletion request by seller: {}", id, sellerId);
        
        mediaService.deleteMedia(id, sellerId);
        
        log.info("DELETE /api/media/{} - Media deleted successfully", id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Media deleted successfully", null));
    }

    @GetMapping("/my-media")
    public ResponseEntity<ApiResponse<List<MediaResponse>>> getSellerMedia(
            @RequestParam(required = false) String productId,
            @RequestHeader("X-User-Id") String sellerId,
            @RequestHeader("X-User-Role") String role) {
        
        if (!role.equals("SELLER")) {
            throw new IllegalArgumentException("Only sellers can access this endpoint");
        }
        
        log.info("GET /api/media/my-media - Seller media request for sellerId: {}, productId: {}", sellerId, productId);
        
        List<MediaResponse> mediaList = mediaService.getSellerMedia(sellerId, productId);
        
        log.info("GET /api/media/my-media - Retrieved {} media items", mediaList.size());
        return ResponseEntity.ok(new ApiResponse<>(true, "Media retrieved successfully", mediaList));
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}