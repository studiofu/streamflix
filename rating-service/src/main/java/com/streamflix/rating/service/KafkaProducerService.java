package com.streamflix.rating.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "movie-ratings-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // Update the method signature to accept the Rating ID
    public void publishRatingEvent(String ratingId, String movieId, String userId, Integer stars) {
        String message = String.format("User %s rated Movie %s with %d stars", userId, movieId, stars);
        
        // send(topic, key, value)
        kafkaTemplate.send(TOPIC, ratingId, message); 
        System.out.println("Published to Kafka: " + message);
    }
}
