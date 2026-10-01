package com.photoshare.dto;

import com.photoshare.entity.Event;
import java.time.Instant;
import java.time.LocalDate;

public record EventResponse(Long id, String name, LocalDate eventDate, Instant createdAt) {
    public static EventResponse from(Event e) {
        return new EventResponse(e.getId(), e.getName(), e.getEventDate(), e.getCreatedAt());
    }
}