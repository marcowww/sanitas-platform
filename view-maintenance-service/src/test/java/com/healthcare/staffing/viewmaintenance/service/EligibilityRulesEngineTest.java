package com.healthcare.staffing.viewmaintenance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EligibilityRulesEngineTest {

    private EligibilityRulesEngine eligibilityRulesEngine;

    @BeforeEach
    void setUp() {
        eligibilityRulesEngine = new EligibilityRulesEngine();
    }

    @Test
    void isCarerEligibleForBooking_SameGradeAndQualifications_ShouldReturnTrue() {
        // Arrange
        EligibilityRulesEngine.CarerProjection carer = new EligibilityRulesEngine.CarerProjection(
                UUID.randomUUID(), "RN", List.of("BLS", "ACLS"), "London", "CITIZEN", 50
        );

        EligibilityRulesEngine.BookingProjection booking = new EligibilityRulesEngine.BookingProjection(
                UUID.randomUUID(), UUID.randomUUID(), "RN", List.of("BLS"), "London",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(8)
        );

        // Act
        boolean result = eligibilityRulesEngine.isCarerEligibleForBooking(carer, booking);

        // Assert
        assertTrue(result);
    }

    @Test
    void isCarerEligibleForBooking_DifferentGrade_ShouldReturnFalse() {
        // Arrange
        EligibilityRulesEngine.CarerProjection carer = new EligibilityRulesEngine.CarerProjection(
                UUID.randomUUID(), "HCA", List.of("BLS"), "London", "CITIZEN", 50
        );

        EligibilityRulesEngine.BookingProjection booking = new EligibilityRulesEngine.BookingProjection(
                UUID.randomUUID(), UUID.randomUUID(), "RN", List.of("BLS"), "London",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(8)
        );

        // Act
        boolean result = eligibilityRulesEngine.isCarerEligibleForBooking(carer, booking);

        // Assert
        assertFalse(result);
    }

    @Test
    void isCarerEligibleForBooking_MissingRequiredQualification_ShouldReturnFalse() {
        // Arrange
        EligibilityRulesEngine.CarerProjection carer = new EligibilityRulesEngine.CarerProjection(
                UUID.randomUUID(), "RN", List.of("BLS"), "London", "CITIZEN", 50
        );

        EligibilityRulesEngine.BookingProjection booking = new EligibilityRulesEngine.BookingProjection(
                UUID.randomUUID(), UUID.randomUUID(), "RN", List.of("BLS", "ACLS"), "London",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(8)
        );

        // Act
        boolean result = eligibilityRulesEngine.isCarerEligibleForBooking(carer, booking);

        // Assert
        assertFalse(result);
    }
}
