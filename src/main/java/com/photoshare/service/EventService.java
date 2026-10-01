package com.photoshare.service;

import com.photoshare.dto.AddMemberRequest;
import com.photoshare.dto.EventRequest;
import com.photoshare.dto.EventResponse;
import com.photoshare.dto.MemberResponse;
import com.photoshare.entity.Event;
import com.photoshare.entity.EventMember;
import com.photoshare.entity.Role;
import com.photoshare.entity.User;
import com.photoshare.exception.ConflictException;
import com.photoshare.repository.EventMemberRepository;
import com.photoshare.repository.EventRepository;
import com.photoshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository events;
    private final EventMemberRepository members;
    private final UserRepository users;
    private final EventAccessService access;
    private final PasswordEncoder encoder;

    @Transactional
    public EventResponse create(EventRequest r, User admin) {
        Event e = new Event();
        e.setName(r.name().trim());
        e.setEventDate(r.eventDate());
        e.setCreatedBy(admin);
        return EventResponse.from(events.save(e));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listFor(User user) {
        List<Event> list = user.getRole() == Role.ADMIN
                ? events.findByCreatedBy(user)
                : events.findAssignedTo(user);
        return list.stream().map(EventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse get(Long eventId, User user) {
        return EventResponse.from(access.requireAccess(eventId, user));
    }

    @Transactional
    public MemberResponse addMember(Long eventId, AddMemberRequest r, User admin) {
        Event event = access.requireOwner(eventId, admin);
        String email = r.email().trim().toLowerCase();

        User member = users.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setName(r.name().trim());
            u.setEmail(email);
            u.setPasswordHash(encoder.encode(r.temporaryPassword()));
            u.setRole(Role.TEAM_MEMBER);
            return users.save(u);
        });

        if (member.getRole() != Role.TEAM_MEMBER) {
            throw new ConflictException("That email belongs to an admin account");
        }
        if (!members.existsByEventAndUser(event, member)) {
            EventMember em = new EventMember();
            em.setEvent(event);
            em.setUser(member);
            members.save(em);
        }
        return MemberResponse.from(member);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(Long eventId, User admin) {
        Event event = access.requireOwner(eventId, admin);
        return members.findByEvent(event).stream()
                .map(m -> MemberResponse.from(m.getUser()))
                .toList();
    }
}