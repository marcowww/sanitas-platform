package com.healthcare.staffing.carer.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "carer_availability")
public class CarerAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotNull
    @Column(name = "carer_id")
    private UUID carerId;
    
    @NotNull
    @Column(name = "date")
    private LocalDate date;
    
    @NotNull
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @NotNull
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @NotNull
    @Column(name = "available")
    private boolean available;

    // Constructors
    public CarerAvailability() {}

    public CarerAvailability(UUID carerId, LocalDate date, LocalTime startTime, 
                           LocalTime endTime, boolean available) {
        this.carerId = carerId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCarerId() { return carerId; }
    public void setCarerId(UUID carerId) { this.carerId = carerId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
