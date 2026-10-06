package com.ice.music.security;

import com.ice.music.artist.service.ArtistService;
import com.ice.music.artistofday.service.CyclicalRotationEngine;
import com.ice.music.catalogue.service.CatalogueService;
import com.ice.music.shared.security.JwtUtil;
import com.ice.music.track.dto.CreateTrackRequest;
import com.ice.music.track.service.TrackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrackService trackService;

    @MockBean
    private ArtistService artistService;

    @MockBean
    private CyclicalRotationEngine rotationEngine;

    @MockBean
    private CatalogueService catalogueService;

    @Test
    @DisplayName("Public Access: /api/v1/featured/today is accessible without authentication")
    void featuredEndpointShouldBePublic() throws Exception {
        when(rotationEngine.getTodayArtist()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/featured/today"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Role-Based Security: POST /api/v1/tracks without auth returns 403 Forbidden")
    void writeEndpointsShouldRejectAnonymousUsers() throws Exception {
        CreateTrackRequest request = new CreateTrackRequest(
            UUID.randomUUID(), "Unauthorized Track", "Pop", 200000, null
        );

        mockMvc.perform(post("/api/v1/tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "editor", roles = {"EDITOR"})
    @DisplayName("Authorized Access: POST /api/v1/tracks succeeds when user possesses ROLE_EDITOR")
    void writeEndpointsShouldPermitEditorRole() throws Exception {
        CreateTrackRequest request = new CreateTrackRequest(
            UUID.randomUUID(), "Authorized Track", "Pop", 200000, null
        );

        mockMvc.perform(post("/api/v1/tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}
