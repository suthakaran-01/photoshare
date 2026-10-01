package com.photoshare.service;

import com.photoshare.dto.PublishRequest;
import com.photoshare.dto.PublishResponse;
import com.photoshare.entity.Event;
import com.photoshare.entity.Gallery;
import com.photoshare.entity.Photo;
import com.photoshare.entity.User;
import com.photoshare.repository.GalleryRepository;
import com.photoshare.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GalleryService {
    private final EventAccessService access;
    private final PhotoRepository photos;
    private final GalleryRepository galleries;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public PublishResponse publish(Long eventId, PublishRequest request, User admin) {
        Event event = access.requireOwner(eventId, admin);

        Set<Long> requestedIds = new HashSet<>(request.photoIds());
        List<Photo> selected = photos.findByIdInAndEventId(requestedIds, eventId);
        if (selected.size() != requestedIds.size()) {
            throw new IllegalArgumentException("Some photo ids do not belong to this event");
        }

        Gallery gallery = galleries.findByEventId(eventId).orElseGet(() -> {
            Gallery g = new Gallery();
            g.setEvent(event);
            g.setShareToken(newToken());
            return g;
        });

        String pin = String.format("%06d", random.nextInt(1_000_000));
        gallery.setPinHash(encoder.encode(pin));
        gallery.setPhotos(new HashSet<>(selected));
        gallery.setPublished(true);
        gallery.setPublishedAt(Instant.now());
        galleries.save(gallery);

        String url = baseUrl + "/gallery.html?t=" + gallery.getShareToken();
        return new PublishResponse(url, pin, selected.size());
    }

    private String newToken() {
        byte[] bytes = new byte[18];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}