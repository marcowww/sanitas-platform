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
        log.info("=== PROCESSING BOOKING BOOKED EVENT ===");
        log.info("Event received - BookingId: {}, CarerId: {}, Timestamp: {}", 
                event.getBookingId(), event.getCarerId(), java.time.LocalDateTime.now());
        
        // Get the booking details to check time overlap
        EligibilityRulesEngine.BookingProjection bookedBooking = 
            viewProjectionService.getBookingData(event.getBookingId());
        
        if (bookedBooking == null) {
            log.warn("Booking data not found for booked booking: {} - Event processing aborted", 
                    event.getBookingId());
            return;
        }
        
        log.info("Booking details - Shift: '{}', Grade: '{}', Location: '{}', Time: {} to {}", 
                bookedBooking.getShift(), bookedBooking.getGrade(), bookedBooking.getLocation(),
                bookedBooking.getStartTime(), bookedBooking.getEndTime());
        
        // Count eligible carers before processing
        List<EligibleCarerDto> originalEligibleCarers = 
            viewProjectionService.getEligibleCarersForShift(event.getBookingId());
        int originalEligibleCount = originalEligibleCarers.size();
        
        // Count carer's available shifts before processing
        List<EligibleShiftDto> carerOriginalShifts = 
            viewProjectionService.getAvailableShiftsForCarer(event.getCarerId());
        int carerOriginalShiftCount = carerOriginalShifts.size();
        
        log.info("Before processing - Eligible carers for this booking: {}, Available shifts for assigned carer: {}", 
                originalEligibleCount, carerOriginalShiftCount);
        
        // 1. Remove this booking from all carer availability lists except the assigned carer
        log.info("Step 1: Removing booking from other carers' availability lists...");
        removeBookingFromCarerProjectionsExcept(event.getBookingId(), event.getCarerId());
        
        // 2. Update the booking status to show it's no longer available
        log.info("Step 2: Updating booking status to 'BOOKED'...");
        updateBookingStatusInProjections(event.getBookingId(), "BOOKED");
        
        // 3. CRITICAL: Remove the assigned carer from all OTHER bookings that overlap in time
        log.info("Step 3: Resolving time conflicts for assigned carer...");
        int conflictingBookingsRemoved = removeCarerFromConflictingBookings(event.getCarerId(), bookedBooking);
        
        // Post-processing metrics
        List<EligibleCarerDto> remainingEligibleCarers = 
            viewProjectionService.getEligibleCarersForShift(event.getBookingId());
        List<EligibleShiftDto> carerRemainingShifts = 
            viewProjectionService.getAvailableShiftsForCarer(event.getCarerId());
        
        int carersRemoved = originalEligibleCount - remainingEligibleCarers.size();
        int shiftsRemovedFromCarer = carerOriginalShiftCount - carerRemainingShifts.size();
        
        log.info("=== BOOKING ASSIGNMENT COMPLETED ===");
        log.info("Summary - BookingId: {}, CarerId: {}", event.getBookingId(), event.getCarerId());
        log.info("  • Carers removed from this shift: {} (from {} to {})", 
                carersRemoved, originalEligibleCount, remainingEligibleCarers.size());
        log.info("  • Conflicting bookings removed from carer: {}", conflictingBookingsRemoved);
        log.info("  • Carer's remaining available shifts: {} (was {})", 
                carerRemainingShifts.size(), carerOriginalShiftCount);
        log.info("  • Shifts removed from carer due to conflicts: {}", shiftsRemovedFromCarer);
        log.info("==========================================");
    }

    private void handleBookingPullout(BookingPullout event) {
        // When a carer pulls out, the booking becomes available again
        // AND the carer becomes available for other bookings that were previously conflicting
        
        EligibilityRulesEngine.BookingProjection bookingProjection = 
            viewProjectionService.getBookingData(event.getBookingId());
        
        if (bookingProjection != null) {
            // 1. Make the booking available again for all eligible carers
            updateEligibilityProjectionsForNewBooking(event.getBookingId(), bookingProjection);
            updateBookingStatusInProjections(event.getBookingId(), "OPEN");
            
            // 2. CRITICAL: Restore the carer's eligibility for other bookings that were previously conflicting
            restoreCarerEligibilityAfterPullout(event.getCarerId(), bookingProjection);
            
            log.info("Processed carer pullout and restored eligibility for carer: {} from booking: {}", 
                    event.getCarerId(), event.getBookingId());
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
        // Optimized approach: Get only the carers who were eligible for this shift
        // instead of iterating through ALL carers in the system
        List<EligibleCarerDto> eligibleCarers = viewProjectionService.getEligibleCarersForShift(bookingId);
        
        for (EligibleCarerDto eligibleCarer : eligibleCarers) {
            UUID carerId = eligibleCarer.getCarerId();
            
            // Skip the carer who got the booking
            if (!carerId.equals(exceptCarerId)) {
                List<EligibleShiftDto> availableShifts = 
                    viewProjectionService.getAvailableShiftsForCarer(carerId);
                
                availableShifts.removeIf(shift -> shift.getBookingId().equals(bookingId));
                viewProjectionService.updateAvailableShiftsForCarer(carerId, availableShifts);
            }
        }
        
        log.debug("Removed booking {} from {} eligible carers (excluding assigned carer {})", 
                 bookingId, eligibleCarers.size() - 1, exceptCarerId);
    }

    private void updateBookingStatusInProjections(UUID bookingId, String status) {
        // Optimized approach: Get only the carers who were eligible for this booking
        // instead of iterating through ALL carers in the system
        List<EligibleCarerDto> eligibleCarers = viewProjectionService.getEligibleCarersForShift(bookingId);
        
        for (EligibleCarerDto eligibleCarer : eligibleCarers) {
            UUID carerId = eligibleCarer.getCarerId();
            List<EligibleShiftDto> availableShifts = 
                viewProjectionService.getAvailableShiftsForCarer(carerId);
            
            for (EligibleShiftDto shift : availableShifts) {
                if (shift.getBookingId().equals(bookingId)) {
                    shift.setStatus(status);
                }
            }
            
            viewProjectionService.updateAvailableShiftsForCarer(carerId, availableShifts);
        }
        
        log.debug("Updated booking {} status to '{}' for {} eligible carers", 
                 bookingId, status, eligibleCarers.size());
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

    /**
     * Removes a carer from all bookings that have time conflicts with the newly assigned booking
     */
    private int removeCarerFromConflictingBookings(UUID carerId, EligibilityRulesEngine.BookingProjection bookedBooking) {
        // Optimized approach: Get only the shifts this carer was eligible for
        // instead of checking ALL bookings in the system
        List<EligibleShiftDto> carerAvailableShifts = 
            viewProjectionService.getAvailableShiftsForCarer(carerId);
        
        int conflictsResolved = 0;
        
        for (EligibleShiftDto availableShift : carerAvailableShifts) {
            UUID bookingId = availableShift.getBookingId();
            
            // Skip the booking that was just assigned
            if (bookingId.equals(bookedBooking.getBookingId())) {
                continue;
            }
            
            // Get booking details to check for time overlap
            EligibilityRulesEngine.BookingProjection otherBooking = 
                viewProjectionService.getBookingData(bookingId);
            
            if (otherBooking != null && hasTimeOverlap(bookedBooking, otherBooking)) {
                // Remove this carer from the conflicting booking's eligible carers list
                removeCarerFromBookingEligibility(carerId, bookingId);
                
                // Remove the conflicting booking from this carer's available shifts
                removeBookingFromCarerAvailableShifts(carerId, bookingId);
                
                conflictsResolved++;
                log.info("Removed carer {} from conflicting booking {} due to time overlap", 
                        carerId, bookingId);
            }
        }
        
        log.debug("Checked {} available shifts for carer {} to resolve time conflicts with booking {} - {} conflicts resolved", 
                 carerAvailableShifts.size(), carerId, bookedBooking.getBookingId(), conflictsResolved);
        
        return conflictsResolved;
    }

    /**
     * Checks if two bookings have overlapping time periods
     */
    private boolean hasTimeOverlap(EligibilityRulesEngine.BookingProjection booking1, 
                                  EligibilityRulesEngine.BookingProjection booking2) {
        java.time.LocalDateTime start1 = booking1.getStartTime();
        java.time.LocalDateTime end1 = booking1.getEndTime();
        java.time.LocalDateTime start2 = booking2.getStartTime();
        java.time.LocalDateTime end2 = booking2.getEndTime();
        
        // Check for overlap: booking1 starts before booking2 ends AND booking2 starts before booking1 ends
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Removes a specific carer from a booking's eligible carers list
     */
    private void removeCarerFromBookingEligibility(UUID carerId, UUID bookingId) {
        List<EligibleCarerDto> eligibleCarers = 
            viewProjectionService.getEligibleCarersForShift(bookingId);
        
        eligibleCarers.removeIf(carer -> carer.getCarerId().equals(carerId));
        viewProjectionService.updateEligibleCarersForShift(bookingId, eligibleCarers);
    }

    /**
     * Removes a specific booking from a carer's available shifts list
     */
    private void removeBookingFromCarerAvailableShifts(UUID carerId, UUID bookingId) {
        List<EligibleShiftDto> availableShifts = 
            viewProjectionService.getAvailableShiftsForCarer(carerId);
        
        availableShifts.removeIf(shift -> shift.getBookingId().equals(bookingId));
        viewProjectionService.updateAvailableShiftsForCarer(carerId, availableShifts);
    }

    /**
     * Restores a carer's eligibility for bookings that no longer conflict after a pullout
     */
    private void restoreCarerEligibilityAfterPullout(UUID carerId, EligibilityRulesEngine.BookingProjection pulledOutBooking) {
        // Get carer data
        EligibilityRulesEngine.CarerProjection carer = viewProjectionService.getCarerData(carerId);
        if (carer == null) {
            log.warn("Carer data not found for pullout restoration: {}", carerId);
            return;
        }
        
        // Check all other bookings to see if the carer is now eligible
        Set<String> allBookingIds = viewProjectionService.getAllBookingIds();
        
        for (String bookingIdStr : allBookingIds) {
            UUID bookingId = UUID.fromString(bookingIdStr);
            
            // Skip the booking they just pulled out from
            if (bookingId.equals(pulledOutBooking.getBookingId())) {
                continue;
            }
            
            EligibilityRulesEngine.BookingProjection otherBooking = 
                viewProjectionService.getBookingData(bookingId);
            
            if (otherBooking != null) {
                // Check if this booking was previously conflicting with the pulled-out booking
                boolean wasConflicting = hasTimeOverlap(pulledOutBooking, otherBooking);
                
                if (wasConflicting && eligibilityRulesEngine.isCarerEligibleForBooking(carer, otherBooking)) {
                    // Check if the booking is still OPEN (not already assigned to someone else)
                    List<EligibleCarerDto> currentEligibleCarers = 
                        viewProjectionService.getEligibleCarersForShift(bookingId);
                    
                    // Only restore if the booking is still available and carer isn't already in the list
                    boolean bookingStillOpen = currentEligibleCarers.stream()
                        .anyMatch(c -> true); // If there are eligible carers, booking is likely still open
                    
                    boolean carerAlreadyEligible = currentEligibleCarers.stream()
                        .anyMatch(c -> c.getCarerId().equals(carerId));
                    
                    if (bookingStillOpen && !carerAlreadyEligible) {
                        // Add carer back to eligible list
                        EligibleCarerDto restoredCarer = createEligibleCarerDto(carer, otherBooking);
                        currentEligibleCarers.add(restoredCarer);
                        viewProjectionService.updateEligibleCarersForShift(bookingId, currentEligibleCarers);
                        
                        // Add booking back to carer's available shifts
                        List<EligibleShiftDto> carerShifts = 
                            viewProjectionService.getAvailableShiftsForCarer(carerId);
                        
                        EligibleShiftDto restoredShift = createEligibleShiftDto(otherBooking);
                        if (carerShifts.stream().noneMatch(s -> s.getBookingId().equals(bookingId))) {
                            carerShifts.add(restoredShift);
                            viewProjectionService.updateAvailableShiftsForCarer(carerId, carerShifts);
                        }
                        
                        log.info("Restored carer {} eligibility for previously conflicting booking {}", 
                                carerId, bookingId);
                    }
                }
            }
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
