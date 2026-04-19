package com.streamflix.playback.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "playback_progress",
    uniqueConstraints = @UniqueConstraint(name = "uk_playback_user_movie", columnNames = { "user_id", "movie_id" })
)
public class PlaybackProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "movie_id", nullable = false)
    private String movieId;

    @Column(name = "position_seconds", nullable = false)
    private int positionSeconds;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PlaybackProgress() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public int getPositionSeconds() {
        return positionSeconds;
    }

    public void setPositionSeconds(int positionSeconds) {
        this.positionSeconds = positionSeconds;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
