package com.photoshare.controller;

import com.photoshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {
    private final UserRepository users;

    @GetMapping("/api/health")
    public String health() {
        long count = users.count(); // forces a real database query
        return "OK - " + count + " users";
    }
}