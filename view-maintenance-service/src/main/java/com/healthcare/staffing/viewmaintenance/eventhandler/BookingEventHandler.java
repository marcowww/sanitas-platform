package com.healthcare.staffing.viewmaintenance.eventhandler;

import com.healthcare.staffing.shared.dto.EligibleCarerDto;
import com.healthcare.staffing.shared.dto.EligibleShiftDto;
import com.healthcare.staffing.shared.events.booking.*;
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
import java.util.Set;
import java.util.UUID;

@Component
public class BookingEventHandler {
    
    private static final Logger log = LoggerFactory.getLogger(BookingEventHandler.class);
    
    private final ViewProjectionService viewProjectionService;
    private final EligibilityRulesEngine eligibilityRulesEngine;

    @Autowired
    public BookingEventHandler(ViewProjectionService viewProjectionService,
                              EligibilityRulesEngine eligibilityRulesEngine) {
        this.viewProjectionService = viewProjectionService;
        this.eligibilityRulesEngine = eligibilityRulesEngine;
    }

    @KafkaListener(topics = "booking-events", groupId = "view-maintenance-service")
    public void handleBookingEvent(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        log.info("Received booking event: {} of type: {}", event, event.getClass().getSimpleName());
        
        if (event instanceof BookingCreated) {
            handleBookingCreated((BookingCreated) event);
        } else if (event instanceof BookingModified) {
            handleBookingModified((BookingModified) event);
        } else if (event instanceof BookingCancelled) {
            handleBookingCancelled((BookingCancelled) event);
        } else if (event instanceof BookingBooked) {
            handleBookingBooked((BookingBooked) event);
        } else if (event instanceof BookingPullout) {
            handleBookingPullout((BookingPullout) event);
        } else {
            log.warn("Unhandled booking event type: {}", event.getClass().getSimpleName());
        }
    }

    private void handleBookingCreated(BookingCreated event) {
        log.info("Processing BookingCreated event for bookingId: {}", event.getBookingId());
        
        // Create booking projection data
        EligibilityRulesEngine.BookingProjection bookingProjection = 
            new EligibilityRulesEngine.BookingProjection(
                event.getBookingId(),
                event.getFacilityId(),
                event.getShift(),
                event.getGrade(),
                event.getHourlyRate(),
                event.getRequiredQualifications(),
                event.getLocation(),
                event.getSpecialRequirements(),
                event.getStartTime(),
                event.getEndTime()
            );
        
        // Store booking data
        viewProjectionService.storeBookingData(event.getBookingId(), bookingProjection);
        log.info("Stored booking data for bookingId: {}", event.getBookingId());
        
        // Find all eligible carers for this new booking
        updateEligibilityProjectionsForNewBooking(event.getBookingId(), bookingProjection);
        log.info("Updated eligibility projections for new booking: {}", event.getBookingId());
    }

    private void handleBookingModified(BookingModified event) {
        // Get existing booking data
        EligibilityRulesEngine.BookingProjection existingBooking = 
            viewProjectionService.getBookingData(event.getBookingId());
        
        if (existingBooking == null) {
            return; // Booking not found, might be a race condition
        }
        
        // Update booking data with changes
        boolean significantChange = updateBookingProjectionWithChanges(existingBooking, event);
        
        // If there were significant changes that affect eligibility, recalculate projections
        if (significantChange) {
            viewProjectionService.storeBookingData(event.getBookingId(), existingBooking);
            updateEligibilityProjectionsForModifiedBooking(event.getBookingId(), existingBooking);
        }
    }

    private void handleBookingCancelled(BookingCancelled event) {
        // Remove all projections related to this booking
        viewProjectionService.removeBookingProjections(event.getBookingId());
    }

    private void handleBookingBooked(BookingBooked event) {
        // When a booking is booked, remove it from all carer availability lists
        // except the assigned carer (who might still see it as "booked")
        removeBookingFromCarerProjectionsExcept(event.getBookingId(), event.getCarerId());
        
        // Update the booking status in the eligibility list to show it's no longer available
        updateBookingStatusInProjections(event.getBookingId(), "BOOKED");
    }

    private void handleBookingPullout(BookingPullout event) {
        // When a carer pulls out, the booking becomes available again
        // Recalculate eligibility for all carers
        EligibilityRulesEngine.BookingProjection bookingProjection = 
            viewProjectionService.getBookingData(event.getBookingId());
        
        if (bookingProjection != null) {
            updateEligibilityProjectionsForNewBooking(event.getBookingId(), bookingProjection);
            updateBookingStatusInProjections(event.getBookingId(), "OPEN");
        }
    }

