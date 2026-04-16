package com.streamflix.rating.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "movie-ratings-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void publishRatingEvent(String movieId, String userId, Integer stars) {
        String message = String.format("User %s rated Movie %s with %d stars", userId, movieId, stars);
        kafkaTemplate.send(TOPIC, movieId, message);
        System.out.println("Published to Kafka: " + message);
    }
}
