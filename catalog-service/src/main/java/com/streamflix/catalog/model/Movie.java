package com.streamflix.catalog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Catalog movie document stored in MongoDB ({@code movies} collection).
 */
@Document(collection = "movies")
public class Movie {
    
    @Id
    private String id;
    private String title;
    private String description;
    private Integer releaseYear;

    // Constructors
    public Movie() {}

    public Movie(String title, String description, Integer releaseYear) {
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
    }

    // Standard Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
}