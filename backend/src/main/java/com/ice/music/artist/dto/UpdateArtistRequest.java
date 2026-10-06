package com.ice.music.artist.dto;

import java.util.List;

public record UpdateArtistRequest(
    String canonicalName,
    List<String> aliases,
    Boolean active
) {}
