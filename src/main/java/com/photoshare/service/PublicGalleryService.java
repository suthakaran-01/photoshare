package com.photoshare.service;

import com.photoshare.dto.PublicPhoto;
import com.photoshare.dto.VerifyResponse;
import com.photoshare.entity.Gallery;
import com.photoshare.exception.NotFoundException;
import com.photoshare.exception.TooManyRequestsException;
import com.photoshare.exception.UnauthorizedException;
import com.photoshare.repository.GalleryRepository;
import com.photoshare.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.Deque;

@Service
@RequiredArgsConstructor
public class PublicGalleryService {
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000;

    private final GalleryRepository galleries;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final Map<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    public VerifyResponse verify(String token, String pin, String clientKey) {
        Gallery gallery = galleries.findByShareTokenAndPublishedTrue(token)
                .orElseThrow(() -> new NotFoundException("Gallery not found"));

        String key = token + "|" + clientKey;
        if (tooManyAttempts(key)) {
            throw new TooManyRequestsException("Too many attempts. Try again later");
        }
        if (!encoder.matches(pin, gallery.getPinHash())) {
            recordFailure(key);
            throw new UnauthorizedException("Incorrect PIN");
        }
        attempts.remove(key);
        return new VerifyResponse(jwtUtil.generateGalleryAccess(token));
    }

    @Transactional(readOnly = true)
    public List<PublicPhoto> photos(String token, String accessToken) {
        Gallery gallery = galleries.findByShareTokenAndPublishedTrue(token)
                .orElseThrow(() -> new NotFoundException("Gallery not found"));
        if (accessToken == null || !jwtUtil.isValidGalleryAccess(accessToken, token)) {
            throw new UnauthorizedException("PIN verification required");
        }
        return gallery.getPhotos().stream()
                .map(p -> new PublicPhoto(p.getId(), p.getStorageUrl()))
                .toList();
    }

    private boolean tooManyAttempts(String key) {
        Deque<Long> times = attempts.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        long now = System.currentTimeMillis();
        while (!times.isEmpty() && now - times.peekFirst() > WINDOW_MS) {
            times.pollFirst();
        }
        return times.size() >= MAX_ATTEMPTS;
    }

    private void recordFailure(String key) {
        attempts.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>())
                .addLast(System.currentTimeMillis());
    }
}