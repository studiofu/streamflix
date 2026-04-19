package com.streamflix.playback.dto;

public record PlaybackState(
    String movieId,
    int positionSeconds,
    Integer durationSeconds,
    boolean completed,
    String updatedAt
) {
}
