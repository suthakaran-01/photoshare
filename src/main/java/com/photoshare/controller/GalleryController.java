package com.photoshare.controller;

import com.photoshare.dto.PublishRequest;
import com.photoshare.dto.PublishResponse;
import com.photoshare.entity.User;
import com.photoshare.service.GalleryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events/{eventId}/gallery")
@RequiredArgsConstructor
public class GalleryController {
    private final GalleryService galleryService;

    @PostMapping("/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public PublishResponse publish(@PathVariable Long eventId,
                                   @Valid @RequestBody PublishRequest request,
                                   @AuthenticationPrincipal User admin) {
        return galleryService.publish(eventId, request, admin);
    }
}