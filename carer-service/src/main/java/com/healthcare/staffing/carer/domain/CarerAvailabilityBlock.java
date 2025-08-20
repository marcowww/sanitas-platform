package com.healthcare.staffing.carer.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "carer_availability_blocks")
public class CarerAvailabilityBlock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotNull
    @Column(name = "carer_id")
    private UUID carerId;
    
    @NotNull
    @Column(name = "booking_id")
    private UUID bookingId;
    
    @NotNull
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @NotNull
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @NotNull
    @Column(name = "blocked_by")
    private String blockedBy;
    
    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public CarerAvailabilityBlock() {}

    public CarerAvailabilityBlock(UUID carerId, UUID bookingId, LocalDateTime startTime, 
                                 LocalDateTime endTime, String blockedBy) {
        this.carerId = carerId;
        this.bookingId = bookingId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.blockedBy = blockedBy;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCarerId() { return carerId; }
    public void setCarerId(UUID carerId) { this.carerId = carerId; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getBlockedBy() { return blockedBy; }
    public void setBlockedBy(String blockedBy) { this.blockedBy = blockedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
