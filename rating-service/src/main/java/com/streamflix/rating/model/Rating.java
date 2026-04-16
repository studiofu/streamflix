package com.streamflix.rating.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String movieId; // References MongoDB ID
    private String userId;  // References Postgres User ID
    private Integer stars;

    public Rating() {}

    public Rating(String movieId, String userId, Integer stars) {
        this.movieId = movieId;
        this.userId = userId;
        this.stars = stars;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
}
