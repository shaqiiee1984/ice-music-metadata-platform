package com.ice.music.artist.service;

import com.ice.music.artist.dto.ArtistResponse;
import com.ice.music.artist.dto.CreateArtistRequest;
import com.ice.music.artist.dto.UpdateArtistRequest;
import com.ice.music.artist.model.Artist;
import com.ice.music.artist.repository.ArtistRepository;
import com.ice.music.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ArtistService {

    private static final Logger log = LoggerFactory.getLogger(ArtistService.class);

    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Transactional
    @CacheEvict(value = "artistOfTheDay", allEntries = true)
    public ArtistResponse createArtist(CreateArtistRequest request) {
        log.info("Creating new artist: {}", request.canonicalName());
        Artist artist = new Artist(UUID.randomUUID(), request.canonicalName().trim());

        if (request.aliases() != null) {
            for (String alias : request.aliases()) {
                artist.addAlias(alias);
            }
        }

        Artist saved = artistRepository.save(artist);
        return ArtistResponse.fromEntity(saved);
    }

    @Transactional
    @CacheEvict(value = "artistOfTheDay", allEntries = true)
    public ArtistResponse updateArtist(UUID id, UpdateArtistRequest request) {
        log.info("Updating artist id: {}", id);
        Artist artist = artistRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found with id: " + id));

        // Preserve previous canonical name as alias if it is changing
        if (request.canonicalName() != null && !request.canonicalName().trim().isEmpty()) {
            String newName = request.canonicalName().trim();
            if (!newName.equalsIgnoreCase(artist.getCanonicalName())) {
                String oldName = artist.getCanonicalName();
                log.info("Artist {} renaming from '{}' to '{}'. Preserving old name as alias.",
                    id, oldName, newName);
                artist.setCanonicalName(newName);
                artist.addAlias(oldName);
            }
        }

        // Add additional aliases if specified
        if (request.aliases() != null) {
            for (String alias : request.aliases()) {
                artist.addAlias(alias);
            }
        }

        // Toggle active status if provided
        if (request.active() != null) {
            artist.setActive(request.active());
        }

        Artist updated = artistRepository.save(artist);
        return ArtistResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public ArtistResponse getArtistById(UUID id) {
        return artistRepository.findById(id)
            .map(ArtistResponse::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ArtistResponse> listArtists(Pageable pageable) {
        return artistRepository.findAll(pageable)
            .map(ArtistResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ArtistResponse> listAllActive() {
        return artistRepository.findByActiveTrueOrderByCreatedAtAscIdAsc().stream()
            .map(ArtistResponse::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return artistRepository.existsById(id);
    }
}
