package com.healthcare.staffing.orchestration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class BlockAvailabilityRequest {
    private UUID bookingId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String blockedBy;

    public BlockAvailabilityRequest() {}

    public BlockAvailabilityRequest(UUID bookingId, LocalDateTime startTime, 
                                   LocalDateTime endTime, String blockedBy) {
        this.bookingId = bookingId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.blockedBy = blockedBy;
    }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getBlockedBy() { return blockedBy; }
    public void setBlockedBy(String blockedBy) { this.blockedBy = blockedBy; }
}
