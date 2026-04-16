package com.streamflix.catalog.repository;

import com.streamflix.catalog.model.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

// This repository manages CRUD operations for the Movie entity using MongoDB.
// Custom query methods can be added here if needed in the future.

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
}