package com.ice.music.artist.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateArtistRequest(
    @NotBlank(message = "Canonical name must not be blank")
    String canonicalName,
    List<String> aliases
) {}
