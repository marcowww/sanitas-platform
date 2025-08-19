package com.healthcare.staffing.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EligibleShiftDto {
    private UUID bookingId;
    private UUID facilityId;
    private String facilityName;
    private String shift;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String grade;
    private BigDecimal hourlyRate;
    private String location;
    private String specialRequirements;
    private List<String> requiredQualifications;
    private String status;
    private Double distanceKm;

    // Constructors
    public EligibleShiftDto() {}

    public EligibleShiftDto(UUID bookingId, UUID facilityId, String facilityName, String shift,
                           LocalDateTime startTime, LocalDateTime endTime, String grade,
                           BigDecimal hourlyRate, String location, String specialRequirements,
                           List<String> requiredQualifications, String status, Double distanceKm) {
        this.bookingId = bookingId;
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.shift = shift;
        this.startTime = startTime;
        this.endTime = endTime;
        this.grade = grade;
        this.hourlyRate = hourlyRate;
        this.location = location;
        this.specialRequirements = specialRequirements;
        this.requiredQualifications = requiredQualifications;
        this.status = status;
        this.distanceKm = distanceKm;
    }

    // Getters and Setters
    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getFacilityId() { return facilityId; }
    public void setFacilityId(UUID facilityId) { this.facilityId = facilityId; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
}
