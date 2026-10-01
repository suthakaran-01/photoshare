package com.photoshare.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final Cloudinary cloudinary;

    public StoredFile store(MultipartFile file, Long eventId) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "events/" + eventId,
                "resource_type", "image"));
        return new StoredFile((String) result.get("secure_url"), (String) result.get("public_id"));
    }

    public void delete(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    public record StoredFile(String url, String publicId) {}
}