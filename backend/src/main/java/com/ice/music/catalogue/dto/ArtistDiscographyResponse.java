package com.ice.music.catalogue.dto;

import com.ice.music.track.dto.TrackResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public record ArtistDiscographyResponse(
    UUID artistId,
    String canonicalName,
    List<String> aliases,
    List<String> availableGenres,
    Page<TrackResponse> tracks
) {}
