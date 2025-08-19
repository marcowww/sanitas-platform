package com.healthcare.staffing.shared.events.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BookingEvent {
    @NotNull
    private UUID bookingId;
    
    @NotNull
    private String eventType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @NotNull
    private String eventId;

    // Default constructor for Jackson
    protected BookingEvent() {
    }

    protected BookingEvent(UUID bookingId, String eventType) {
        this.bookingId = bookingId;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.eventId = UUID.randomUUID().toString();
    }

    public UUID getBookingId() { return bookingId; }
    public String getEventType() { return eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEventId() { return eventId; }
}
