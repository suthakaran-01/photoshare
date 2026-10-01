package com.photoshare.service;

import com.photoshare.dto.PhotoResponse;
import com.photoshare.dto.UploadResult;
import com.photoshare.entity.Event;
import com.photoshare.entity.Photo;
import com.photoshare.entity.Role;
import com.photoshare.entity.User;
import com.photoshare.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_BYTES = 10L * 1024 * 1024;

    private final EventAccessService access;
    private final StorageService storage;
    private final PhotoRepository photos;

    @Transactional
    public UploadResult upload(Long eventId, List<MultipartFile> files, User user) {
        Event event = access.requireAccess(eventId, user);
        List<PhotoResponse> uploaded = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (MultipartFile file : files) {
            String name = file.getOriginalFilename() == null ? "unnamed" : file.getOriginalFilename();
            try {
                if (file.isEmpty()) {
                    throw new IllegalArgumentException("file is empty");
                }
                if (!ALLOWED_TYPES.contains(file.getContentType())) {
                    throw new IllegalArgumentException("unsupported file type");
                }
                if (file.getSize() > MAX_BYTES) {
                    throw new IllegalArgumentException("file exceeds 10MB limit");
                }
                StorageService.StoredFile stored = storage.store(file, eventId);

                Photo photo = new Photo();
                photo.setEvent(event);
                photo.setUploadedBy(user);
                photo.setFilename(name);
                photo.setStorageUrl(stored.url());
                photo.setStoragePublicId(stored.publicId());
                photo.setFileSize(file.getSize());
                uploaded.add(PhotoResponse.from(photos.save(photo)));
            } catch (Exception ex) {
                failed.add(name + ": " + ex.getMessage());
            }
        }
        return new UploadResult(uploaded, failed);
    }

    @Transactional(readOnly = true)
    public List<PhotoResponse> list(Long eventId, User user) {
        access.requireAccess(eventId, user);
        List<Photo> list = user.getRole() == Role.ADMIN
                ? photos.findByEventId(eventId)
                : photos.findByEventIdAndUploadedById(eventId, user.getId());
        return list.stream().map(PhotoResponse::from).toList();
    }
}