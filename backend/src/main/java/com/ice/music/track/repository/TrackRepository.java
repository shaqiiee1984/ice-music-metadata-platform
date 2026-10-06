package com.ice.music.track.repository;

import com.ice.music.track.model.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackRepository extends JpaRepository<Track, UUID> {

    Page<Track> findByArtistId(UUID artistId, Pageable pageable);

    Page<Track> findByArtistIdAndGenreIgnoreCase(UUID artistId, String genre, Pageable pageable);

    List<Track> findByArtistIdOrderByCreatedAtDesc(UUID artistId);

    long countByArtistId(UUID artistId);

    @Query("SELECT DISTINCT t.genre FROM Track t WHERE t.artistId = :artistId ORDER BY t.genre")
    List<String> findDistinctGenresByArtistId(@Param("artistId") UUID artistId);
}
