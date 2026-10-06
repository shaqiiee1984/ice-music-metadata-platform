-- V1__init_schema.sql: Initial Schema Definition with Auditing and Indexes

CREATE TABLE artists (
    id UUID PRIMARY KEY,
    canonical_name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE artist_aliases (
    id UUID PRIMARY KEY,
    artist_id UUID NOT NULL,
    alias_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_artist_alias FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE,
    CONSTRAINT uq_artist_alias UNIQUE (artist_id, alias_name)
);

CREATE TABLE tracks (
    id UUID PRIMARY KEY,
    artist_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    duration_ms BIGINT NOT NULL,
    isrc VARCHAR(12),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_track_artist FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE RESTRICT
);

-- Indexes optimized for query performance and deterministic cycle ordering
CREATE INDEX idx_artists_cycle ON artists(active, created_at ASC, id ASC);
CREATE INDEX idx_tracks_artist_id ON tracks(artist_id);
CREATE INDEX idx_tracks_genre ON tracks(genre);
