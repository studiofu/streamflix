package com.streamflix.playback.service;

import com.streamflix.playback.dto.PlaybackState;
import com.streamflix.playback.model.PlaybackProgress;
import com.streamflix.playback.repository.PlaybackProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PlaybackProgressService {

    private static final int CONTINUE_WATCHING_LIMIT = 20;

    @Autowired
    private PlaybackProgressRepository playbackProgressRepository;

    @Transactional
    public PlaybackState upsert(String userId, String movieId, int positionSeconds, Integer durationSeconds) {
        Instant now = Instant.now();
        boolean completed = computeCompleted(positionSeconds, durationSeconds);

        Optional<PlaybackProgress> existing = playbackProgressRepository.findByUserIdAndMovieId(userId, movieId);
        PlaybackProgress row;
        if (existing.isPresent()) {
            row = existing.get();
            row.setPositionSeconds(positionSeconds);
            if (durationSeconds != null) {
                row.setDurationSeconds(durationSeconds);
            }
            row.setCompleted(completed);
            row.setUpdatedAt(now);
        } else {
            row = new PlaybackProgress();
            row.setUserId(userId);
            row.setMovieId(movieId);
            row.setPositionSeconds(positionSeconds);
            row.setDurationSeconds(durationSeconds);
            row.setCompleted(completed);
            row.setUpdatedAt(now);
        }
        playbackProgressRepository.save(row);
        return toPlaybackState(row);
    }

    public Optional<PlaybackState> findForUserAndMovie(String userId, String movieId) {
        return playbackProgressRepository.findByUserIdAndMovieId(userId, movieId).map(this::toPlaybackState);
    }

    public List<PlaybackState> findContinueWatchingForUser(String userId) {
        List<PlaybackProgress> rows = playbackProgressRepository.findByUserIdOrderByUpdatedAtDesc(
            userId,
            PageRequest.of(0, CONTINUE_WATCHING_LIMIT)
        );
        return rows.stream().map(this::toPlaybackState).toList();
    }

    private static boolean computeCompleted(int positionSeconds, Integer durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return false;
        }
        return positionSeconds * 100 >= durationSeconds * 95;
    }

    private PlaybackState toPlaybackState(PlaybackProgress e) {
        return new PlaybackState(
            e.getMovieId(),
            e.getPositionSeconds(),
            e.getDurationSeconds(),
            e.isCompleted(),
            e.getUpdatedAt().toString()
        );
    }
}
