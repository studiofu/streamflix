package com.streamflix.catalog.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.streamflix.catalog.model.Movie;
import com.streamflix.catalog.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

// This class provides DGS data fetchers to expose Movie queries via GraphQL.
// It maps queries defined in the GraphQL schema to method handlers,
// allowing for retrieval of movie data from the MongoDB-backed repository.

// test graphql at
// http://localhost:8081/graphiql

@DgsComponent
public class MovieDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(MovieDataFetcher.class);

    @Autowired
    private MovieRepository movieRepository;

    // Maps to "movies: [Movie]" in schema.graphqls
    @DgsQuery
    @Cacheable(value = "moviesCatalog")
    public List<Movie> movies() {
        log.info("Fetching movies from MongoDB");
        return movieRepository.findAll();
    }

    // Maps to "movie(id: ID!): Movie" in schema.graphqls
    @DgsQuery
    public Movie movie(@InputArgument String id) {
        return movieRepository.findById(id).orElse(null);
    }

    @DgsMutation
    @CacheEvict(value = "moviesCatalog", allEntries = true)
    public Movie addMovie(
            @InputArgument String title,
            @InputArgument String description,
            @InputArgument Integer releaseYear) {
        Movie movie = new Movie(title, description, releaseYear);
        return movieRepository.save(movie);
    }

    // Required for Apollo Federation (@key directive)
    @DgsEntityFetcher(name = "Movie")
    public Movie movieEntityFetcher(Map<String, Object> values) {
        String id = (String) values.get("id");
        return movieRepository.findById(id).orElse(null);
    }
}