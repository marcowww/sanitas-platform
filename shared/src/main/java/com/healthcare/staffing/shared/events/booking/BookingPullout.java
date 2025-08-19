package com.healthcare.staffing.shared.events.booking;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class BookingPullout extends BookingEvent {
    @NotNull
    private UUID carerId;
    
    @NotNull
    private String pulloutReason;
    
    @NotNull
    private String pulloutBy;

    // Default constructor for Jackson
    public BookingPullout() {
        super();
    }

    public BookingPullout(UUID bookingId, UUID carerId, String pulloutReason, String pulloutBy) {
        super(bookingId, "BookingPullout");
        this.carerId = carerId;
        this.pulloutReason = pulloutReason;
        this.pulloutBy = pulloutBy;
    }

    public UUID getCarerId() { return carerId; }
    public String getPulloutReason() { return pulloutReason; }
    public String getPulloutBy() { return pulloutBy; }
    
    // Setters for Jackson
    public void setCarerId(UUID carerId) { this.carerId = carerId; }
    public void setPulloutReason(String pulloutReason) { this.pulloutReason = pulloutReason; }
    public void setPulloutBy(String pulloutBy) { this.pulloutBy = pulloutBy; }
}
