package com.healthcare.staffing.shared.events.carer;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class CarerEvent {
    @NotNull
    private UUID carerId;
    
    @NotNull
    private String eventType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @NotNull
    private String eventId;

    // Default constructor for Jackson
    protected CarerEvent() {
    }

    protected CarerEvent(UUID carerId, String eventType) {
        this.carerId = carerId;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.eventId = UUID.randomUUID().toString();
    }

    public UUID getCarerId() { return carerId; }
    public String getEventType() { return eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEventId() { return eventId; }
}
