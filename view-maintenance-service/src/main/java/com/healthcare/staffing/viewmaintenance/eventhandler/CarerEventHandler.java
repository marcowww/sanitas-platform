package com.healthcare.staffing.viewmaintenance.eventhandler;

import com.healthcare.staffing.shared.dto.EligibleShiftDto;
import com.healthcare.staffing.shared.events.carer.*;
import com.healthcare.staffing.viewmaintenance.service.EligibilityRulesEngine;
import com.healthcare.staffing.viewmaintenance.service.ViewProjectionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class CarerEventHandler {
    
    private static final Logger log = LoggerFactory.getLogger(CarerEventHandler.class);
    
    private final ViewProjectionService viewProjectionService;
    private final EligibilityRulesEngine eligibilityRulesEngine;

    @Autowired
    public CarerEventHandler(ViewProjectionService viewProjectionService,
                            EligibilityRulesEngine eligibilityRulesEngine) {
        this.viewProjectionService = viewProjectionService;
        this.eligibilityRulesEngine = eligibilityRulesEngine;
    }

    @KafkaListener(topics = "carer-events", groupId = "view-maintenance-service")
    public void handleCarerEvent(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        log.info("Received carer event: {} of type: {}", event, event.getClass().getSimpleName());
        
        if (event instanceof NewCarer) {
            handleNewCarer((NewCarer) event);
        } else if (event instanceof CarerUpdated) {
            handleCarerUpdated((CarerUpdated) event);
        } else {
            log.warn("Unhandled carer event type: {}", event.getClass().getSimpleName());
        }
    }

    private void handleNewCarer(NewCarer event) {
        log.info("Processing NewCarer event for carerId: {}", event.getCarerId());
        
        // Create carer projection data
        EligibilityRulesEngine.CarerProjection carerProjection = 
            new EligibilityRulesEngine.CarerProjection(
                event.getCarerId(),
                event.getFirstName(),
                event.getLastName(),
                event.getEmail(),
                event.getPhone(),
                event.getGrade(),
                event.getQualifications(),
                event.getLocation(),
                event.getVisaStatus(),
                event.getMaxTravelDistance()
            );
        
        // Store carer data
        viewProjectionService.storeCarerData(event.getCarerId(), carerProjection);
        log.info("Stored carer data for carerId: {}", event.getCarerId());
        
        // Find all eligible shifts for this new carer
        updateEligibilityProjectionsForNewCarer(event.getCarerId(), carerProjection);
        log.info("Updated eligibility projections for new carer: {}", event.getCarerId());
    }

    private void handleCarerUpdated(CarerUpdated event) {
        log.info("Processing CarerUpdated event for carerId: {}", event.getCarerId());
        
        // Get existing carer data
        EligibilityRulesEngine.CarerProjection existingCarer = 
            viewProjectionService.getCarerData(event.getCarerId());
        
        if (existingCarer == null) {
            log.warn("Carer not found for CarerUpdated event: {}", event.getCarerId());
            return; // Carer not found, might be a race condition
        }
        
        // Update carer data with changes
        boolean significantChange = updateCarerProjectionWithChanges(existingCarer, event);
        
        // If there were significant changes that affect eligibility, recalculate projections
        if (significantChange) {
            viewProjectionService.storeCarerData(event.getCarerId(), existingCarer);
            updateEligibilityProjectionsForModifiedCarer(event.getCarerId(), existingCarer);
            log.info("Updated carer data and eligibility projections for carerId: {}", event.getCarerId());
        } else {
            log.info("No significant changes for carerId: {}", event.getCarerId());
        }
    }

    private void updateEligibilityProjectionsForNewCarer(UUID carerId, 
                                                        EligibilityRulesEngine.CarerProjection carer) {
        List<EligibleShiftDto> eligibleShifts = new ArrayList<>();
        
        // Get all bookings and check eligibility
        Set<String> bookingIds = viewProjectionService.getAllBookingIds();
        
        for (String bookingIdStr : bookingIds) {
            UUID bookingId = UUID.fromString(bookingIdStr);
            EligibilityRulesEngine.BookingProjection booking = 
                viewProjectionService.getBookingData(bookingId);
            
            if (booking != null && eligibilityRulesEngine.isCarerEligibleForBooking(carer, booking)) {
                // Add to eligible shifts list
                EligibleShiftDto eligibleShift = createEligibleShiftDto(booking, carer);
                eligibleShifts.add(eligibleShift);
                
                // Add this carer to the booking's eligible carers list
                addCarerToBookingEligibleCarers(bookingId, carer, booking);
            }
        }
        
        // Update the available shifts projection for this carer
        viewProjectionService.updateAvailableShiftsForCarer(carerId, eligibleShifts);
    }

    private void updateEligibilityProjectionsForModifiedCarer(UUID carerId,
                                                             EligibilityRulesEngine.CarerProjection carer) {
        // Remove this carer from all projections first
        viewProjectionService.removeCarerProjections(carerId);
        
        // Then recalculate eligibility as if it's a new carer
        updateEligibilityProjectionsForNewCarer(carerId, carer);
    }

    private boolean updateCarerProjectionWithChanges(EligibilityRulesEngine.CarerProjection carer,
                                                    CarerUpdated event) {
        boolean significantChange = false;
        
        for (var entry : event.getChangedFields().entrySet()) {
            String field = entry.getKey();
            @SuppressWarnings("unchecked")
            var changeMap = (Map<String, Object>) entry.getValue();
            Object newValue = changeMap.get("new");
            
            switch (field) {
                case "firstName":
                    carer.setFirstName((String) newValue);
                    // Personal info changes don't affect eligibility, but we store them for display
                    break;
                case "lastName":
                    carer.setLastName((String) newValue);
                    // Personal info changes don't affect eligibility, but we store them for display
                    break;
                case "email":
                    carer.setEmail((String) newValue);
                    // Personal info changes don't affect eligibility, but we store them for display
                    break;
                case "phone":
                    carer.setPhone((String) newValue);
                    // Personal info changes don't affect eligibility, but we store them for display
                    break;
                case "grade":
                    carer.setGrade((String) newValue);
                    significantChange = true;
                    break;
                case "location":
                    carer.setLocation((String) newValue);
                    significantChange = true;
                    break;
                case "qualifications":
                    @SuppressWarnings("unchecked")
                    List<String> newQualifications = (List<String>) newValue;
                    carer.setQualifications(newQualifications);
                    significantChange = true;
                    break;
                case "visaStatus":
                    carer.setVisaStatus((String) newValue);
                    significantChange = true;
                    break;
                case "maxTravelDistance":
                    carer.setMaxTravelDistance((Integer) newValue);
                    significantChange = true;
                    break;
                // All relevant fields are now handled
            }
        }
        
        return significantChange;
    }

    private void addCarerToBookingEligibleCarers(UUID bookingId, 
                                                EligibilityRulesEngine.CarerProjection carer,
                                                EligibilityRulesEngine.BookingProjection booking) {
        List<com.healthcare.staffing.shared.dto.EligibleCarerDto> eligibleCarers = 
            viewProjectionService.getEligibleCarersForShift(bookingId);
        
        // Create new carer DTO
        com.healthcare.staffing.shared.dto.EligibleCarerDto carerDto = createEligibleCarerDto(carer, booking);
        
        // Add to list if not already present
        if (eligibleCarers.stream().noneMatch(c -> c.getCarerId().equals(carer.getCarerId()))) {
            eligibleCarers.add(carerDto);
            viewProjectionService.updateEligibleCarersForShift(bookingId, eligibleCarers);
        }
    }

    private EligibleShiftDto createEligibleShiftDto(EligibilityRulesEngine.BookingProjection booking,
                                                   EligibilityRulesEngine.CarerProjection carer) {
        // Calculate distance for this specific carer
        double distance = calculateDistance(carer.getLocation(), booking.getLocation());
        
        return new EligibleShiftDto(
            booking.getBookingId(),
            booking.getFacilityId(),
            null, // facilityName not available in projection (would need facility service lookup)
            booking.getShift(),
            booking.getStartTime(),
            booking.getEndTime(),
            booking.getGrade(),
            booking.getHourlyRate(),
            booking.getLocation(),
            booking.getSpecialRequirements(),
            booking.getRequiredQualifications(),
            "OPEN", // default status
            distance
        );
    }

    private com.healthcare.staffing.shared.dto.EligibleCarerDto createEligibleCarerDto(
            EligibilityRulesEngine.CarerProjection carer,
            EligibilityRulesEngine.BookingProjection booking) {
        // Calculate distance for display purposes
        double distance = calculateDistance(carer.getLocation(), booking.getLocation());
        
        return new com.healthcare.staffing.shared.dto.EligibleCarerDto(
            carer.getCarerId(),
            carer.getFirstName(),
            carer.getLastName(),
            carer.getEmail(),
            carer.getPhone(),
            carer.getLocation(),
            carer.getGrade(),
            carer.getQualifications(),
            carer.getVisaStatus(),
            carer.getMaxTravelDistance(),
            distance,
            true // available (since they passed eligibility check)
        );
    }

    private double calculateDistance(String location1, String location2) {
        // Simplified distance calculation - in production use actual geolocation
        if (location1.equals(location2)) {
            return 0.0;
        }
        return Math.random() * 50; // Random distance for demo
    }
}
