package com.healthcare.staffing.shared.events.booking;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class BookingCancelled extends BookingEvent {
    @NotNull
    private String cancellationReason;
    
    @NotNull
    private String cancelledBy;

    // Default constructor for Jackson
    public BookingCancelled() {
        super();
    }

    public BookingCancelled(UUID bookingId, String cancellationReason, String cancelledBy) {
        super(bookingId, "BookingCancelled");
        this.cancellationReason = cancellationReason;
        this.cancelledBy = cancelledBy;
    }

    public String getCancellationReason() { return cancellationReason; }
    public String getCancelledBy() { return cancelledBy; }
}
