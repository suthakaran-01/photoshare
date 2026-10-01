package com.photoshare.repository;

import com.photoshare.entity.Event;
import com.photoshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCreatedBy(User admin);

    @Query("select m.event from EventMember m where m.user = :user")
    List<Event> findAssignedTo(@Param("user") User user);
}