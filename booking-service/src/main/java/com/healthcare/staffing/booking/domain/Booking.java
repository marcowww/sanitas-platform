package com.healthcare.staffing.booking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    private UUID id;
    
    @NotNull
    @Column(name = "facility_id")
    private UUID facilityId;
    
    @NotNull
    @Column(name = "shift_type")
    private String shift;
    
    @NotNull
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @NotNull
    @Column(name = "end_time") 
    private LocalDateTime endTime;
    
    @NotNull
    @Column(name = "grade")
    private String grade;
    
    @NotNull
    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;
    
    @NotNull
    @Column(name = "location")
    private String location;
    
    @Column(name = "special_requirements")
    private String specialRequirements;
    
    @ElementCollection
    @CollectionTable(name = "booking_qualifications", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "qualification")
    private List<String> requiredQualifications;
    
    @Column(name = "assigned_carer_id")
    private UUID assignedCarerId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status = BookingStatus.OPEN;
    
    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Booking() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    public Booking(UUID facilityId, String shift, LocalDateTime startTime, LocalDateTime endTime,
                  String grade, BigDecimal hourlyRate, String location, String specialRequirements,
                  List<String> requiredQualifications) {
        this();
        this.facilityId = facilityId;
        this.shift = shift;
        this.startTime = startTime;
        this.endTime = endTime;
        this.grade = grade;
        this.hourlyRate = hourlyRate;
        this.location = location;
        this.specialRequirements = specialRequirements;
        this.requiredQualifications = requiredQualifications;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getFacilityId() { return facilityId; }
    public void setFacilityId(UUID facilityId) { this.facilityId = facilityId; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }

    public List<String> getRequiredQualifications() { return requiredQualifications; }
    public void setRequiredQualifications(List<String> requiredQualifications) { this.requiredQualifications = requiredQualifications; }

    public UUID getAssignedCarerId() { return assignedCarerId; }
    public void setAssignedCarerId(UUID assignedCarerId) { this.assignedCarerId = assignedCarerId; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public enum BookingStatus {
        OPEN, BOOKED, CANCELLED, COMPLETED
    }
}
