package com.streamflix.playback.repository;

import com.streamflix.playback.model.PlayHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, UUID> {

    List<PlayHistory> findByUserIdOrderByPlayedAtDesc(String userId, Pageable pageable);
}
