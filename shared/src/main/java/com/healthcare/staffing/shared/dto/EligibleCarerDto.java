package com.healthcare.staffing.shared.dto;

import java.util.List;
import java.util.UUID;

public class EligibleCarerDto {
    private UUID carerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String location;
    private String grade;
    private List<String> qualifications;
    private String visaStatus;
    private Integer maxTravelDistance;
    private Double distanceKm;
    private boolean available;

    // Constructors
    public EligibleCarerDto() {}

    public EligibleCarerDto(UUID carerId, String firstName, String lastName, String email,
                           String phone, String location, String grade, List<String> qualifications,
                           String visaStatus, Integer maxTravelDistance, Double distanceKm, boolean available) {
        this.carerId = carerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.grade = grade;
        this.qualifications = qualifications;
        this.visaStatus = visaStatus;
        this.maxTravelDistance = maxTravelDistance;
        this.distanceKm = distanceKm;
        this.available = available;
    }

    // Getters and Setters
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

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public List<String> getQualifications() { return qualifications; }
    public void setQualifications(List<String> qualifications) { this.qualifications = qualifications; }

    public String getVisaStatus() { return visaStatus; }
    public void setVisaStatus(String visaStatus) { this.visaStatus = visaStatus; }

    public Integer getMaxTravelDistance() { return maxTravelDistance; }
    public void setMaxTravelDistance(Integer maxTravelDistance) { this.maxTravelDistance = maxTravelDistance; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
