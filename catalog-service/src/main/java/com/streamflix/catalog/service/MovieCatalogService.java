package com.streamflix.catalog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.stereotype.Service;

import com.streamflix.catalog.model.Movie;
import com.streamflix.catalog.repository.MovieRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MovieCatalogService {

    private static final Logger log = LoggerFactory.getLogger(MovieCatalogService.class);

    private static final String MONGO_CATALOG_BREAKER = "mongoCatalog";

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    public List<Movie> findAllMovies() {
        log.info("Fetching movies from MongoDB");
        return circuitBreakerFactory.create(MONGO_CATALOG_BREAKER)
                .run(movieRepository::findAll, ex -> {
                    log.warn("Mongo read failed for findAll; returning empty list", ex);
                    return List.of();
                });
    }

    public Optional<Movie> findMovieById(String id) {
        return circuitBreakerFactory.create(MONGO_CATALOG_BREAKER)
                .run(() -> movieRepository.findById(id), ex -> {
                    log.warn("Mongo read failed for findById id={}; returning empty", id, ex);
                    return Optional.empty();
                });
    }

    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }
}
