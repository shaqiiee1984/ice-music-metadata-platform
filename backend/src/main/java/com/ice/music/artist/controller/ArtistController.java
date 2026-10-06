package com.ice.music.artist.controller;

import com.ice.music.artist.dto.ArtistResponse;
import com.ice.music.artist.dto.CreateArtistRequest;
import com.ice.music.artist.dto.UpdateArtistRequest;
import com.ice.music.artist.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/artists")
@Tag(name = "Artist Commands & Queries", description = "Endpoints for managing artists, canonical names, and aliases")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @PostMapping
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create a new artist with optional initial aliases (Role: EDITOR)")
    public ResponseEntity<ArtistResponse> createArtist(@Valid @RequestBody CreateArtistRequest request) {
        ArtistResponse created = artistService.createArtist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Edit artist canonical name or aliases. Old name is preserved as an alias. (Role: EDITOR)")
    public ResponseEntity<ArtistResponse> updateArtist(
            @PathVariable("id") UUID id,
            @RequestBody UpdateArtistRequest request) {
        ArtistResponse updated = artistService.updateArtist(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get artist details and alias history by ID")
    public ResponseEntity<ArtistResponse> getArtistById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(artistService.getArtistById(id));
    }

    @GetMapping
    @Operation(summary = "List all artists with pagination")
    public ResponseEntity<Page<ArtistResponse>> listArtists(
            @PageableDefault(size = 20, sort = "canonicalName") Pageable pageable) {
        return ResponseEntity.ok(artistService.listArtists(pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "List all active artists sorted by deterministic cycle sequence")
    public ResponseEntity<List<ArtistResponse>> listActiveArtists() {
        return ResponseEntity.ok(artistService.listAllActive());
    }
}
