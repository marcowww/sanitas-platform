package com.healthcare.staffing.orchestration.service;

import com.healthcare.staffing.orchestration.client.BookingServiceClient;
import com.healthcare.staffing.orchestration.client.CarerServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingOrchestrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingOrchestrationService.class);
    
    private final BookingServiceClient bookingServiceClient;
    private final CarerServiceClient carerServiceClient;
    
    @Autowired
    public BookingOrchestrationService(BookingServiceClient bookingServiceClient, 
                                     CarerServiceClient carerServiceClient) {
        this.bookingServiceClient = bookingServiceClient;
        this.carerServiceClient = carerServiceClient;
    }
    
    /**
     * Orchestrates the assignment of a carer to a booking.
     * This method implements the two-phase operation with rollback on failure.
     */
    public void assignCarerToBooking(UUID bookingId, UUID carerId, String bookedBy, 
                                   LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Starting carer assignment orchestration - Booking: {}, Carer: {}, BookedBy: {}", 
            bookingId, carerId, bookedBy);
        
        try {
            // Phase 1: Book the carer in the booking service
            logger.info("Phase 1: Booking carer {} in booking service for booking {}", carerId, bookingId);
            bookingServiceClient.bookCarer(bookingId, carerId, bookedBy);
            
            try {
                // Phase 2: Block carer availability
                logger.info("Phase 2: Blocking availability for carer {} for booking {}", carerId, bookingId);
                carerServiceClient.blockAvailability(carerId, bookingId, startTime, endTime, bookedBy);
                
                logger.info("Successfully completed carer assignment orchestration - Booking: {}, Carer: {}", 
                    bookingId, carerId);
                    
            } catch (CarerServiceClient.CarerServiceException e) {
                // Phase 2 failed - rollback phase 1
                logger.error("Phase 2 failed, initiating rollback for booking {}: {}", bookingId, e.getMessage());
                
                try {
                    logger.info("Rolling back booking assignment for booking {}", bookingId);
                    bookingServiceClient.pulloutCarer(bookingId, carerId, "System rollback", "SYSTEM");
                    logger.info("Successfully rolled back booking assignment for booking {}", bookingId);
                } catch (Exception rollbackException) {
                    logger.error("CRITICAL: Rollback failed for booking {}. Manual intervention required: {}", 
                        bookingId, rollbackException.getMessage());
                    throw new OrchestrationException(
                        "Carer assignment failed and rollback failed. Manual intervention required.", 
                        rollbackException);
                }
                
                throw new OrchestrationException("Carer not available: " + e.getMessage(), e);
            }
            
        } catch (BookingServiceClient.BookingServiceException e) {
            // Phase 1 failed - no rollback needed
            logger.error("Phase 1 failed for booking {}: {}", bookingId, e.getMessage());
            throw new OrchestrationException("Booking not available: " + e.getMessage(), e);
        }
    }
    
    /**
     * Orchestrates the removal of a carer from a booking.
     * This method implements the two-phase operation for pullout.
     */
    public void removeCarerFromBooking(UUID bookingId, UUID carerId, String pulloutReason, String pulloutBy) {
        logger.info("Starting carer removal orchestration - Booking: {}, Carer: {}, Reason: {}", 
            bookingId, carerId, pulloutReason);
        
        try {
            // Phase 1: Remove carer from booking service
            logger.info("Phase 1: Removing carer {} from booking service for booking {}", carerId, bookingId);
            bookingServiceClient.pulloutCarer(bookingId, carerId, pulloutReason, pulloutBy);
            
            try {
                // Phase 2: Unblock carer availability
                logger.info("Phase 2: Unblocking availability for carer {} for booking {}", carerId, bookingId);
                carerServiceClient.unblockAvailability(carerId, bookingId, pulloutBy);
                
                logger.info("Successfully completed carer removal orchestration - Booking: {}, Carer: {}", 
                    bookingId, carerId);
                    
            } catch (CarerServiceClient.CarerServiceException e) {
                // Phase 2 failed - rollback phase 1
                logger.error("Phase 2 failed during removal, initiating rollback for booking {}: {}", 
                    bookingId, e.getMessage());
                
                try {
                    logger.info("Rolling back carer removal for booking {}", bookingId);
                    bookingServiceClient.bookCarer(bookingId, carerId, pulloutBy);
                    logger.info("Successfully rolled back carer removal for booking {}", bookingId);
                } catch (Exception rollbackException) {
                    logger.error("CRITICAL: Rollback failed for booking removal {}. Manual intervention required: {}", 
                        bookingId, rollbackException.getMessage());
                    throw new OrchestrationException(
                        "Carer removal failed and rollback failed. Manual intervention required.", 
                        rollbackException);
                }
                
                throw new OrchestrationException("Failed to unblock carer availability: " + e.getMessage(), e);
            }
            
        } catch (BookingServiceClient.BookingServiceException e) {
            // Phase 1 failed - no rollback needed
            logger.error("Phase 1 failed during removal for booking {}: {}", bookingId, e.getMessage());
            throw new OrchestrationException("Failed to remove carer from booking: " + e.getMessage(), e);
        }
    }
    
    public static class OrchestrationException extends RuntimeException {
        public OrchestrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
