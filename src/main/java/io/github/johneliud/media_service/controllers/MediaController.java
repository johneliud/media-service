package io.github.johneliud.media_service.controllers;

import io.github.johneliud.media_service.dto.ApiResponse;
import io.github.johneliud.media_service.dto.MediaResponse;
import io.github.johneliud.media_service.services.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {
    private final MediaService mediaService;

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
}
