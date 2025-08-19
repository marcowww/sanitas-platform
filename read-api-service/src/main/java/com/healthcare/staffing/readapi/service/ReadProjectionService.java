package com.healthcare.staffing.readapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.staffing.shared.dto.EligibleCarerDto;
import com.healthcare.staffing.shared.dto.EligibleShiftDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReadProjectionService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Redis key prefixes - must match those in view-maintenance-service
    private static final String AVAILABLE_SHIFTS_PREFIX = "AvailableShiftsPerCarer:";
    private static final String ELIGIBLE_CARERS_PREFIX = "EligibleCarersPerShift:";

    @Autowired
    public ReadProjectionService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves eligible shifts for a specific carer
     */
    public List<EligibleShiftDto> getEligibleShiftsForCarer(UUID carerId) {
        String key = AVAILABLE_SHIFTS_PREFIX + carerId.toString();
        String jsonValue = redisTemplate.opsForValue().get(key);
        
        if (jsonValue == null) {
            return List.of(); // Return empty list if not found
        }
        
        try {
            return objectMapper.readValue(jsonValue, new TypeReference<List<EligibleShiftDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize eligible shifts for carer: " + carerId, e);
        }
    }

    /**
     * Retrieves eligible carers for a specific shift
     */
    public List<EligibleCarerDto> getEligibleCarersForShift(UUID shiftId) {
        String key = ELIGIBLE_CARERS_PREFIX + shiftId.toString();
        String jsonValue = redisTemplate.opsForValue().get(key);
        
        if (jsonValue == null) {
            return List.of(); // Return empty list if not found
        }
        
        try {
            return objectMapper.readValue(jsonValue, new TypeReference<List<EligibleCarerDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize eligible carers for shift: " + shiftId, e);
        }
    }

    /**
     * Checks if a specific carer-shift combination exists in projections
     */
    public boolean isCarerEligibleForShift(UUID carerId, UUID shiftId) {
        List<EligibleShiftDto> eligibleShifts = getEligibleShiftsForCarer(carerId);
        return eligibleShifts.stream().anyMatch(shift -> shift.getBookingId().equals(shiftId));
    }

    /**
     * Gets the count of eligible shifts for a carer
     */
    public long getEligibleShiftsCount(UUID carerId) {
        return getEligibleShiftsForCarer(carerId).size();
    }

    /**
     * Gets the count of eligible carers for a shift
     */
    public long getEligibleCarersCount(UUID shiftId) {
        return getEligibleCarersForShift(shiftId).size();
    }

    /**
     * Filters eligible shifts by location
     */
    public List<EligibleShiftDto> getEligibleShiftsForCarerByLocation(UUID carerId, String location) {
        List<EligibleShiftDto> allShifts = getEligibleShiftsForCarer(carerId);
        return allShifts.stream()
            .filter(shift -> location.equals(shift.getLocation()))
            .toList();
    }

    /**
     * Filters eligible carers by grade
     */
    public List<EligibleCarerDto> getEligibleCarersForShiftByGrade(UUID shiftId, String grade) {
        List<EligibleCarerDto> allCarers = getEligibleCarersForShift(shiftId);
        return allCarers.stream()
            .filter(carer -> grade.equals(carer.getGrade()))
            .toList();
    }

    /**
     * Filters eligible shifts by maximum distance
     */
    public List<EligibleShiftDto> getEligibleShiftsForCarerWithinDistance(UUID carerId, double maxDistanceKm) {
        List<EligibleShiftDto> allShifts = getEligibleShiftsForCarer(carerId);
        return allShifts.stream()
            .filter(shift -> shift.getDistanceKm() != null && shift.getDistanceKm() <= maxDistanceKm)
            .toList();
    }

    /**
     * Filters eligible carers by maximum distance
     */
    public List<EligibleCarerDto> getEligibleCarersForShiftWithinDistance(UUID shiftId, double maxDistanceKm) {
        List<EligibleCarerDto> allCarers = getEligibleCarersForShift(shiftId);
        return allCarers.stream()
            .filter(carer -> carer.getDistanceKm() != null && carer.getDistanceKm() <= maxDistanceKm)
            .toList();
    }

    /**
     * Sorts eligible shifts by distance (ascending)
     */
    public List<EligibleShiftDto> getEligibleShiftsForCarerSortedByDistance(UUID carerId) {
        List<EligibleShiftDto> shifts = getEligibleShiftsForCarer(carerId);
        return shifts.stream()
            .filter(shift -> shift.getDistanceKm() != null)
            .sorted((s1, s2) -> Double.compare(s1.getDistanceKm(), s2.getDistanceKm()))
            .toList();
    }

    /**
     * Sorts eligible carers by distance (ascending)
     */
    public List<EligibleCarerDto> getEligibleCarersForShiftSortedByDistance(UUID shiftId) {
        List<EligibleCarerDto> carers = getEligibleCarersForShift(shiftId);
        return carers.stream()
            .filter(carer -> carer.getDistanceKm() != null)
            .sorted((c1, c2) -> Double.compare(c1.getDistanceKm(), c2.getDistanceKm()))
            .toList();
    }
}
