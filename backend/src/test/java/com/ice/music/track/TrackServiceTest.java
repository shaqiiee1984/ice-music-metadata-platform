package com.ice.music.track;

import com.ice.music.artist.service.ArtistService;
import com.ice.music.shared.exception.ResourceNotFoundException;
import com.ice.music.track.dto.CreateTrackRequest;
import com.ice.music.track.dto.TrackResponse;
import com.ice.music.track.model.Track;
import com.ice.music.track.repository.TrackRepository;
import com.ice.music.track.service.TrackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private ArtistService artistService;

    @InjectMocks
    private TrackService trackService;

    @Test
    @DisplayName("Add Track: Successfully adds a track for an existing artist with formatted duration")
    void shouldCreateTrackSuccessfully() {
        UUID artistId = UUID.randomUUID();
        when(artistService.existsById(artistId)).thenReturn(true);
        when(trackRepository.save(any(Track.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTrackRequest request = new CreateTrackRequest(
            artistId, "Redbone", "Funk", 326000, "USGL11600001"
        );

        TrackResponse response = trackService.createTrack(request);

        assertThat(response.title()).isEqualTo("Redbone");
        assertThat(response.artistId()).isEqualTo(artistId);
        assertThat(response.durationFormatted()).isEqualTo("05:26");
        verify(trackRepository).save(any(Track.class));
    }

    @Test
    @DisplayName("Add Track: Fails when artistId does not exist in the system")
    void shouldRejectTrackWhenArtistNotFound() {
        UUID nonExistentArtistId = UUID.randomUUID();
        when(artistService.existsById(nonExistentArtistId)).thenReturn(false);

        CreateTrackRequest request = new CreateTrackRequest(
            nonExistentArtistId, "Orphan Track", "Rock", 180000, null
        );

        assertThatThrownBy(() -> trackService.createTrack(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Artist not found");

        verify(trackRepository, never()).save(any(Track.class));
    }
}
