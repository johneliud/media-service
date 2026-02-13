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
            @RequestParam("productId") String productId) {
        
        log.info("POST /api/media/upload - Media upload request for productId: {}", productId);
        
        MediaResponse mediaResponse = mediaService.uploadMedia(image, productId);
        
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

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "" -> "image/jpg";
            case "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}