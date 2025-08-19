package com.healthcare.staffing.booking.service;

import com.healthcare.staffing.booking.domain.Booking;
import com.healthcare.staffing.booking.repository.BookingRepository;
import com.healthcare.staffing.shared.events.booking.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String BOOKING_EVENTS_TOPIC = "booking-events";

    @Autowired
    public BookingService(BookingRepository bookingRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.bookingRepository = bookingRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Booking createBooking(UUID facilityId, String shift, LocalDateTime startTime, 
                               LocalDateTime endTime, String grade, BigDecimal hourlyRate, 
                               String location, String specialRequirements, 
                               List<String> requiredQualifications) {
        
        Booking booking = new Booking(facilityId, shift, startTime, endTime, grade, 
                                     hourlyRate, location, specialRequirements, requiredQualifications);
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Emit BookingCreated event
        BookingCreated event = new BookingCreated(
            savedBooking.getId(), facilityId, shift, startTime, endTime,
            grade, hourlyRate, location, specialRequirements, requiredQualifications
        );
        
        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId().toString(), event);
        
        return savedBooking;
    }

    public Booking updateBooking(UUID bookingId, Map<String, Object> updates, String modificationReason) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        
        Map<String, Object> changedFields = new HashMap<>();
        
        // Apply updates
        if (updates.containsKey("shift")) {
            String newShift = (String) updates.get("shift");
            if (!newShift.equals(booking.getShift())) {
                changedFields.put("shift", Map.of("old", booking.getShift(), "new", newShift));
                booking.setShift(newShift);
            }
        }
        
        if (updates.containsKey("hourlyRate")) {
            BigDecimal newRate = (BigDecimal) updates.get("hourlyRate");
            if (!newRate.equals(booking.getHourlyRate())) {
                changedFields.put("hourlyRate", Map.of("old", booking.getHourlyRate(), "new", newRate));
                booking.setHourlyRate(newRate);
            }
        }
        
        if (updates.containsKey("specialRequirements")) {
            String newRequirements = (String) updates.get("specialRequirements");
            if (!newRequirements.equals(booking.getSpecialRequirements())) {
                changedFields.put("specialRequirements", 
                    Map.of("old", booking.getSpecialRequirements(), "new", newRequirements));
                booking.setSpecialRequirements(newRequirements);
            }
        }
        
        if (updates.containsKey("location")) {
            String newLocation = (String) updates.get("location");
            if (!newLocation.equals(booking.getLocation())) {
                changedFields.put("location", 
                    Map.of("old", booking.getLocation(), "new", newLocation));
                booking.setLocation(newLocation);
            }
        }
        
        if (updates.containsKey("grade")) {
            String newGrade = (String) updates.get("grade");
            if (!newGrade.equals(booking.getGrade())) {
                changedFields.put("grade", 
                    Map.of("old", booking.getGrade(), "new", newGrade));
                booking.setGrade(newGrade);
            }
        }
        
        if (updates.containsKey("requiredQualifications")) {
            @SuppressWarnings("unchecked")
            List<String> newQualifications = (List<String>) updates.get("requiredQualifications");
            if (!newQualifications.equals(booking.getRequiredQualifications())) {
                changedFields.put("requiredQualifications", 
                    Map.of("old", booking.getRequiredQualifications(), "new", newQualifications));
                booking.setRequiredQualifications(newQualifications);
            }
        }
        
        if (!changedFields.isEmpty()) {
            Booking savedBooking = bookingRepository.save(booking);
            
            // Emit BookingModified event
            BookingModified event = new BookingModified(bookingId, changedFields, modificationReason);
            kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId().toString(), event);
            
            return savedBooking;
        }
        
        return booking;
    }

    public void cancelBooking(UUID bookingId, String cancellationReason, String cancelledBy) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        // Emit BookingCancelled event
        BookingCancelled event = new BookingCancelled(bookingId, cancellationReason, cancelledBy);
        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId().toString(), event);
    }

    public void bookCarer(UUID bookingId, UUID carerId, String bookedBy) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        
        if (booking.getStatus() != Booking.BookingStatus.OPEN) {
            throw new RuntimeException("Booking is not available for assignment");
        }
        
        booking.setAssignedCarerId(carerId);
        booking.setStatus(Booking.BookingStatus.BOOKED);
        bookingRepository.save(booking);
        
        // Emit BookingBooked event
        BookingBooked event = new BookingBooked(bookingId, carerId, bookedBy);
        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId().toString(), event);
    }

    public void pulloutCarer(UUID bookingId, UUID carerId, String pulloutReason, String pulloutBy) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        
        if (!carerId.equals(booking.getAssignedCarerId())) {
            throw new RuntimeException("Carer is not assigned to this booking");
        }
        
        booking.setAssignedCarerId(null);
        booking.setStatus(Booking.BookingStatus.OPEN);
        bookingRepository.save(booking);
        
        // Emit BookingPullout event
        BookingPullout event = new BookingPullout(bookingId, carerId, pulloutReason, pulloutBy);
        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId().toString(), event);
    }

    public Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}
