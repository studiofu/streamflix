package com.streamflix.analytics.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger log = LoggerFactory.getLogger(AnalyticsConsumer.class);

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
          log.warn("Duplicate event detected in Redis, skipped: {}", eventId);
          acknowledgment.acknowledge();
          return;
        }
      }

      log.info("Analytics processing event id={} payload={}", eventId, message);

      // 3. Manual Acknowledgment: We successfully finished processing!
      // If the app crashes BEFORE this line, Kafka will safely redeliver the message.
      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Error processing Kafka message, not acknowledged", e);
      // We do NOT call acknowledge().
      // Depending on your setup, this will either pause the partition or send it to a
      // Dead Letter Queue (DLQ).
    }
  }

}