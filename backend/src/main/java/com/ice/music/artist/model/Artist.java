package com.ice.music.artist.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "artists")
public class Artist {

    @Id
    private UUID id;

    @Column(name = "canonical_name", nullable = false)
    private String canonicalName;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ArtistAlias> aliases = new ArrayList<>();

    public Artist() {}

    public Artist(UUID id, String canonicalName) {
        this.id = id != null ? id : UUID.randomUUID();
        this.canonicalName = canonicalName;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.version = 0L;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<ArtistAlias> getAliases() { return aliases; }
    public void setAliases(List<ArtistAlias> aliases) { this.aliases = aliases; }

    public void addAlias(String aliasName) {
        if (aliasName == null || aliasName.trim().isEmpty()) return;
        String trimmed = aliasName.trim();
        boolean exists = aliases.stream()
            .anyMatch(a -> a.getAliasName().equalsIgnoreCase(trimmed));
        if (!exists && !trimmed.equalsIgnoreCase(this.canonicalName)) {
            aliases.add(new ArtistAlias(this, trimmed));
            this.updatedAt = Instant.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artist artist = (Artist) o;
        return Objects.equals(id, artist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
