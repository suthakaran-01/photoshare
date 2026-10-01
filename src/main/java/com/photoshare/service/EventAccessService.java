package com.photoshare.service;

import com.photoshare.entity.Event;
import com.photoshare.entity.Role;
import com.photoshare.entity.User;
import com.photoshare.exception.ForbiddenException;
import com.photoshare.exception.NotFoundException;
import com.photoshare.repository.EventMemberRepository;
import com.photoshare.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventAccessService {
    private final EventRepository events;
    private final EventMemberRepository members;

    /** Event owner (admin who created it) or an assigned team member. */
    @Transactional(readOnly = true)
    public Event requireAccess(Long eventId, User user) {
        Event event = events.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        boolean owner = user.getRole() == Role.ADMIN
                && event.getCreatedBy().getId().equals(user.getId());
        boolean member = members.existsByEventAndUser(event, user);
        if (!owner && !member) {
            throw new ForbiddenException("No access to this event");
        }
        return event;
    }

    /** Only the admin who created the event. */
    @Transactional(readOnly = true)
    public Event requireOwner(Long eventId, User user) {
        Event event = requireAccess(eventId, user);
        if (user.getRole() != Role.ADMIN
                || !event.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the admin of this event can do this");
        }
        return event;
    }
}