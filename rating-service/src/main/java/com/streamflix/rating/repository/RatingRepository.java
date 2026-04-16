package com.streamflix.rating.repository;

import com.streamflix.rating.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    // Custom method to fetch all ratings for a specific movie
    List<Rating> findByMovieId(String movieId);
}
