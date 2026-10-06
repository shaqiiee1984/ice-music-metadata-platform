package com.ice.music.artist.repository;

import com.ice.music.artist.model.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID> {

    List<Artist> findByActiveTrueOrderByCreatedAtAscIdAsc();

    long countByActiveTrue();

    Page<Artist> findByActiveTrue(Pageable pageable);

    @Query("SELECT a FROM Artist a WHERE a.active = true ORDER BY a.createdAt ASC, a.id ASC")
    Page<Artist> findActiveOrdered(Pageable pageable);

    Optional<Artist> findByIdAndActiveTrue(UUID id);
}
