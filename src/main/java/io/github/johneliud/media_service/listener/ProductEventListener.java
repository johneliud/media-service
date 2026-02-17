package io.github.johneliud.media_service.listener;

import io.github.johneliud.media_service.event.ProductDeletedEvent;
import io.github.johneliud.media_service.models.Media;
import io.github.johneliud.media_service.repositories.MediaRepository;
import io.github.johneliud.media_service.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;

    @KafkaListener(topics = "product-deleted", groupId = "media-service")
    public void handleProductDeleted(ProductDeletedEvent event) {
        log.info("Received product-deleted event for productId: {}", event.getProductId());
        
        List<Media> mediaList = mediaRepository.findByProductId(event.getProductId());
        
        for (Media media : mediaList) {
            try {
                fileStorageService.deleteMedia(media.getImagePath());
                mediaRepository.deleteById(media.getId());
                log.info("Deleted media: {} for product: {}", media.getId(), event.getProductId());
            } catch (Exception e) {
                log.error("Failed to delete media: {} - {}", media.getId(), e.getMessage());
            }
        }
        
        log.info("Cascading deletion completed for product: {}, deleted {} media files", 
                event.getProductId(), mediaList.size());
    }
}
