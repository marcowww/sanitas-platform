package com.healthcare.staffing.carer.service;

import com.healthcare.staffing.carer.domain.Carer;
import com.healthcare.staffing.carer.domain.CarerAvailability;
import com.healthcare.staffing.carer.repository.CarerAvailabilityRepository;
import com.healthcare.staffing.carer.repository.CarerRepository;
import com.healthcare.staffing.shared.events.carer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarerService {
    
    private final CarerRepository carerRepository;
    private final CarerAvailabilityRepository availabilityRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String CARER_EVENTS_TOPIC = "carer-events";

    @Autowired
    public CarerService(CarerRepository carerRepository, 
                       CarerAvailabilityRepository availabilityRepository,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.carerRepository = carerRepository;
        this.availabilityRepository = availabilityRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Carer createCarer(String firstName, String lastName, String email, String phone,
                           String location, String grade, List<String> qualifications,
                           String visaStatus, Integer maxTravelDistance) {
        
        Carer carer = new Carer(firstName, lastName, email, phone, location, 
                               grade, qualifications, visaStatus, maxTravelDistance);
        
        Carer savedCarer = carerRepository.save(carer);
        
        // Emit NewCarer event
        NewCarer event = new NewCarer(
            savedCarer.getId(), firstName, lastName, email, phone, location,
            grade, qualifications, visaStatus, maxTravelDistance
        );
        
        kafkaTemplate.send(CARER_EVENTS_TOPIC, event.getCarerId().toString(), event);
        
        return savedCarer;
    }

    public Carer updateCarer(UUID carerId, Map<String, Object> updates, String updateReason) {
        Carer carer = carerRepository.findById(carerId)
            .orElseThrow(() -> new RuntimeException("Carer not found: " + carerId));
        
        Map<String, Object> changedFields = new HashMap<>();
        
        // Apply updates
        if (updates.containsKey("firstName")) {
            String newFirstName = (String) updates.get("firstName");
            if (!newFirstName.equals(carer.getFirstName())) {
                changedFields.put("firstName", Map.of("old", carer.getFirstName(), "new", newFirstName));
                carer.setFirstName(newFirstName);
            }
        }
        
        if (updates.containsKey("lastName")) {
            String newLastName = (String) updates.get("lastName");
            if (!newLastName.equals(carer.getLastName())) {
                changedFields.put("lastName", Map.of("old", carer.getLastName(), "new", newLastName));
                carer.setLastName(newLastName);
            }
        }
        
        if (updates.containsKey("email")) {
            String newEmail = (String) updates.get("email");
            if (!newEmail.equals(carer.getEmail())) {
                changedFields.put("email", Map.of("old", carer.getEmail(), "new", newEmail));
                carer.setEmail(newEmail);
            }
        }
        
        if (updates.containsKey("phone")) {
            String newPhone = (String) updates.get("phone");
            if (!newPhone.equals(carer.getPhone())) {
                changedFields.put("phone", Map.of("old", carer.getPhone(), "new", newPhone));
                carer.setPhone(newPhone);
            }
        }
        
        if (updates.containsKey("location")) {
            String newLocation = (String) updates.get("location");
            if (!newLocation.equals(carer.getLocation())) {
                changedFields.put("location", Map.of("old", carer.getLocation(), "new", newLocation));
                carer.setLocation(newLocation);
            }
        }
        
        if (updates.containsKey("grade")) {
            String newGrade = (String) updates.get("grade");
            if (!newGrade.equals(carer.getGrade())) {
                changedFields.put("grade", Map.of("old", carer.getGrade(), "new", newGrade));
                carer.setGrade(newGrade);
            }
        }
        
        if (updates.containsKey("qualifications")) {
            @SuppressWarnings("unchecked")
            List<String> newQualifications = (List<String>) updates.get("qualifications");
            if (!newQualifications.equals(carer.getQualifications())) {
                changedFields.put("qualifications", Map.of("old", carer.getQualifications(), "new", newQualifications));
                carer.setQualifications(newQualifications);
            }
        }
        
        if (updates.containsKey("visaStatus")) {
            String newVisaStatus = (String) updates.get("visaStatus");
            if (!newVisaStatus.equals(carer.getVisaStatus())) {
                changedFields.put("visaStatus", Map.of("old", carer.getVisaStatus(), "new", newVisaStatus));
                carer.setVisaStatus(newVisaStatus);
            }
        }
        
        if (updates.containsKey("maxTravelDistance")) {
            Integer newMaxTravelDistance = (Integer) updates.get("maxTravelDistance");
            if (!newMaxTravelDistance.equals(carer.getMaxTravelDistance())) {
                changedFields.put("maxTravelDistance", Map.of("old", carer.getMaxTravelDistance(), "new", newMaxTravelDistance));
                carer.setMaxTravelDistance(newMaxTravelDistance);
            }
        }
        
        if (!changedFields.isEmpty()) {
            Carer savedCarer = carerRepository.save(carer);
            
            // Emit CarerUpdated event
            CarerUpdated event = new CarerUpdated(carerId, changedFields, updateReason);
            kafkaTemplate.send(CARER_EVENTS_TOPIC, event.getCarerId().toString(), event);
            
            return savedCarer;
        }
        
        return carer;
    }

    public void updateAvailability(UUID carerId, List<AvailabilitySlotDto> availabilitySlots) {
        // Verify carer exists
        carerRepository.findById(carerId)
            .orElseThrow(() -> new RuntimeException("Carer not found: " + carerId));
        
        // Convert DTOs to domain objects
        List<CarerAvailability> availabilities = availabilitySlots.stream()
            .map(slot -> new CarerAvailability(carerId, slot.getDate(), 
                slot.getStartTime(), slot.getEndTime(), slot.isAvailable()))
            .collect(Collectors.toList());
        
        // Save availability updates
        availabilityRepository.saveAll(availabilities);
        
        // Convert to event objects
        List<CarerAvailabilityChanged.AvailabilitySlot> eventSlots = availabilitySlots.stream()
            .map(slot -> new CarerAvailabilityChanged.AvailabilitySlot(
                slot.getDate(), slot.getStartTime(), slot.getEndTime(), slot.isAvailable()))
            .collect(Collectors.toList());
        
        // Emit CarerAvailabilityChanged event
        CarerAvailabilityChanged event = new CarerAvailabilityChanged(carerId, eventSlots);
        kafkaTemplate.send(CARER_EVENTS_TOPIC, event.getCarerId().toString(), event);
    }

    public Carer getCarer(UUID carerId) {
        return carerRepository.findById(carerId)
            .orElseThrow(() -> new RuntimeException("Carer not found: " + carerId));
    }

    public List<Carer> getAllCarers() {
        return carerRepository.findAll();
    }

    public List<CarerAvailability> getCarerAvailability(UUID carerId) {
        return availabilityRepository.findByCarerId(carerId);
    }

    public List<CarerAvailability> getCarerAvailability(UUID carerId, LocalDate fromDate, LocalDate toDate) {
        return availabilityRepository.findByCarerIdAndDateRange(carerId, fromDate, toDate);
    }

    // DTO for availability updates
    public static class AvailabilitySlotDto {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean available;

        public AvailabilitySlotDto() {}

        public AvailabilitySlotDto(LocalDate date, LocalTime startTime, LocalTime endTime, boolean available) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.available = available;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }
}
