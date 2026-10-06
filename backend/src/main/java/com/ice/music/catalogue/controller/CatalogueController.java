package com.ice.music.catalogue.controller;

import com.ice.music.catalogue.dto.ArtistDiscographyResponse;
import com.ice.music.catalogue.service.CatalogueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalogue")
@Tag(name = "Catalogue Queries", description = "Read model endpoints for high-throughput track discovery")
public class CatalogueController {

    private final CatalogueService catalogueService;

    public CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping("/artists/{id}/tracks")
    @Operation(summary = "Fetch all tracks associated with a specific artist (paginated, with optional genre filter)")
    public ResponseEntity<ArtistDiscographyResponse> getArtistTracks(
            @PathVariable("id") UUID id,
            @RequestParam(value = "genre", required = false) String genre,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        ArtistDiscographyResponse response = catalogueService.getArtistTracks(id, genre, pageable);
        return ResponseEntity.ok(response);
    }
}
