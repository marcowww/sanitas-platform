package com.healthcare.staffing.carer.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carers")
public class Carer {
    
    @Id
    private UUID id;
    
    @NotNull
    @Column(name = "first_name")
    private String firstName;
    
    @NotNull
    @Column(name = "last_name")
    private String lastName;
    
    @NotNull
    @Column(name = "email", unique = true)
    private String email;
    
    @NotNull
    @Column(name = "phone")
    private String phone;
    
    @NotNull
    @Column(name = "location")
    private String location;
    
    @NotNull
    @Column(name = "grade")
    private String grade;
    
    @ElementCollection
    @CollectionTable(name = "carer_qualifications", joinColumns = @JoinColumn(name = "carer_id"))
    @Column(name = "qualification")
    private List<String> qualifications;
    
    @NotNull
    @Column(name = "visa_status")
    private String visaStatus;
    
    @Column(name = "max_travel_distance")
    private Integer maxTravelDistance;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CarerStatus status = CarerStatus.ACTIVE;
    
    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Carer() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    public Carer(String firstName, String lastName, String email, String phone, 
                String location, String grade, List<String> qualifications, 
                String visaStatus, Integer maxTravelDistance) {
        this();
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

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public CarerStatus getStatus() { return status; }
    public void setStatus(CarerStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public enum CarerStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
