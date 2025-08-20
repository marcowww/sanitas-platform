package com.healthcare.staffing.carer.service;

import com.healthcare.staffing.carer.domain.Carer;
import com.healthcare.staffing.carer.domain.CarerAvailabilityBlock;
import com.healthcare.staffing.carer.repository.CarerAvailabilityBlockRepository;
import com.healthcare.staffing.carer.repository.CarerRepository;
import com.healthcare.staffing.shared.events.carer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CarerService {
    
    private final CarerRepository carerRepository;
    private final CarerAvailabilityBlockRepository availabilityBlockRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String CARER_EVENTS_TOPIC = "carer-events";

    @Autowired
    public CarerService(CarerRepository carerRepository, 
                       CarerAvailabilityBlockRepository availabilityBlockRepository,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.carerRepository = carerRepository;
        this.availabilityBlockRepository = availabilityBlockRepository;
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

    public Carer getCarer(UUID carerId) {
        return carerRepository.findById(carerId)
            .orElseThrow(() -> new RuntimeException("Carer not found: " + carerId));
    }

    public List<Carer> getAllCarers() {
        return carerRepository.findAll();
    }

    // Availability blocking methods for orchestration
    public void blockAvailability(UUID carerId, LocalDateTime startTime, LocalDateTime endTime, 
                                  UUID bookingId, String blockedBy) {
        // Verify carer exists
        carerRepository.findById(carerId)
            .orElseThrow(() -> new RuntimeException("Carer not found: " + carerId));

        // Check for overlapping blocks
        List<CarerAvailabilityBlock> overlapping = availabilityBlockRepository
            .findOverlappingBlocks(carerId, startTime, endTime);
        
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Carer has overlapping availability blocks during this time");
        }

        // Create new block
        CarerAvailabilityBlock block = new CarerAvailabilityBlock(
            carerId, bookingId, startTime, endTime, blockedBy);
        
        availabilityBlockRepository.save(block);
    }

    public void unblockAvailability(UUID carerId, UUID bookingId, String unblockedBy) {
        // Verify carer exists
        carerRepository.findById(carerId)
            .orElseThrow(() -> new RuntimeException("Carer not found: " + carerId));

        // Find and remove the block
        Optional<CarerAvailabilityBlock> blockOpt = availabilityBlockRepository
            .findByCarerIdAndBookingId(carerId, bookingId);
        
        if (blockOpt.isEmpty()) {
            throw new RuntimeException("No availability block found for carer " + carerId + " and booking " + bookingId);
        }

        availabilityBlockRepository.delete(blockOpt.get());
    }

    public List<CarerAvailabilityBlock> getCarerAvailabilityBlocks(UUID carerId) {
        return availabilityBlockRepository.findByCarerId(carerId);
    }
}
