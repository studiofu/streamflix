package com.streamflix.playback.repository;

import com.streamflix.playback.model.PlaybackProgress;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaybackProgressRepository extends JpaRepository<PlaybackProgress, UUID> {

    Optional<PlaybackProgress> findByUserIdAndMovieId(String userId, String movieId);

    List<PlaybackProgress> findByUserIdOrderByUpdatedAtDesc(String userId, Pageable pageable);
}
