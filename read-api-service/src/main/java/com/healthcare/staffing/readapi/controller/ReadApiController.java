package com.healthcare.staffing.readapi.controller;

import com.healthcare.staffing.readapi.service.ReadProjectionService;
import com.healthcare.staffing.shared.dto.EligibleCarerDto;
import com.healthcare.staffing.shared.dto.EligibleShiftDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/read")
public class ReadApiController {
    
    private final ReadProjectionService readProjectionService;

    @Autowired
    public ReadApiController(ReadProjectionService readProjectionService) {
        this.readProjectionService = readProjectionService;
    }

    /**
     * Get eligible shifts for a carer with optional distance filtering and sorting
     */
    @GetMapping("/carer/{carerId}/eligible-shifts")
    public ResponseEntity<List<EligibleShiftDto>> getEligibleShiftsForCarer(
            @PathVariable UUID carerId,
            @RequestParam(value = "maxDistance", required = false) Double maxDistance,
            @RequestParam(value = "sortByDistance", defaultValue = "false") boolean sortByDistance) {
        
        List<EligibleShiftDto> eligibleShifts;
        
        if (maxDistance != null) {
            eligibleShifts = readProjectionService.getEligibleShiftsForCarerWithinDistance(carerId, maxDistance);
        } else if (sortByDistance) {
            eligibleShifts = readProjectionService.getEligibleShiftsForCarerSortedByDistance(carerId);
        } else {
            eligibleShifts = readProjectionService.getEligibleShiftsForCarer(carerId);
        }
        
        return ResponseEntity.ok(eligibleShifts);
    }

    /**
     * Get eligible carers for a shift with optional distance filtering and sorting
     */
    @GetMapping("/shift/{shiftId}/eligible-carers")
    public ResponseEntity<List<EligibleCarerDto>> getEligibleCarersForShift(
            @PathVariable UUID shiftId,
            @RequestParam(value = "maxDistance", required = false) Double maxDistance,
            @RequestParam(value = "sortByDistance", defaultValue = "false") boolean sortByDistance) {
        
        List<EligibleCarerDto> eligibleCarers;
        
        if (maxDistance != null) {
            eligibleCarers = readProjectionService.getEligibleCarersForShiftWithinDistance(shiftId, maxDistance);
        } else if (sortByDistance) {
            eligibleCarers = readProjectionService.getEligibleCarersForShiftSortedByDistance(shiftId);
        } else {
            eligibleCarers = readProjectionService.getEligibleCarersForShift(shiftId);
        }
        
        return ResponseEntity.ok(eligibleCarers);
    }

    /**
     * Get eligible shifts for a carer filtered by location
     */
    @GetMapping("/carer/{carerId}/eligible-shifts/location/{location}")
    public ResponseEntity<List<EligibleShiftDto>> getEligibleShiftsForCarerByLocation(
            @PathVariable UUID carerId, 
            @PathVariable String location) {
        List<EligibleShiftDto> eligibleShifts = 
            readProjectionService.getEligibleShiftsForCarerByLocation(carerId, location);
        return ResponseEntity.ok(eligibleShifts);
    }

    /**
     * Get eligible carers for a shift filtered by grade
     */
    @GetMapping("/shift/{shiftId}/eligible-carers/grade/{grade}")
    public ResponseEntity<List<EligibleCarerDto>> getEligibleCarersForShiftByGrade(
            @PathVariable UUID shiftId, 
            @PathVariable String grade) {
        List<EligibleCarerDto> eligibleCarers = 
            readProjectionService.getEligibleCarersForShiftByGrade(shiftId, grade);
        return ResponseEntity.ok(eligibleCarers);
    }




    /**
     * Check if a specific carer is eligible for a specific shift
     */
    @GetMapping("/carer/{carerId}/shift/{shiftId}/eligible")
    public ResponseEntity<EligibilityCheckResponse> checkCarerEligibilityForShift(
            @PathVariable UUID carerId, 
            @PathVariable UUID shiftId) {
        boolean isEligible = readProjectionService.isCarerEligibleForShift(carerId, shiftId);
        EligibilityCheckResponse response = new EligibilityCheckResponse(carerId, shiftId, isEligible);
        return ResponseEntity.ok(response);
    }

    /**
     * Get count of eligible shifts for a carer
     */
    @GetMapping("/carer/{carerId}/eligible-shifts/count")
    public ResponseEntity<CountResponse> getEligibleShiftsCount(@PathVariable UUID carerId) {
        long count = readProjectionService.getEligibleShiftsCount(carerId);
        return ResponseEntity.ok(new CountResponse(count));
    }

    /**
     * Get count of eligible carers for a shift
     */
    @GetMapping("/shift/{shiftId}/eligible-carers/count")
    public ResponseEntity<CountResponse> getEligibleCarersCount(@PathVariable UUID shiftId) {
        long count = readProjectionService.getEligibleCarersCount(shiftId);
        return ResponseEntity.ok(new CountResponse(count));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("Read API Service is healthy", System.currentTimeMillis()));
    }

    // Response DTOs
    public static class EligibilityCheckResponse {
        private UUID carerId;
        private UUID shiftId;
        private boolean eligible;

        public EligibilityCheckResponse(UUID carerId, UUID shiftId, boolean eligible) {
            this.carerId = carerId;
            this.shiftId = shiftId;
            this.eligible = eligible;
        }

        public UUID getCarerId() { return carerId; }
        public void setCarerId(UUID carerId) { this.carerId = carerId; }

        public UUID getShiftId() { return shiftId; }
        public void setShiftId(UUID shiftId) { this.shiftId = shiftId; }

        public boolean isEligible() { return eligible; }
        public void setEligible(boolean eligible) { this.eligible = eligible; }
    }

    public static class CountResponse {
        private long count;

        public CountResponse(long count) {
            this.count = count;
        }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class HealthResponse {
        private String status;
        private long timestamp;

        public HealthResponse(String status, long timestamp) {
            this.status = status;
            this.timestamp = timestamp;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
