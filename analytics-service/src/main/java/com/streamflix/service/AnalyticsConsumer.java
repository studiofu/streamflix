package com.streamflix.analytics.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsConsumer {

    // Listens to the exact topic the Rating Service is publishing to
    @KafkaListener(topics = "movie-ratings-topic", groupId = "analytics-group")
    public void consumeRatingEvent(String message) {
        System.out.println("==========================================");
        System.out.println("📈 ANALYTICS SERVICE RECEIVED EVENT:");
        System.out.println(message);
        System.out.println("Action: Recalculating Trending Movies...");
        System.out.println("==========================================");
    }
}