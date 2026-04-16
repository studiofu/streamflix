package com.streamflix.rating.fetcher;

import com.netflix.graphql.dgs.*;
import com.streamflix.rating.model.Rating;
import com.streamflix.rating.repository.RatingRepository;
import com.streamflix.rating.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DgsComponent
public class RatingDataFetcher {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    // 1. Standard Query
    @DgsQuery
    public List<Rating> ratingsByMovie(@InputArgument String movieId) {
        return ratingRepository.findByMovieId(movieId);
    }

    // 2. The Mutation (Saves to DB, Publishes to Kafka)
    @DgsMutation
    public Rating addRating(@InputArgument String movieId, @InputArgument String userId, @InputArgument Integer stars) {
        Rating newRating = new Rating(movieId, userId, stars);
        Rating savedRating = ratingRepository.save(newRating);
        
        System.out.println("Saved rating: " + savedRating);
        
        // Send event to Kafka
        kafkaProducerService.publishRatingEvent(movieId, userId, stars);
        
        return savedRating;
    }

    // 3. Federation Magic: Resolves the "ratings" field on the extended Movie type
    @DgsData(parentType = "Movie", field = "ratings")
    public List<Rating> ratingsForMovie(DgsDataFetchingEnvironment dfe) {
        // Retrieve the "Movie" object that the gateway passed to us
        Map<String, Object> movie = dfe.getSource();
        String movieId = (String) movie.get("id");
        return ratingRepository.findByMovieId(movieId);
    }
}
