package com.ice.music.artistofday;

import com.ice.music.artist.model.Artist;
import com.ice.music.artist.repository.ArtistRepository;
import com.ice.music.artistofday.service.CyclicalRotationEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CyclicalRotationEngineTest {

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private CyclicalRotationEngine engine;

    @Test
    @DisplayName("Rotation Formula: Fair cyclical rotation across N artists, restarting at artist 0 on day N+1")
    void shouldRotateDeterministicallyAcrossNDaysAndRestart() {
        int n = 3;
        when(artistRepository.countByActiveTrue()).thenReturn((long) n);

        Artist a1 = new Artist(UUID.randomUUID(), "Artist A");
        Artist a2 = new Artist(UUID.randomUUID(), "Artist B");
        Artist a3 = new Artist(UUID.randomUUID(), "Artist C");

        lenient().when(artistRepository.findActiveOrdered(PageRequest.of(0, 1)))
            .thenReturn(new PageImpl<>(List.of(a1)));
        lenient().when(artistRepository.findActiveOrdered(PageRequest.of(1, 1)))
            .thenReturn(new PageImpl<>(List.of(a2)));
        lenient().when(artistRepository.findActiveOrdered(PageRequest.of(2, 1)))
            .thenReturn(new PageImpl<>(List.of(a3)));

        LocalDate baseDate = LocalDate.of(2026, 6, 1);

        // Day 1
        var day1 = engine.getArtistForDate(baseDate);
        assertThat(day1).isPresent();

        // Day 1 + N (exact full cycle completion)
        LocalDate nextCycleDate = baseDate.plusDays(n);
        var restartDay = engine.getArtistForDate(nextCycleDate);
        assertThat(restartDay).isPresent();

        // Must yield the exact same artist identity on Day 1 and Day N+1
        assertThat(restartDay.get().canonicalName()).isEqualTo(day1.get().canonicalName());
        assertThat(restartDay.get().cycleDayIndex()).isEqualTo(day1.get().cycleDayIndex());
    }

    @Test
    @DisplayName("Corner Case: Returns empty optional when catalog has 0 active artists")
    void shouldReturnEmptyWhenNoActiveArtists() {
        when(artistRepository.countByActiveTrue()).thenReturn(0L);

        var result = engine.getArtistForDate(LocalDate.now());

        assertThat(result).isEmpty();
        verify(artistRepository, never()).findActiveOrdered(any());
    }

    @Test
    @DisplayName("Mathematical Modulo: Handles negative epoch days correctly without IndexOutOfBoundsException")
    void shouldHandlePre1970DatesSafely() {
        when(artistRepository.countByActiveTrue()).thenReturn(5L);
        Artist artist = new Artist(UUID.randomUUID(), "Pre-1970 Legend");
        when(artistRepository.findActiveOrdered(any())).thenReturn(new PageImpl<>(List.of(artist)));

        LocalDate historicalDate = LocalDate.of(1965, 1, 1); // Negative epoch day
        var result = engine.getArtistForDate(historicalDate);

        assertThat(result).isPresent();
        assertThat(result.get().cycleDayIndex()).isBetween(1L, 5L);
    }
}
