package com.ice.music.artistofday.dto;

import java.util.List;
import java.util.UUID;

public record ArtistOfTheDayResponse(
    UUID id,
    String canonicalName,
    List<String> aliases,
    String utcDate,
    long cycleDayIndex,
    long totalActiveArtists,
    long nextRotationSecondsRemaining
) {}
