package io.github.johneliud.media_service.services;

import io.github.johneliud.media_service.dto.MediaResponse;
import io.github.johneliud.media_service.models.Media;
import io.github.johneliud.media_service.repositories.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;

    public MediaResponse uploadMedia(MultipartFile file, String productId) {
        log.info("Attempting to upload media for productId: {}", productId);

        if (productId == null || productId.isBlank()) {
            log.warn("Media upload failed: productId is required");
            throw new IllegalArgumentException("productId is required");
        }

        String imagePath = fileStorageService.storeMedia(file);

        Media media = new Media();
        media.setImagePath(imagePath);
        media.setProductId(productId);

        Media savedMedia = mediaRepository.save(media);
        log.info("Media uploaded successfully with ID: {} for productId: {}", savedMedia.getId(), productId);

        return toMediaResponse(savedMedia);
    }

    public MediaResponse getMediaById(String id) {
        log.info("Fetching media by ID: {}", id);
        
        Media media = mediaRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Media not found with ID: {}", id);
                return new IllegalArgumentException("Media not found");
            });
        
        log.info("Media retrieved successfully: {}", id);
        return toMediaResponse(media);
    }

    public java.util.List<MediaResponse> getMediaByProductId(String productId) {
        log.info("Fetching media for productId: {}", productId);
        
        java.util.List<Media> mediaList = mediaRepository.findByProductId(productId);
        
        log.info("Retrieved {} media items for productId: {}", mediaList.size(), productId);
        return mediaList.stream()
            .map(this::toMediaResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    private MediaResponse toMediaResponse(Media media) {
        return new MediaResponse(
            media.getId(),
            media.getImagePath(),
            media.getProductId()
        );
    }
}
