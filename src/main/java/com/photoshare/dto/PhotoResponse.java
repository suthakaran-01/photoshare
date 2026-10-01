package com.photoshare.dto;

import com.photoshare.entity.Photo;
import java.time.Instant;

public record PhotoResponse(Long id, String filename, String storageUrl, long fileSize, Instant createdAt) {
    public static PhotoResponse from(Photo p) {
        return new PhotoResponse(p.getId(), p.getFilename(), p.getStorageUrl(), p.getFileSize(), p.getCreatedAt());
    }
}