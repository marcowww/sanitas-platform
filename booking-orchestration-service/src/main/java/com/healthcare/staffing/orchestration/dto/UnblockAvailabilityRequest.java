package com.healthcare.staffing.orchestration.dto;

import java.util.UUID;

public class UnblockAvailabilityRequest {
    private UUID bookingId;
    private String unblockedBy;

    public UnblockAvailabilityRequest() {}

    public UnblockAvailabilityRequest(UUID bookingId, String unblockedBy) {
        this.bookingId = bookingId;
        this.unblockedBy = unblockedBy;
    }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public String getUnblockedBy() { return unblockedBy; }
    public void setUnblockedBy(String unblockedBy) { this.unblockedBy = unblockedBy; }
}
