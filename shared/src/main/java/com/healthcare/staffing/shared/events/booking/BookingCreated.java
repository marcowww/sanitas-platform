package com.healthcare.staffing.shared.events.booking;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingCreated extends BookingEvent {
    @NotNull
    private UUID facilityId;
    
    @NotNull
    private String shift;
    
    @NotNull
    private LocalDateTime startTime;
    
    @NotNull
    private LocalDateTime endTime;
    
    @NotNull
    private String grade;
    
    @NotNull
    private BigDecimal hourlyRate;
    
    @NotNull
    private String location;
    
    private String specialRequirements;
    
    @NotNull
    private List<String> requiredQualifications;

    // Default constructor for Jackson
    public BookingCreated() {
        super();
    }

    public BookingCreated(UUID bookingId, UUID facilityId, String shift, 
                         LocalDateTime startTime, LocalDateTime endTime, 
                         String grade, BigDecimal hourlyRate, String location,
                         String specialRequirements, List<String> requiredQualifications) {
        super(bookingId, "BookingCreated");
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

    // Getters
    public UUID getFacilityId() { return facilityId; }
    public String getShift() { return shift; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getGrade() { return grade; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public String getLocation() { return location; }
    public String getSpecialRequirements() { return specialRequirements; }
    public List<String> getRequiredQualifications() { return requiredQualifications; }
}
