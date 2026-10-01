package com.photoshare.repository;

import com.photoshare.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByEventId(Long eventId);
    List<Photo> findByEventIdAndUploadedById(Long eventId, Long userId);
    List<Photo> findByIdInAndEventId(Collection<Long> ids, Long eventId);
    
}