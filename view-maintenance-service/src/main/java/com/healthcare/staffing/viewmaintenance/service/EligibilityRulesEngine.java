package com.healthcare.staffing.viewmaintenance.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EligibilityRulesEngine {
    
    /**
     * Determines if a carer is eligible for a specific booking based on business rules
     */
    public boolean isCarerEligibleForBooking(CarerProjection carer, BookingProjection booking) {
        
        // Rule 1: Grade matching
        if (!carer.getGrade().equals(booking.getGrade())) {
            return false;
        }
        
        // Rule 2: Qualifications - carer must have all required qualifications
        if (booking.getRequiredQualifications() != null && !booking.getRequiredQualifications().isEmpty()) {
            if (carer.getQualifications() == null || 
                !carer.getQualifications().containsAll(booking.getRequiredQualifications())) {
                return false;
            }
        }
        
        // Rule 3: Distance constraint
        double distance = calculateDistance(carer.getLocation(), booking.getLocation());
        if (carer.getMaxTravelDistance() != null && distance > carer.getMaxTravelDistance()) {
            return false;
        }
        
        // Rule 4: Visa status constraints for certain facilities/locations
        if (!isVisaStatusValid(carer.getVisaStatus(), booking.getFacilityId())) {
            return false;
        }
        
        // Rule 5: Availability check (simplified - would need more complex logic in real system)
        if (!isCarerAvailable(carer.getCarerId(), booking.getStartTime(), booking.getEndTime())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculates distance between two locations (simplified implementation)
     * In a real system, this would use actual geolocation services
     */
    private double calculateDistance(String location1, String location2) {
        // Simplified distance calculation
        // In production, you would use actual coordinates and distance calculation
        if (location1.equals(location2)) {
            return 0.0;
        }
        
        // Mock distance calculation based on location names
        return Math.random() * 50; // Random distance between 0-50km for demo
    }
    
    /**
     * Validates visa status against facility requirements
     */
    private boolean isVisaStatusValid(String visaStatus, UUID facilityId) {
        // Simplified visa validation logic
        // In production, this would check facility-specific visa requirements
        
        if ("CITIZEN".equals(visaStatus) || "PERMANENT_RESIDENT".equals(visaStatus)) {
            return true; // Always valid
        }
        
        if ("WORK_VISA".equals(visaStatus)) {
            // Some facilities might not accept work visas
            return !isRestrictedFacility(facilityId);
        }
        
        return false; // Other visa statuses not accepted
    }
    
    /**
     * Checks if a facility has visa restrictions
     */
    private boolean isRestrictedFacility(UUID facilityId) {
        // Mock implementation - in production, this would be a configurable rule
        return facilityId.toString().hashCode() % 10 == 0; // 10% of facilities are restricted
    }
    
    /**
     * Checks if carer is available during the specified time period
     */
    private boolean isCarerAvailable(UUID carerId, java.time.LocalDateTime startTime, 
                                   java.time.LocalDateTime endTime) {
    // Availability check should only fail if carer is already booked for this time slot.
    // TODO: Integrate with actual booking records to check for conflicts.
    return true;
    }
    
    // Supporting classes for projections
    public static class CarerProjection {
        private UUID carerId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String grade;
        private List<String> qualifications;
        private String location;
        private String visaStatus;
        private Integer maxTravelDistance;
        
        // Constructors, getters, and setters
        public CarerProjection() {}
        
        public CarerProjection(UUID carerId, String firstName, String lastName, String email, String phone,
                             String grade, List<String> qualifications, 
                             String location, String visaStatus, Integer maxTravelDistance) {
            this.carerId = carerId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.grade = grade;
            this.qualifications = qualifications;
            this.location = location;
            this.visaStatus = visaStatus;
            this.maxTravelDistance = maxTravelDistance;
        }
        
        public UUID getCarerId() { return carerId; }
        public void setCarerId(UUID carerId) { this.carerId = carerId; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        
        public List<String> getQualifications() { return qualifications; }
        public void setQualifications(List<String> qualifications) { this.qualifications = qualifications; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getVisaStatus() { return visaStatus; }
        public void setVisaStatus(String visaStatus) { this.visaStatus = visaStatus; }
        
        public Integer getMaxTravelDistance() { return maxTravelDistance; }
        public void setMaxTravelDistance(Integer maxTravelDistance) { this.maxTravelDistance = maxTravelDistance; }
    }
    
    public static class BookingProjection {
        private UUID bookingId;
        private UUID facilityId;
        private String shift;
        private String grade;
        private java.math.BigDecimal hourlyRate;
        private List<String> requiredQualifications;
        private String location;
        private String specialRequirements;
        private java.time.LocalDateTime startTime;
        private java.time.LocalDateTime endTime;
        
        // Constructors, getters, and setters
        public BookingProjection() {}
        
        public BookingProjection(UUID bookingId, UUID facilityId, String shift, String grade, 
                               java.math.BigDecimal hourlyRate, List<String> requiredQualifications, 
                               String location, String specialRequirements,
                               java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
            this.bookingId = bookingId;
            this.facilityId = facilityId;
            this.shift = shift;
            this.grade = grade;
            this.hourlyRate = hourlyRate;
            this.requiredQualifications = requiredQualifications;
            this.location = location;
            this.specialRequirements = specialRequirements;
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        public UUID getBookingId() { return bookingId; }
        public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
        
        public UUID getFacilityId() { return facilityId; }
        public void setFacilityId(UUID facilityId) { this.facilityId = facilityId; }
        
        public String getShift() { return shift; }
        public void setShift(String shift) { this.shift = shift; }
        
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        
        public java.math.BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(java.math.BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
        
        public List<String> getRequiredQualifications() { return requiredQualifications; }
        public void setRequiredQualifications(List<String> requiredQualifications) { 
            this.requiredQualifications = requiredQualifications; 
        }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getSpecialRequirements() { return specialRequirements; }
        public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }
        
        public java.time.LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(java.time.LocalDateTime startTime) { this.startTime = startTime; }
        
        public java.time.LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(java.time.LocalDateTime endTime) { this.endTime = endTime; }
    }
}
