package com.healthcare.staffing.shared.events.booking;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class BookingBooked extends BookingEvent {
    @NotNull
    private UUID carerId;
    
    @NotNull
    private String bookedBy;

    // Default constructor for Jackson
    public BookingBooked() {
        super();
    }

    public BookingBooked(UUID bookingId, UUID carerId, String bookedBy) {
        super(bookingId, "BookingBooked");
        this.carerId = carerId;
        this.bookedBy = bookedBy;
    }

    public UUID getCarerId() { return carerId; }
    public String getBookedBy() { return bookedBy; }
}
