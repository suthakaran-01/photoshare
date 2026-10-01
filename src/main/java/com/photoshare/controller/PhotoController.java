package com.photoshare.controller;

import com.photoshare.dto.PhotoResponse;
import com.photoshare.dto.UploadResult;
import com.photoshare.entity.User;
import com.photoshare.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/photos")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService photoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResult upload(@PathVariable Long eventId,
                               @RequestPart("files") List<MultipartFile> files,
                               @AuthenticationPrincipal User user) {
        return photoService.upload(eventId, files, user);
    }

    @GetMapping
    public List<PhotoResponse> list(@PathVariable Long eventId,
                                    @AuthenticationPrincipal User user) {
        return photoService.list(eventId, user);
    }
}