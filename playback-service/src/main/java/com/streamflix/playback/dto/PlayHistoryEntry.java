package com.streamflix.playback.dto;

public record PlayHistoryEntry(
    String id,
    String movieId,
    String playedAt
) {
}
