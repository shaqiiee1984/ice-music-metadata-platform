package com.ice.music.artist;

import com.ice.music.artist.dto.ArtistResponse;
import com.ice.music.artist.dto.CreateArtistRequest;
import com.ice.music.artist.dto.UpdateArtistRequest;
import com.ice.music.artist.model.Artist;
import com.ice.music.artist.repository.ArtistRepository;
import com.ice.music.artist.service.ArtistService;
import com.ice.music.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private ArtistService artistService;

    @Test
    @DisplayName("Create Artist: Successfully creates artist and populates initial aliases")
    void shouldCreateArtistWithAliases() {
        CreateArtistRequest request = new CreateArtistRequest("Childish Gambino", List.of("Donald Glover", "mcDJ"));
        when(artistRepository.save(any(Artist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArtistResponse response = artistService.createArtist(request);

        assertThat(response.canonicalName()).isEqualTo("Childish Gambino");
        assertThat(response.aliases()).containsExactlyInAnyOrder("Donald Glover", "mcDJ");
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    @DisplayName("Rename Artist: Renaming preserves the previous canonical name as an alias")
    void shouldPreserveOldCanonicalNameAsAliasOnRename() {
        UUID id = UUID.randomUUID();
        Artist existing = new Artist(id, "Eminem");
        existing.addAlias("Slim Shady");

        when(artistRepository.findById(id)).thenReturn(Optional.of(existing));
        when(artistRepository.save(any(Artist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateArtistRequest updateRequest = new UpdateArtistRequest("Marshall Mathers", null, null);
        ArtistResponse updated = artistService.updateArtist(id, updateRequest);

        assertThat(updated.canonicalName()).isEqualTo("Marshall Mathers");
        // Old name "Eminem" must now be preserved in aliases!
        assertThat(updated.aliases()).contains("Eminem", "Slim Shady");
    }

    @Test
    @DisplayName("Update Artist: Throws ResourceNotFoundException for unknown ID")
    void shouldThrowNotFoundWhenArtistDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(artistRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.updateArtist(unknownId, new UpdateArtistRequest("New Name", null, null)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(unknownId.toString());
    }
}
