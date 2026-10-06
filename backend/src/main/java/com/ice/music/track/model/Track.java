package com.ice.music.track.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    private UUID id;

    @Column(name = "artist_id", nullable = false)
    private UUID artistId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "genre", nullable = false)
    private String genre;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "isrc")
    private String isrc;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Track() {}

    public Track(UUID id, UUID artistId, String title, String genre, long durationMs, String isrc) {
        this.id = id != null ? id : UUID.randomUUID();
        this.artistId = artistId;
        this.title = title;
        this.genre = genre;
        this.durationMs = durationMs;
        this.isrc = isrc;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.version = 0L;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getArtistId() { return artistId; }
    public void setArtistId(UUID artistId) { this.artistId = artistId; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public String getGenre() { return genre; }
    public void setGenre(String genre) {
        this.genre = genre;
        this.updatedAt = Instant.now();
    }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
        this.updatedAt = Instant.now();
    }

    public String getIsrc() { return isrc; }
    public void setIsrc(String isrc) {
        this.isrc = isrc;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(id, track.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
