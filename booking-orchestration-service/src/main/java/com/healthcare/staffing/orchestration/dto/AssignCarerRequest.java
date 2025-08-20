package com.healthcare.staffing.orchestration.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class AssignCarerRequest {
    
    @NotNull
    private UUID bookingId;
    
    @NotNull
    private UUID carerId;
    
    @NotNull
    private String bookedBy;
    
    @NotNull
    private LocalDateTime startTime;
    
    @NotNull
    private LocalDateTime endTime;
    
    public AssignCarerRequest() {}
    
    public AssignCarerRequest(UUID bookingId, UUID carerId, String bookedBy, 
                             LocalDateTime startTime, LocalDateTime endTime) {
        this.bookingId = bookingId;
        this.carerId = carerId;
        this.bookedBy = bookedBy;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getCarerId() { return carerId; }
    public void setCarerId(UUID carerId) { this.carerId = carerId; }

    public String getBookedBy() { return bookedBy; }
    public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
