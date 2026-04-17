package com.streamflix.rating.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String aggregateId; // The Rating ID
    private String payload;     // The actual message
    private boolean processed = false;

    public OutboxEvent() {}

    public OutboxEvent(String aggregateId, String payload) {
        this.aggregateId = aggregateId;
        this.payload = payload;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public String getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
}

