package com.ice.music.track.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CreateTrackRequest(
    @NotNull(message = "artistId is required")
    UUID artistId,

    @NotBlank(message = "Track title must not be blank")
    String title,

    @NotBlank(message = "Genre must not be blank")
    String genre,

    @Positive(message = "durationMs must be a positive integer greater than zero")
    long durationMs,

    String isrc
) {}
