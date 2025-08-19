package com.healthcare.staffing.shared.events.carer;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class NewCarer extends CarerEvent {
    @NotNull
    private String firstName;
    
    @NotNull
    private String lastName;
    
    @NotNull
    private String email;
    
    @NotNull
    private String phone;
    
    @NotNull
    private String location;
    
    @NotNull
    private String grade;
    
    @NotNull
    private List<String> qualifications;
    
    @NotNull
    private String visaStatus;
    
    private Integer maxTravelDistance;

    // Default constructor for Jackson
    public NewCarer() {
        super();
    }

    public NewCarer(UUID carerId, String firstName, String lastName, String email, 
                   String phone, String location, String grade, List<String> qualifications,
                   String visaStatus, Integer maxTravelDistance) {
        super(carerId, "NewCarer");
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.grade = grade;
        this.qualifications = qualifications;
        this.visaStatus = visaStatus;
        this.maxTravelDistance = maxTravelDistance;
    }

    // Getters
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
    public String getGrade() { return grade; }
    public List<String> getQualifications() { return qualifications; }
    public String getVisaStatus() { return visaStatus; }
    public Integer getMaxTravelDistance() { return maxTravelDistance; }
}
