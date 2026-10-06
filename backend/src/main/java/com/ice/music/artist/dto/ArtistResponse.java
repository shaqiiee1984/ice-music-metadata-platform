package com.ice.music.artist.dto;

import com.ice.music.artist.model.Artist;
import com.ice.music.artist.model.ArtistAlias;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ArtistResponse(
    UUID id,
    String canonicalName,
    List<String> aliases,
    boolean active,
    Instant createdAt,
    Instant updatedAt,
    Long version
) {
    public static ArtistResponse fromEntity(Artist artist) {
        List<String> aliasNames = artist.getAliases() != null
            ? artist.getAliases().stream().map(ArtistAlias::getAliasName).sorted().toList()
            : List.of();

        return new ArtistResponse(
            artist.getId(),
            artist.getCanonicalName(),
            aliasNames,
            artist.isActive(),
            artist.getCreatedAt(),
            artist.getUpdatedAt(),
            artist.getVersion()
        );
    }
}
