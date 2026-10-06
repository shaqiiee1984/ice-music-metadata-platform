-- V2__seed_data.sql: Seed Initial Catalog for Immediate Verification

-- Artist 1: Childish Gambino (Donald Glover)
INSERT INTO artists (id, canonical_name, active, created_at, updated_at, version)
VALUES ('11111111-1111-1111-1111-111111111111', 'Childish Gambino', TRUE, '2026-01-01 10:00:00+00', '2026-01-01 10:00:00+00', 0);

INSERT INTO artist_aliases (id, artist_id, alias_name, created_at)
VALUES ('11111111-1111-1111-1111-111111111112', '11111111-1111-1111-1111-111111111111', 'Donald Glover', '2026-01-01 10:00:00+00'),
       ('11111111-1111-1111-1111-111111111113', '11111111-1111-1111-1111-111111111111', 'mcDJ', '2026-01-01 10:00:00+00');

INSERT INTO tracks (id, artist_id, title, genre, duration_ms, isrc, created_at, updated_at, version)
VALUES ('11111111-1111-1111-1111-222222222221', '11111111-1111-1111-1111-111111111111', 'Redbone', 'Funk / R&B', 326000, 'USGL11600001', '2026-01-01 10:05:00+00', '2026-01-01 10:05:00+00', 0),
       ('11111111-1111-1111-1111-222222222222', '11111111-1111-1111-1111-111111111111', 'This Is America', 'Hip-Hop', 225000, 'USGL11800002', '2026-01-01 10:06:00+00', '2026-01-01 10:06:00+00', 0),
       ('11111111-1111-1111-1111-222222222223', '11111111-1111-1111-1111-111111111111', 'Sober', 'Neo-Soul', 252000, 'USGL11400003', '2026-01-01 10:07:00+00', '2026-01-01 10:07:00+00', 0);

-- Artist 2: Daft Punk (The Robots)
INSERT INTO artists (id, canonical_name, active, created_at, updated_at, version)
VALUES ('22222222-2222-2222-2222-222222222222', 'Daft Punk', TRUE, '2026-01-02 10:00:00+00', '2026-01-02 10:00:00+00', 0);

INSERT INTO artist_aliases (id, artist_id, alias_name, created_at)
VALUES ('22222222-2222-2222-2222-222222222223', '22222222-2222-2222-2222-222222222222', 'The Robots', '2026-01-02 10:00:00+00');

INSERT INTO tracks (id, artist_id, title, genre, duration_ms, isrc, created_at, updated_at, version)
VALUES ('22222222-2222-2222-2222-333333333331', '22222222-2222-2222-2222-222222222222', 'Get Lucky', 'Disco / Funk', 248000, 'FRUM71300001', '2026-01-02 10:05:00+00', '2026-01-02 10:05:00+00', 0),
       ('22222222-2222-2222-2222-333333333332', '22222222-2222-2222-2222-222222222222', 'One More Time', 'House', 320000, 'FRUM70000002', '2026-01-02 10:06:00+00', '2026-01-02 10:06:00+00', 0);

-- Artist 3: Eminem (Marshall Mathers)
INSERT INTO artists (id, canonical_name, active, created_at, updated_at, version)
VALUES ('33333333-3333-3333-3333-333333333333', 'Eminem', TRUE, '2026-01-03 10:00:00+00', '2026-01-03 10:00:00+00', 0);

INSERT INTO artist_aliases (id, artist_id, alias_name, created_at)
VALUES ('33333333-3333-3333-3333-333333333334', '33333333-3333-3333-3333-333333333333', 'Marshall Mathers', '2026-01-03 10:00:00+00'),
       ('33333333-3333-3333-3333-333333333335', '33333333-3333-3333-3333-333333333333', 'Slim Shady', '2026-01-03 10:00:00+00');

INSERT INTO tracks (id, artist_id, title, genre, duration_ms, isrc, created_at, updated_at, version)
VALUES ('33333333-3333-3333-3333-444444444441', '33333333-3333-3333-3333-333333333333', 'Lose Yourself', 'Hip-Hop', 326000, 'USIR10200001', '2026-01-03 10:05:00+00', '2026-01-03 10:05:00+00', 0);

-- Artist 4: David Bowie (Ziggy Stardust)
INSERT INTO artists (id, canonical_name, active, created_at, updated_at, version)
VALUES ('44444444-4444-4444-4444-444444444444', 'David Bowie', TRUE, '2026-01-04 10:00:00+00', '2026-01-04 10:00:00+00', 0);

INSERT INTO artist_aliases (id, artist_id, alias_name, created_at)
VALUES ('44444444-4444-4444-4444-444444444445', '44444444-4444-4444-4444-444444444444', 'Ziggy Stardust', '2026-01-04 10:00:00+00'),
       ('44444444-4444-4444-4444-444444444446', '44444444-4444-4444-4444-444444444444', 'The Thin White Duke', '2026-01-04 10:00:00+00');

INSERT INTO tracks (id, artist_id, title, genre, duration_ms, isrc, created_at, updated_at, version)
VALUES ('44444444-4444-4444-4444-555555555551', '44444444-4444-4444-4444-444444444444', 'Heroes', 'Art Rock', 367000, 'GBAYE7700001', '2026-01-04 10:05:00+00', '2026-01-04 10:05:00+00', 0),
       ('44444444-4444-4444-4444-555555555552', '44444444-4444-4444-4444-444444444444', 'Space Oddity', 'Psychedelic Folk', 315000, 'GBAYE6900002', '2026-01-04 10:06:00+00', '2026-01-04 10:06:00+00', 0);

-- Artist 5: Lady Gaga (Stefani Germanotta)
INSERT INTO artists (id, canonical_name, active, created_at, updated_at, version)
VALUES ('55555555-5555-5555-5555-555555555555', 'Lady Gaga', TRUE, '2026-01-05 10:00:00+00', '2026-01-05 10:00:00+00', 0);

INSERT INTO artist_aliases (id, artist_id, alias_name, created_at)
VALUES ('55555555-5555-5555-5555-555555555556', '55555555-5555-5555-5555-555555555555', 'Stefani Germanotta', '2026-01-05 10:00:00+00');

INSERT INTO tracks (id, artist_id, title, genre, duration_ms, isrc, created_at, updated_at, version)
VALUES ('55555555-5555-5555-5555-666666666661', '55555555-5555-5555-5555-555555555555', 'Bad Romance', 'Electropop', 294000, 'USUM70900001', '2026-01-05 10:05:00+00', '2026-01-05 10:05:00+00', 0);
