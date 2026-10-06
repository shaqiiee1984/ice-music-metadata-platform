package com.ice.music.artistofday.controller;

import com.ice.music.artistofday.dto.ArtistOfTheDayResponse;
import com.ice.music.artistofday.service.CyclicalRotationEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/featured")
@Tag(name = "Artist of the Day", description = "Endpoints for fair, deterministic cyclical artist rotation")
public class ArtistOfTheDayController {

    private final CyclicalRotationEngine rotationEngine;

    public ArtistOfTheDayController(CyclicalRotationEngine rotationEngine) {
        this.rotationEngine = rotationEngine;
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's featured Artist of the Day based on UTC calendar rotation")
    public ResponseEntity<ArtistOfTheDayResponse> getTodayArtist() {
        return rotationEngine.getTodayArtist()
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Recompute who was or will be the featured artist for any given UTC date (Verification endpoint)")
    public ResponseEntity<ArtistOfTheDayResponse> getArtistForDate(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return rotationEngine.getArtistForDate(date)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
