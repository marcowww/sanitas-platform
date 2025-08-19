package com.healthcare.staffing.viewmaintenance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.staffing.shared.dto.EligibleCarerDto;
import com.healthcare.staffing.shared.dto.EligibleShiftDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ViewProjectionService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Redis key prefixes
    private static final String AVAILABLE_SHIFTS_PREFIX = "AvailableShiftsPerCarer:";
    private static final String ELIGIBLE_CARERS_PREFIX = "EligibleCarersPerShift:";
    private static final String CARER_DATA_PREFIX = "CarerData:";
    private static final String BOOKING_DATA_PREFIX = "BookingData:";
    
    // TTL for projections (24 hours)
    private static final long PROJECTION_TTL_HOURS = 24;

    @Autowired
    public ViewProjectionService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Updates the list of available shifts for a specific carer
     */
    public void updateAvailableShiftsForCarer(UUID carerId, List<EligibleShiftDto> eligibleShifts) {
        String key = AVAILABLE_SHIFTS_PREFIX + carerId.toString();
        try {
            String jsonValue = objectMapper.writeValueAsString(eligibleShifts);
            redisTemplate.opsForValue().set(key, jsonValue, PROJECTION_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize eligible shifts for carer: " + carerId, e);
        }
    }

    /**
     * Updates the list of eligible carers for a specific shift
     */
    public void updateEligibleCarersForShift(UUID bookingId, List<EligibleCarerDto> eligibleCarers) {
        String key = ELIGIBLE_CARERS_PREFIX + bookingId.toString();
        try {
            String jsonValue = objectMapper.writeValueAsString(eligibleCarers);
            redisTemplate.opsForValue().set(key, jsonValue, PROJECTION_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize eligible carers for shift: " + bookingId, e);
        }
    }

    /**
     * Retrieves available shifts for a carer
     */
    public List<EligibleShiftDto> getAvailableShiftsForCarer(UUID carerId) {
        String key = AVAILABLE_SHIFTS_PREFIX + carerId.toString();
        String jsonValue = redisTemplate.opsForValue().get(key);
        
        if (jsonValue == null) {
            return List.of(); // Return empty list if not found
        }
        
        try {
            return objectMapper.readValue(jsonValue, new TypeReference<List<EligibleShiftDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize available shifts for carer: " + carerId, e);
        }
    }

    /**
     * Retrieves eligible carers for a shift
     */
    public List<EligibleCarerDto> getEligibleCarersForShift(UUID bookingId) {
        String key = ELIGIBLE_CARERS_PREFIX + bookingId.toString();
        String jsonValue = redisTemplate.opsForValue().get(key);
        
        if (jsonValue == null) {
            return List.of(); // Return empty list if not found
        }
        
        try {
            return objectMapper.readValue(jsonValue, new TypeReference<List<EligibleCarerDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize eligible carers for shift: " + bookingId, e);
        }
    }

    /**
     * Stores carer data for eligibility calculations
     */
    public void storeCarerData(UUID carerId, EligibilityRulesEngine.CarerProjection carerData) {
        String key = CARER_DATA_PREFIX + carerId.toString();
        try {
            String jsonValue = objectMapper.writeValueAsString(carerData);
            redisTemplate.opsForValue().set(key, jsonValue, PROJECTION_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize carer data: " + carerId, e);
        }
    }

    /**
     * Retrieves carer data for eligibility calculations
     */
    public EligibilityRulesEngine.CarerProjection getCarerData(UUID carerId) {
        String key = CARER_DATA_PREFIX + carerId.toString();
        String jsonValue = redisTemplate.opsForValue().get(key);
        
        if (jsonValue == null) {
            return null;
        }
        
        try {
            return objectMapper.readValue(jsonValue, EligibilityRulesEngine.CarerProjection.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize carer data: " + carerId, e);
        }
    }

    /**
     * Stores booking data for eligibility calculations
     */
    public void storeBookingData(UUID bookingId, EligibilityRulesEngine.BookingProjection bookingData) {
        String key = BOOKING_DATA_PREFIX + bookingId.toString();
        try {
            String jsonValue = objectMapper.writeValueAsString(bookingData);
            redisTemplate.opsForValue().set(key, jsonValue, PROJECTION_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize booking data: " + bookingId, e);
        }
    }

    /**
     * Retrieves booking data for eligibility calculations
     */
    public EligibilityRulesEngine.BookingProjection getBookingData(UUID bookingId) {
        String key = BOOKING_DATA_PREFIX + bookingId.toString();
        String jsonValue = redisTemplate.opsForValue().get(key);
        
        if (jsonValue == null) {
            return null;
        }
        
        try {
            return objectMapper.readValue(jsonValue, EligibilityRulesEngine.BookingProjection.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize booking data: " + bookingId, e);
        }
    }

    /**
     * Removes all projections for a specific carer (when carer is deleted)
     */
    public void removeCarerProjections(UUID carerId) {
        String availableShiftsKey = AVAILABLE_SHIFTS_PREFIX + carerId.toString();
        String carerDataKey = CARER_DATA_PREFIX + carerId.toString();
        
        redisTemplate.delete(availableShiftsKey);
        redisTemplate.delete(carerDataKey);
        
        // Also need to remove this carer from all shift eligibility lists
        removeCarerFromAllShiftProjections(carerId);
    }

    /**
     * Removes all projections for a specific booking (when booking is cancelled)
     */
    public void removeBookingProjections(UUID bookingId) {
        String eligibleCarersKey = ELIGIBLE_CARERS_PREFIX + bookingId.toString();
        String bookingDataKey = BOOKING_DATA_PREFIX + bookingId.toString();
        
        redisTemplate.delete(eligibleCarersKey);
        redisTemplate.delete(bookingDataKey);
        
        // Also need to remove this booking from all carer availability lists
        removeBookingFromAllCarerProjections(bookingId);
    }

    /**
     * Gets all carer IDs that have projections
     */
    public Set<String> getAllCarerIds() {
        return redisTemplate.keys(CARER_DATA_PREFIX + "*")
            .stream()
            .map(key -> key.substring(CARER_DATA_PREFIX.length()))
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Gets all booking IDs that have projections
     */
    public Set<String> getAllBookingIds() {
        return redisTemplate.keys(BOOKING_DATA_PREFIX + "*")
            .stream()
            .map(key -> key.substring(BOOKING_DATA_PREFIX.length()))
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Private helper method to remove a carer from all shift eligibility lists
     */
    private void removeCarerFromAllShiftProjections(UUID carerId) {
        Set<String> shiftKeys = redisTemplate.keys(ELIGIBLE_CARERS_PREFIX + "*");
        
        for (String key : shiftKeys) {
            List<EligibleCarerDto> eligibleCarers = getEligibleCarersForShift(
                UUID.fromString(key.substring(ELIGIBLE_CARERS_PREFIX.length())));
            
            eligibleCarers.removeIf(carer -> carer.getCarerId().equals(carerId));
            
            UUID bookingId = UUID.fromString(key.substring(ELIGIBLE_CARERS_PREFIX.length()));
            updateEligibleCarersForShift(bookingId, eligibleCarers);
        }
    }

    /**
     * Private helper method to remove a booking from all carer availability lists
     */
    private void removeBookingFromAllCarerProjections(UUID bookingId) {
        Set<String> carerKeys = redisTemplate.keys(AVAILABLE_SHIFTS_PREFIX + "*");
        
        for (String key : carerKeys) {
            List<EligibleShiftDto> availableShifts = getAvailableShiftsForCarer(
                UUID.fromString(key.substring(AVAILABLE_SHIFTS_PREFIX.length())));
            
            availableShifts.removeIf(shift -> shift.getBookingId().equals(bookingId));
            
            UUID carerId = UUID.fromString(key.substring(AVAILABLE_SHIFTS_PREFIX.length()));
            updateAvailableShiftsForCarer(carerId, availableShifts);
        }
    }
}
