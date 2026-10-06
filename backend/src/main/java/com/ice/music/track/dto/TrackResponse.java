package com.ice.music.track.dto;

import com.ice.music.track.model.Track;

import java.time.Instant;
import java.util.UUID;

public record TrackResponse(
    UUID id,
    UUID artistId,
    String title,
    String genre,
    long durationMs,
    String durationFormatted,
    String isrc,
    Instant createdAt,
    Instant updatedAt,
    Long version
) {
    public static TrackResponse fromEntity(Track track) {
        long totalSeconds = track.getDurationMs() / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        String formatted = String.format("%02d:%02d", minutes, seconds);

        return new TrackResponse(
            track.getId(),
            track.getArtistId(),
            track.getTitle(),
            track.getGenre(),
            track.getDurationMs(),
            formatted,
            track.getIsrc(),
            track.getCreatedAt(),
            track.getUpdatedAt(),
            track.getVersion()
        );
    }
}
