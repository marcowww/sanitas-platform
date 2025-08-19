package com.healthcare.staffing.carer.controller;

import com.healthcare.staffing.carer.domain.Carer;
import com.healthcare.staffing.carer.domain.CarerAvailability;
import com.healthcare.staffing.carer.service.CarerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/carers")
public class CarerController {
    
    private final CarerService carerService;

    @Autowired
    public CarerController(CarerService carerService) {
        this.carerService = carerService;
    }

    @PostMapping
    public ResponseEntity<Carer> createCarer(@Valid @RequestBody CreateCarerRequest request) {
        Carer carer = carerService.createCarer(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPhone(),
            request.getLocation(),
            request.getGrade(),
            request.getQualifications(),
            request.getVisaStatus(),
            request.getMaxTravelDistance()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(carer);
    }

    @PutMapping("/{carerId}")
    public ResponseEntity<Carer> updateCarer(@PathVariable UUID carerId,
                                           @Valid @RequestBody UpdateCarerRequest request) {
        Carer carer = carerService.updateCarer(carerId, request.getUpdates(), request.getUpdateReason());
        return ResponseEntity.ok(carer);
    }

    @PostMapping("/{carerId}/availability")
    public ResponseEntity<Void> updateAvailability(@PathVariable UUID carerId,
                                                  @Valid @RequestBody UpdateAvailabilityRequest request) {
        carerService.updateAvailability(carerId, request.getAvailabilitySlots());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{carerId}")
    public ResponseEntity<Carer> getCarer(@PathVariable UUID carerId) {
        Carer carer = carerService.getCarer(carerId);
        return ResponseEntity.ok(carer);
    }

    @GetMapping
    public ResponseEntity<List<Carer>> getAllCarers() {
        List<Carer> carers = carerService.getAllCarers();
        return ResponseEntity.ok(carers);
    }

    @GetMapping("/{carerId}/availability")
    public ResponseEntity<List<CarerAvailability>> getCarerAvailability(
            @PathVariable UUID carerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        List<CarerAvailability> availability;
        if (fromDate != null && toDate != null) {
            availability = carerService.getCarerAvailability(carerId, fromDate, toDate);
        } else {
            availability = carerService.getCarerAvailability(carerId);
        }
        
        return ResponseEntity.ok(availability);
    }

    // Request DTOs
    public static class CreateCarerRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String location;
        private String grade;
        private List<String> qualifications;
        private String visaStatus;
        private Integer maxTravelDistance;

        // Getters and Setters
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
    }

    public static class UpdateCarerRequest {
        private Map<String, Object> updates;
        private String updateReason;

        public Map<String, Object> getUpdates() { return updates; }
        public void setUpdates(Map<String, Object> updates) { this.updates = updates; }

        public String getUpdateReason() { return updateReason; }
        public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
    }

    public static class UpdateAvailabilityRequest {
        private List<CarerService.AvailabilitySlotDto> availabilitySlots;

        public List<CarerService.AvailabilitySlotDto> getAvailabilitySlots() { return availabilitySlots; }
        public void setAvailabilitySlots(List<CarerService.AvailabilitySlotDto> availabilitySlots) { 
            this.availabilitySlots = availabilitySlots; 
        }
    }
}
