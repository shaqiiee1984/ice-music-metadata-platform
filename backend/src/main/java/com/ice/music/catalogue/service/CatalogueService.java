package com.ice.music.catalogue.service;

import com.ice.music.artist.dto.ArtistResponse;
import com.ice.music.artist.service.ArtistService;
import com.ice.music.catalogue.dto.ArtistDiscographyResponse;
import com.ice.music.track.dto.TrackResponse;
import com.ice.music.track.repository.TrackRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CatalogueService {

    private final ArtistService artistService;
    private final TrackRepository trackRepository;

    public CatalogueService(ArtistService artistService, TrackRepository trackRepository) {
        this.artistService = artistService;
        this.trackRepository = trackRepository;
    }

    @Transactional(readOnly = true)
    public ArtistDiscographyResponse getArtistTracks(UUID artistId, String genre, Pageable pageable) {
        ArtistResponse artist = artistService.getArtistById(artistId);

        List<String> distinctGenres = trackRepository.findDistinctGenresByArtistId(artistId);

        Page<TrackResponse> trackPage;
        if (genre != null && !genre.trim().isEmpty()) {
            trackPage = trackRepository.findByArtistIdAndGenreIgnoreCase(artistId, genre.trim(), pageable)
                .map(TrackResponse::fromEntity);
        } else {
            trackPage = trackRepository.findByArtistId(artistId, pageable)
                .map(TrackResponse::fromEntity);
        }

        return new ArtistDiscographyResponse(
            artist.id(),
            artist.canonicalName(),
            artist.aliases(),
            distinctGenres,
            trackPage
        );
    }
}
