package com.healthcare.staffing.orchestration.dto;

import java.util.UUID;

public class BookCarerRequest {
    private UUID carerId;
    private String bookedBy;

    public BookCarerRequest() {}

    public BookCarerRequest(UUID carerId, String bookedBy) {
        this.carerId = carerId;
        this.bookedBy = bookedBy;
    }

    public UUID getCarerId() { return carerId; }
    public void setCarerId(UUID carerId) { this.carerId = carerId; }

    public String getBookedBy() { return bookedBy; }
    public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }
}
