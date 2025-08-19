package com.healthcare.staffing.booking.service;

import com.healthcare.staffing.booking.domain.Booking;
import com.healthcare.staffing.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository, kafkaTemplate);
    }

    @Test
    void createBooking_ShouldCreateBookingAndEmitEvent() {
        // Arrange
        UUID facilityId = UUID.randomUUID();
        String shift = "DAY";
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(8);
        String grade = "RN";
        BigDecimal hourlyRate = new BigDecimal("35.00");
        String location = "London";
        String specialRequirements = "ICU experience required";
        List<String> requiredQualifications = List.of("BLS", "ACLS");

        Booking savedBooking = new Booking(facilityId, shift, startTime, endTime, 
                                          grade, hourlyRate, location, specialRequirements, requiredQualifications);
        
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // Act
        Booking result = bookingService.createBooking(facilityId, shift, startTime, endTime,
                                                     grade, hourlyRate, location, specialRequirements, requiredQualifications);

        // Assert
        assertNotNull(result);
        assertEquals(facilityId, result.getFacilityId());
        assertEquals(shift, result.getShift());
        assertEquals(grade, result.getGrade());
        assertEquals(Booking.BookingStatus.OPEN, result.getStatus());

        verify(bookingRepository).save(any(Booking.class));
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }

    @Test
    void cancelBooking_ShouldUpdateStatusAndEmitEvent() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setStatus(Booking.BookingStatus.OPEN);

        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.of(existingBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(existingBooking);

        // Act
        bookingService.cancelBooking(bookingId, "No longer needed", "admin");

        // Assert
        assertEquals(Booking.BookingStatus.CANCELLED, existingBooking.getStatus());
        verify(bookingRepository).save(existingBooking);
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }
}
