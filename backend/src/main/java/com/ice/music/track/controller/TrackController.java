package com.ice.music.track.controller;

import com.ice.music.track.dto.CreateTrackRequest;
import com.ice.music.track.dto.TrackResponse;
import com.ice.music.track.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracks")
@Tag(name = "Track Commands & Queries", description = "Endpoints for creating and retrieving music tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @PostMapping
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Add a new track to an artist's catalogue (Role: EDITOR)")
    public ResponseEntity<TrackResponse> createTrack(@Valid @RequestBody CreateTrackRequest request) {
        TrackResponse response = trackService.createTrack(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get track details by ID")
    public ResponseEntity<TrackResponse> getTrackById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(trackService.getTrackById(id));
    }
}
