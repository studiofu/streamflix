package com.streamflix.analytics.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnalyticsConsumer {

  // In a real production environment, processedEventIds would be a Redis Cache or
  // a Postgres database table so it survives restarts. For this example, we will
  // use a thread-safe in-memory cache.

  // Listens to the exact topic the Rating Service is publishing to
  // @KafkaListener(topics = "movie-ratings-topic", groupId = "analytics-group")
  // public void consumeRatingEvent(String message) {
  // System.out.println("==========================================");
  // System.out.println("📈 ANALYTICS SERVICE RECEIVED EVENT:");
  // System.out.println(message);
  // System.out.println("Action: Recalculating Trending Movies...");
  // System.out.println("==========================================");
  // }

  // Simulating a Redis Cache or Database table for Idempotency
  private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

  @Autowired
  private StringRedisTemplate redisTemplate;

  @KafkaListener(topics = "movie-ratings-topic", groupId = "analytics-group")
  public void consumeRatingEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
    String eventId = record.key();
    String message = record.value();

    try {
      if (eventId != null) {
        // Redis SETNX command: Try to set the key.
        // We also give it a Time-To-Live (TTL) of 7 days so Redis doesn't run out of
        // memory!
        String redisKey = "processed_event:" + eventId;
        Boolean isNewEvent = redisTemplate.opsForValue()
            .setIfAbsent(redisKey, "done", Duration.ofDays(7));

        if (Boolean.FALSE.equals(isNewEvent)) {
          System.out.println("⚠️ Duplicate event detected in Redis! Skipped: " + eventId);
          acknowledgment.acknowledge();
          return;
        }
      }

      // 2. Process the Message
      System.out.println("==========================================");
      System.out.println("📈 ANALYTICS PROCESSING EVENT ID: " + eventId);
      System.out.println(message);
      System.out.println("Action: Recalculating Trending Movies...");
      System.out.println("==========================================");

      // 3. Manual Acknowledgment: We successfully finished processing!
      // If the app crashes BEFORE this line, Kafka will safely redeliver the message.
      acknowledgment.acknowledge();

    } catch (Exception e) {
      System.err.println("❌ Error processing message. It will not be acknowledged.");
      // We do NOT call acknowledge().
      // Depending on your setup, this will either pause the partition or send it to a
      // Dead Letter Queue (DLQ).
    }
  }

}