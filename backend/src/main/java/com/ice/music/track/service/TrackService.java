package com.ice.music.track.service;

import com.ice.music.artist.service.ArtistService;
import com.ice.music.shared.exception.ResourceNotFoundException;
import com.ice.music.track.dto.CreateTrackRequest;
import com.ice.music.track.dto.TrackResponse;
import com.ice.music.track.model.Track;
import com.ice.music.track.repository.TrackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TrackService {

    private static final Logger log = LoggerFactory.getLogger(TrackService.class);

    private final TrackRepository trackRepository;
    private final ArtistService artistService;

    public TrackService(TrackRepository trackRepository, ArtistService artistService) {
        this.trackRepository = trackRepository;
        this.artistService = artistService;
    }

    @Transactional
    public TrackResponse createTrack(CreateTrackRequest request) {
        log.info("Adding track '{}' for artist id {}", request.title(), request.artistId());

        // Domain Rule: Validate artist existence before adding track
        if (!artistService.existsById(request.artistId())) {
            throw new ResourceNotFoundException("Cannot add track. Artist not found with id: " + request.artistId());
        }

        Track track = new Track(
            UUID.randomUUID(),
            request.artistId(),
            request.title().trim(),
            request.genre().trim(),
            request.durationMs(),
            request.isrc() != null ? request.isrc().trim() : null
        );

        Track saved = trackRepository.save(track);
        return TrackResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TrackResponse getTrackById(UUID id) {
        return trackRepository.findById(id)
            .map(TrackResponse::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<TrackResponse> getTracksByArtist(UUID artistId, String genre, Pageable pageable) {
        if (genre != null && !genre.trim().isEmpty()) {
            return trackRepository.findByArtistIdAndGenreIgnoreCase(artistId, genre.trim(), pageable)
                .map(TrackResponse::fromEntity);
        }
        return trackRepository.findByArtistId(artistId, pageable)
            .map(TrackResponse::fromEntity);
    }
}