    private void updateEligibilityProjectionsForNewBooking(UUID bookingId, 
                                                          EligibilityRulesEngine.BookingProjection booking) {
        List<EligibleCarerDto> eligibleCarers = new ArrayList<>();
        
        // Get all carers and check eligibility
        Set<String> carerIds = viewProjectionService.getAllCarerIds();
        
        for (String carerIdStr : carerIds) {
            UUID carerId = UUID.fromString(carerIdStr);
            EligibilityRulesEngine.CarerProjection carer = viewProjectionService.getCarerData(carerId);
            
            if (carer != null && eligibilityRulesEngine.isCarerEligibleForBooking(carer, booking)) {
                // Add to eligible carers list
                EligibleCarerDto eligibleCarer = createEligibleCarerDto(carer, booking);
                eligibleCarers.add(eligibleCarer);
                
                // Add this booking to the carer's available shifts
                addBookingToCarerAvailableShifts(carerId, booking);
            }
        }
        
        // Update the eligible carers projection for this booking
        viewProjectionService.updateEligibleCarersForShift(bookingId, eligibleCarers);
    }

    private void updateEligibilityProjectionsForModifiedBooking(UUID bookingId,
                                                               EligibilityRulesEngine.BookingProjection booking) {
        // Remove this booking from all carer projections first
        viewProjectionService.removeBookingProjections(bookingId);
        
        // Then recalculate eligibility as if it's a new booking
        updateEligibilityProjectionsForNewBooking(bookingId, booking);
    }

    private boolean updateBookingProjectionWithChanges(EligibilityRulesEngine.BookingProjection booking,
                                                      BookingModified event) {
        boolean significantChange = false;
        
        for (var entry : event.getChangedFields().entrySet()) {
            String field = entry.getKey();
            @SuppressWarnings("unchecked")
            var changeMap = (java.util.Map<String, Object>) entry.getValue();
            Object newValue = changeMap.get("new");
            
            switch (field) {
                case "shift":
                    booking.setShift((String) newValue);
                    // Shift name changes don't affect eligibility, but we store them for display
                    break;
                case "grade":
                    booking.setGrade((String) newValue);
                    significantChange = true;
                    break;
                case "hourlyRate":
                    booking.setHourlyRate((java.math.BigDecimal) newValue);
                    // Hourly rate changes don't affect eligibility, but we store them for display
                    break;
                case "location":
                    booking.setLocation((String) newValue);
                    significantChange = true;
                    break;
                case "specialRequirements":
                    booking.setSpecialRequirements((String) newValue);
                    // Special requirements changes don't affect basic eligibility, but we store them for display
                    break;
                case "requiredQualifications":
                    @SuppressWarnings("unchecked")
                    List<String> newQualifications = (List<String>) newValue;
                    booking.setRequiredQualifications(newQualifications);
                    significantChange = true;
                    break;
                case "startTime":
                case "endTime":
                    // Time changes affect availability calculations
                    significantChange = true;
                    break;
                // All relevant fields are now handled
            }
        }
        
        return significantChange;
    }

    private void removeBookingFromCarerProjectionsExcept(UUID bookingId, UUID exceptCarerId) {
        Set<String> carerIds = viewProjectionService.getAllCarerIds();
        
        for (String carerIdStr : carerIds) {
            UUID carerId = UUID.fromString(carerIdStr);
            if (!carerId.equals(exceptCarerId)) {
                List<EligibleShiftDto> availableShifts = 
                    viewProjectionService.getAvailableShiftsForCarer(carerId);
                
                availableShifts.removeIf(shift -> shift.getBookingId().equals(bookingId));
                viewProjectionService.updateAvailableShiftsForCarer(carerId, availableShifts);
            }
        }
    }

    private void updateBookingStatusInProjections(UUID bookingId, String status) {
        // Update status in all carer availability lists
        Set<String> carerIds = viewProjectionService.getAllCarerIds();
        
        for (String carerIdStr : carerIds) {
            UUID carerId = UUID.fromString(carerIdStr);
            List<EligibleShiftDto> availableShifts = 
                viewProjectionService.getAvailableShiftsForCarer(carerId);
            
            for (EligibleShiftDto shift : availableShifts) {
                if (shift.getBookingId().equals(bookingId)) {
                    shift.setStatus(status);
                }
            }
            
            viewProjectionService.updateAvailableShiftsForCarer(carerId, availableShifts);
        }
    }

    private void addBookingToCarerAvailableShifts(UUID carerId, 
                                                 EligibilityRulesEngine.BookingProjection booking) {
        List<EligibleShiftDto> availableShifts = 
            viewProjectionService.getAvailableShiftsForCarer(carerId);
        
        // Create new shift DTO
        EligibleShiftDto shiftDto = createEligibleShiftDto(booking);
        
        // Add to list if not already present
        if (availableShifts.stream().noneMatch(s -> s.getBookingId().equals(booking.getBookingId()))) {
            availableShifts.add(shiftDto);
            viewProjectionService.updateAvailableShiftsForCarer(carerId, availableShifts);
        }
    }

    private EligibleCarerDto createEligibleCarerDto(EligibilityRulesEngine.CarerProjection carer,
                                                   EligibilityRulesEngine.BookingProjection booking) {
        // Calculate distance for display purposes
        double distance = calculateDistance(carer.getLocation(), booking.getLocation());
        
        return new EligibleCarerDto(
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

    private EligibleShiftDto createEligibleShiftDto(EligibilityRulesEngine.BookingProjection booking) {
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
            null // distance calculated per carer
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
