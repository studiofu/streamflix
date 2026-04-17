package com.streamflix.rating.fetcher;

import com.netflix.graphql.dgs.*;
import com.streamflix.rating.model.Movie;
import com.streamflix.rating.model.Rating;
import com.streamflix.rating.repository.RatingRepository;
import com.streamflix.rating.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@DgsComponent
public class RatingDataFetcher {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RatingService ratingService;

    // 1. Standard Query
    @DgsQuery
    public List<Rating> ratingsByMovie(@InputArgument String movieId) {
        return ratingRepository.findByMovieId(movieId);
    }

    @DgsQuery
    public List<Rating> allRatings() {
        return ratingRepository.findAll();
    }

    // 2. The Mutation (Saves to DB, Publishes to Kafka)
    @DgsMutation
    public Rating addRating(@InputArgument String movieId, 
        //@InputArgument String userId, 
        @InputArgument Integer stars,
        @RequestHeader(name = "x-user-id", required = false) String loggedInUserId        
    ) {

        if(loggedInUserId == null || loggedInUserId.isEmpty()) {
            throw new RuntimeException("User ID is required");
        }

        return ratingService.createRatingAndOutboxEvent(movieId, loggedInUserId, stars);
    }

    // 3. Federation Magic: Resolves the "ratings" field on the extended Movie type
    @DgsData(parentType = "Movie", field = "ratings")
    public List<Rating> ratingsForMovie(DgsDataFetchingEnvironment dfe) {
        // Retrieve the "Movie" object that the gateway passed to us
        // Map<String, Object> movie = dfe.getSource();
        // String movieId = (String) movie.get("id");
        // return ratingRepository.findByMovieId(movieId);

        // Now we cast it directly to the Movie object we created
        Movie movie = dfe.getSource(); 
        return ratingRepository.findByMovieId(movie.getId());

    }

    // Required for Apollo Federation to stitch the extended Movie type
    // @DgsEntityFetcher(name = "Movie")
    // public Map<String, Object> movieEntityFetcher(Map<String, Object> values) {
    //     // The Gateway passes us the @key fields (in this case, just the "id").
    //     // We simply return this map so that the ratingsForMovie() method can read the ID from it.

    //     // Explicitly tell the GraphQL engine that this Map represents the "Movie" type
    //     values.put("__typename", "Movie");        
    //     return values;
    // }    

    @DgsEntityFetcher(name = "Movie")
    public Movie movieEntityFetcher(Map<String, Object> values) {
        String id = (String) values.get("id");
        return new Movie(id);
    }    


      
}
