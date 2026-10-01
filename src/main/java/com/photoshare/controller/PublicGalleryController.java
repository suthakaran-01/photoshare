package com.photoshare.controller;

import com.photoshare.dto.PinRequest;
import com.photoshare.dto.PublicPhoto;
import com.photoshare.dto.VerifyResponse;
import com.photoshare.service.PublicGalleryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/public/gallery")
@RequiredArgsConstructor
public class PublicGalleryController {
    private final PublicGalleryService service;

    @PostMapping("/{token}/verify")
    public VerifyResponse verify(@PathVariable String token,
                                 @Valid @RequestBody PinRequest body,
                                 HttpServletRequest request) {
        return service.verify(token, body.pin(), request.getRemoteAddr());
    }

    @GetMapping("/{token}/photos")
    public List<PublicPhoto> photos(@PathVariable String token,
                                    @RequestHeader("X-Gallery-Access") String accessToken) {
        return service.photos(token, accessToken);
    }
}