package com.healthcare.staffing.orchestration.controller;

import com.healthcare.staffing.orchestration.dto.AssignCarerRequest;
import com.healthcare.staffing.orchestration.service.BookingOrchestrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/booking-orchestration")
public class BookingOrchestrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingOrchestrationController.class);
    
    private final BookingOrchestrationService orchestrationService;
    
    @Autowired
    public BookingOrchestrationController(BookingOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }
    
    @PostMapping("/assign-carer")
    public ResponseEntity<Map<String, Object>> assignCarer(@Valid @RequestBody AssignCarerRequest request) {
        logger.info("Received carer assignment request - Booking: {}, Carer: {}", 
            request.getBookingId(), request.getCarerId());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            orchestrationService.assignCarerToBooking(
                request.getBookingId(),
                request.getCarerId(),
                request.getBookedBy(),
                request.getStartTime(),
                request.getEndTime()
            );
            
            response.put("success", true);
            response.put("message", "Carer successfully assigned to booking");
            response.put("bookingId", request.getBookingId());
            response.put("carerId", request.getCarerId());
            
            logger.info("Successfully assigned carer {} to booking {}", 
                request.getCarerId(), request.getBookingId());
            
            return ResponseEntity.ok(response);
            
        } catch (BookingOrchestrationService.OrchestrationException e) {
            logger.error("Failed to assign carer {} to booking {}: {}", 
                request.getCarerId(), request.getBookingId(), e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("bookingId", request.getBookingId());
            response.put("carerId", request.getCarerId());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Unexpected error during carer assignment for booking {}: {}", 
                request.getBookingId(), e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", "Internal server error occurred");
            response.put("bookingId", request.getBookingId());
            response.put("carerId", request.getCarerId());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/remove-carer")
    public ResponseEntity<Map<String, Object>> removeCarer(@Valid @RequestBody RemoveCarerRequest request) {
        logger.info("Received carer removal request - Booking: {}, Carer: {}", 
            request.getBookingId(), request.getCarerId());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            orchestrationService.removeCarerFromBooking(
                request.getBookingId(),
                request.getCarerId(),
                request.getPulloutReason(),
                request.getPulloutBy()
            );
            
            response.put("success", true);
            response.put("message", "Carer successfully removed from booking");
            response.put("bookingId", request.getBookingId());
            response.put("carerId", request.getCarerId());
            
            logger.info("Successfully removed carer {} from booking {}", 
                request.getCarerId(), request.getBookingId());
            
            return ResponseEntity.ok(response);
            
        } catch (BookingOrchestrationService.OrchestrationException e) {
            logger.error("Failed to remove carer {} from booking {}: {}", 
                request.getCarerId(), request.getBookingId(), e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("bookingId", request.getBookingId());
            response.put("carerId", request.getCarerId());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Unexpected error during carer removal for booking {}: {}", 
                request.getBookingId(), e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", "Internal server error occurred");
            response.put("bookingId", request.getBookingId());
            response.put("carerId", request.getCarerId());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "booking-orchestration-service");
        return ResponseEntity.ok(response);
    }
    
    // Remove carer request DTO
    public static class RemoveCarerRequest {
        private UUID bookingId;
        private UUID carerId;
        private String pulloutReason;
        private String pulloutBy;
        
        public RemoveCarerRequest() {}
        
        public UUID getBookingId() { return bookingId; }
        public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
        
        public UUID getCarerId() { return carerId; }
        public void setCarerId(UUID carerId) { this.carerId = carerId; }
        
        public String getPulloutReason() { return pulloutReason; }
        public void setPulloutReason(String pulloutReason) { this.pulloutReason = pulloutReason; }
        
        public String getPulloutBy() { return pulloutBy; }
        public void setPulloutBy(String pulloutBy) { this.pulloutBy = pulloutBy; }
    }
}
