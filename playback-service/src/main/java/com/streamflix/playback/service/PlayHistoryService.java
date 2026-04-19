package com.streamflix.playback.service;

import com.streamflix.playback.dto.PlayHistoryEntry;
import com.streamflix.playback.model.PlayHistory;
import com.streamflix.playback.repository.PlayHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PlayHistoryService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    @Autowired
    private PlayHistoryRepository playHistoryRepository;

    @Transactional
    public PlayHistoryEntry recordPlay(String userId, String movieId) {
        Instant now = Instant.now();
        PlayHistory row = new PlayHistory();
        row.setUserId(userId);
        row.setMovieId(movieId);
        row.setPlayedAt(now);
        playHistoryRepository.save(row);
        return toEntry(row);
    }

    public List<PlayHistoryEntry> listForUser(String userId, Integer limit) {
        int n = DEFAULT_LIMIT;
        if (limit != null && limit > 0) {
            n = Math.min(limit, MAX_LIMIT);
        }
        List<PlayHistory> rows = playHistoryRepository.findByUserIdOrderByPlayedAtDesc(
            userId,
            PageRequest.of(0, n)
        );
        return rows.stream().map(this::toEntry).toList();
    }

    private PlayHistoryEntry toEntry(PlayHistory e) {
        return new PlayHistoryEntry(
            e.getId().toString(),
            e.getMovieId(),
            e.getPlayedAt().toString()
        );
    }
}
