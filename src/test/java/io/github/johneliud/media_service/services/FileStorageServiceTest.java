package io.github.johneliud.media_service.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeMedia_ValidImage_Success() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());

        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile file = new MockMultipartFile("image", "test.png", "image/png", pngBytes);

        String filename = service.storeMedia(file);

        assertNotNull(filename);
        assertTrue(filename.endsWith(".png"));
    }

    @Test
    void storeMedia_FileTooLarge_ThrowsException() {
        FileStorageService service = new FileStorageService();
        byte[] largeFile = new byte[3 * 1024 * 1024]; // 3MB
        MockMultipartFile file = new MockMultipartFile("image", "test.png", "image/png", largeFile);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.storeMedia(file);
        });
        assertEquals("File size exceeds 2MB limit", exception.getMessage());
    }

    @Test
    void storeMedia_InvalidMimeType_ThrowsException() {
        FileStorageService service = new FileStorageService();
        byte[] content = new byte[]{1, 2, 3, 4};
        MockMultipartFile file = new MockMultipartFile("image", "test.txt", "text/plain", content);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.storeMedia(file);
        });
        assertTrue(exception.getMessage().contains("Only PNG, JPG, JPEG"));
    }

    @Test
    void storeMedia_InvalidExtension_ThrowsException() {
        FileStorageService service = new FileStorageService();
        byte[] content = new byte[]{1, 2, 3, 4};
        MockMultipartFile file = new MockMultipartFile("image", "test.txt", "image/png", content);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.storeMedia(file);
        });
        assertTrue(exception.getMessage().contains("Only PNG, JPG, JPEG"));
    }

    @Test
    void storeMedia_InvalidImageContent_ThrowsException() {
        FileStorageService service = new FileStorageService();
        byte[] invalidImage = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        MockMultipartFile file = new MockMultipartFile("image", "test.png", "image/png", invalidImage);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.storeMedia(file);
        });
        assertEquals("Invalid image file", exception.getMessage());
    }

    @Test
    void storeMedia_EmptyFile_ThrowsException() {
        FileStorageService service = new FileStorageService();
        MockMultipartFile file = new MockMultipartFile("image", "test.png", "image/png", new byte[0]);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.storeMedia(file);
        });
        assertEquals("File is empty", exception.getMessage());
    }
}
