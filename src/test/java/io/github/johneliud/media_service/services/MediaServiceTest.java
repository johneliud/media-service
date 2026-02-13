package io.github.johneliud.media_service.services;

import io.github.johneliud.media_service.dto.MediaResponse;
import io.github.johneliud.media_service.models.Media;
import io.github.johneliud.media_service.repositories.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private MediaService mediaService;

    @Mock
    private MultipartFile mockFile;

    private Media testMedia;

    @BeforeEach
    void setUp() {
        testMedia = new Media();
        testMedia.setId("media123");
        testMedia.setImagePath("test-image.jpg");
        testMedia.setProductId("product123");
        testMedia.setSellerId("seller123");
    }

    @Test
    void uploadMedia_Success() {
        when(fileStorageService.storeMedia(mockFile)).thenReturn("test-image.jpg");
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);

        MediaResponse response = mediaService.uploadMedia(mockFile, "product123", "seller123");

        assertNotNull(response);
        assertEquals("media123", response.getId());
        assertEquals("test-image.jpg", response.getImagePath());
        assertEquals("product123", response.getProductId());
        assertEquals("seller123", response.getSellerId());
        verify(fileStorageService).storeMedia(mockFile);
        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    void uploadMedia_NullProductId_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            mediaService.uploadMedia(mockFile, null, "seller123");
        });
        assertEquals("productId is required", exception.getMessage());
    }

    @Test
    void deleteMedia_Success() {
        when(mediaRepository.findById("media123")).thenReturn(Optional.of(testMedia));

        mediaService.deleteMedia("media123", "seller123");

        verify(fileStorageService).deleteMedia("test-image.jpg");
        verify(mediaRepository).deleteById("media123");
    }

    @Test
    void deleteMedia_WrongSeller_ThrowsException() {
        when(mediaRepository.findById("media123")).thenReturn(Optional.of(testMedia));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            mediaService.deleteMedia("media123", "wrongSeller");
        });
        assertEquals("You do not have permission to delete this media", exception.getMessage());
    }

    @Test
    void deleteMedia_NotFound_ThrowsException() {
        when(mediaRepository.findById("media123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            mediaService.deleteMedia("media123", "seller123");
        });
        assertEquals("Media not found", exception.getMessage());
    }

    @Test
    void getMediaById_Success() {
        when(mediaRepository.findById("media123")).thenReturn(Optional.of(testMedia));

        MediaResponse response = mediaService.getMediaById("media123");

        assertNotNull(response);
        assertEquals("media123", response.getId());
        verify(mediaRepository).findById("media123");
    }
}
