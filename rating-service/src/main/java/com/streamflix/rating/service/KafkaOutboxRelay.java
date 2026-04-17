package com.streamflix.rating.service;

import com.streamflix.rating.model.OutboxEvent;
import com.streamflix.rating.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaOutboxRelay {

    private static final String TOPIC = "movie-ratings-topic";

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // Runs every 5 seconds
    @Scheduled(fixedDelay = 5000)
    public void relayEventsToKafka() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByProcessedFalse();

        for (OutboxEvent event : pendingEvents) {
            try {
                // Send to Kafka
                kafkaTemplate.send(TOPIC, event.getAggregateId(), event.getPayload()).get(); 
                
                // If successful, mark as processed and save
                event.setProcessed(true);
                outboxEventRepository.save(event);
                
                System.out.println("Outbox Relay: Successfully published event " + event.getId());
            } catch (Exception e) {
                System.err.println("Outbox Relay: Failed to publish event " + event.getId() + ". Will retry next cycle.");
                // We don't mark it as processed, so the scheduler will try again in 5 seconds!
            }
        }
    }
}

