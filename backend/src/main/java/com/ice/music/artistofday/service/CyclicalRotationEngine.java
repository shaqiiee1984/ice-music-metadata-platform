package com.ice.music.artistofday.service;

import com.ice.music.artist.model.Artist;
import com.ice.music.artist.model.ArtistAlias;
import com.ice.music.artist.repository.ArtistRepository;
import com.ice.music.artistofday.dto.ArtistOfTheDayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CyclicalRotationEngine {

    private static final Logger log = LoggerFactory.getLogger(CyclicalRotationEngine.class);

    private final ArtistRepository artistRepository;

    public CyclicalRotationEngine(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    /**
     * Deterministically calculates the Artist of the Day for any given UTC date.
     * Formula: index = daysSinceEpoch mod n (with stable ordering by created_at ASC, id ASC)
     */
    @Cacheable(value = "artistOfTheDay", key = "#date.toString()")
    @Transactional(readOnly = true)
    public Optional<ArtistOfTheDayResponse> getArtistForDate(LocalDate date) {
        long totalActive = artistRepository.countByActiveTrue();
        if (totalActive == 0) {
            log.warn("Catalog has 0 active artists. Featured artist unavailable for date {}", date);
            return Optional.empty();
        }

        long daysSinceEpoch = date.toEpochDay();
        // Mathematical non-negative modulo: index in [0, totalActive - 1]
        int targetIndex = (int) Math.floorMod(daysSinceEpoch, totalActive);

        log.info("Computing Artist of the Day for date {} (epochDay={}): targetIndex={} of {} active artists",
            date, daysSinceEpoch, targetIndex, totalActive);

        Page<Artist> page = artistRepository.findActiveOrdered(PageRequest.of(targetIndex, 1));
        if (page.isEmpty()) {
            return Optional.empty();
        }

        Artist artist = page.getContent().get(0);
        List<String> aliases = artist.getAliases() != null
            ? artist.getAliases().stream().map(ArtistAlias::getAliasName).sorted().toList()
            : List.of();

        long secondsUntilMidnight = calculateSecondsUntilUtcMidnight();

        return Optional.of(new ArtistOfTheDayResponse(
            artist.getId(),
            artist.getCanonicalName(),
            aliases,
            date.toString(),
            targetIndex + 1,
            totalActive,
            secondsUntilMidnight
        ));
    }

    public Optional<ArtistOfTheDayResponse> getTodayArtist() {
        return getArtistForDate(LocalDate.now(ZoneOffset.UTC));
    }

    private long calculateSecondsUntilUtcMidnight() {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime midnightUtc = nowUtc.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC);
        return Math.max(0, Duration.between(nowUtc, midnightUtc).getSeconds());
    }
}
