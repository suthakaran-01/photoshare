package com.photoshare.controller;

import com.photoshare.dto.AddMemberRequest;
import com.photoshare.dto.EventRequest;
import com.photoshare.dto.EventResponse;
import com.photoshare.dto.MemberResponse;
import com.photoshare.entity.User;
import com.photoshare.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody EventRequest req,
                                @AuthenticationPrincipal User admin) {
        return eventService.create(req, admin);
    }

    @GetMapping
    public List<EventResponse> mine(@AuthenticationPrincipal User user) {
        return eventService.listFor(user);
    }

    @GetMapping("/{id}")
    public EventResponse one(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return eventService.get(id, user);
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse addMember(@PathVariable Long id,
                                    @Valid @RequestBody AddMemberRequest req,
                                    @AuthenticationPrincipal User admin) {
        return eventService.addMember(id, req, admin);
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberResponse> members(@PathVariable Long id,
                                        @AuthenticationPrincipal User admin) {
        return eventService.listMembers(id, admin);
    }
}