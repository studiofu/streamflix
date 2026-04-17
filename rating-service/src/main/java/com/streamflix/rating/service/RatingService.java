package com.streamflix.rating.service;

import com.streamflix.rating.model.OutboxEvent;
import com.streamflix.rating.model.Rating;
import com.streamflix.rating.repository.OutboxEventRepository;
import com.streamflix.rating.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Transactional // CRITICAL: Both saves happen, or neither happens.
    public Rating createRatingAndOutboxEvent(String movieId, String userId, Integer stars) {
        // 1. Save the Rating
        Rating newRating = new Rating(movieId, userId, stars);
        Rating savedRating = ratingRepository.save(newRating);

        // 2. Save the Outbox Event
        String message = String.format("User %s rated Movie %s with %d stars", userId, movieId, stars);
        OutboxEvent outboxEvent = new OutboxEvent(savedRating.getId().toString(), message);
        outboxEventRepository.save(outboxEvent);

        return savedRating;
    }
}
