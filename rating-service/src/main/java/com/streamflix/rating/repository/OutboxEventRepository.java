package com.streamflix.rating.repository;

import com.streamflix.rating.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    // Find all events that haven't been sent to Kafka yet
    List<OutboxEvent> findByProcessedFalse();
}
