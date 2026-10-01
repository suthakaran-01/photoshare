package com.photoshare.repository;

import com.photoshare.entity.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GalleryRepository extends JpaRepository<Gallery, Long> {
    Optional<Gallery> findByEventId(Long eventId);
    Optional<Gallery> findByShareTokenAndPublishedTrue(String token);
}