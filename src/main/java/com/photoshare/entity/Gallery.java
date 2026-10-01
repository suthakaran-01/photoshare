package com.photoshare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "galleries")
@Getter @Setter @NoArgsConstructor
public class Gallery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", unique = true)
    private Event event;

    @Column(unique = true, nullable = false)
    private String shareToken;

    private String pinHash;

    private boolean published;

    private Instant publishedAt;

    private Instant createdAt = Instant.now();

    @ManyToMany
    @JoinTable(name = "gallery_photos",
        joinColumns = @JoinColumn(name = "gallery_id"),
        inverseJoinColumns = @JoinColumn(name = "photo_id"))
    private Set<Photo> photos = new HashSet<>();
}