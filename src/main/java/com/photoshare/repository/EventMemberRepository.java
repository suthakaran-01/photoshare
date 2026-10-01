package com.photoshare.repository;

import com.photoshare.entity.Event;
import com.photoshare.entity.EventMember;
import com.photoshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventMemberRepository extends JpaRepository<EventMember, Long> {
    boolean existsByEventAndUser(Event event, User user);
    List<EventMember> findByEvent(Event event);
}