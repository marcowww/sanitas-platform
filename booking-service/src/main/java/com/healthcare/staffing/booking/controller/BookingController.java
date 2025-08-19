package com.healthcare.staffing.booking.controller;

import com.healthcare.staffing.booking.domain.Booking;
import com.healthcare.staffing.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.createBooking(
            request.getFacilityId(),
            request.getShift(),
            request.getStartTime(),
            request.getEndTime(),
            request.getGrade(),
            request.getHourlyRate(),
            request.getLocation(),
            request.getSpecialRequirements(),
            request.getRequiredQualifications()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<Booking> updateBooking(@PathVariable UUID bookingId,
                                               @Valid @RequestBody UpdateBookingRequest request) {
        Booking booking = bookingService.updateBooking(bookingId, request.getUpdates(), request.getModificationReason());
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable UUID bookingId,
                                            @Valid @RequestBody CancelBookingRequest request) {
        bookingService.cancelBooking(bookingId, request.getCancellationReason(), request.getCancelledBy());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookingId}/book")
    public ResponseEntity<Void> bookCarer(@PathVariable UUID bookingId,
                                        @Valid @RequestBody BookCarerRequest request) {
        bookingService.bookCarer(bookingId, request.getCarerId(), request.getBookedBy());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookingId}/pullout")
    public ResponseEntity<Void> pulloutCarer(@PathVariable UUID bookingId,
                                           @Valid @RequestBody PulloutCarerRequest request) {
        bookingService.pulloutCarer(bookingId, request.getCarerId(), request.getPulloutReason(), request.getPulloutBy());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getBooking(@PathVariable UUID bookingId) {
        Booking booking = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // Request DTOs
    public static class CreateBookingRequest {
        private UUID facilityId;
        private String shift;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String grade;
        private BigDecimal hourlyRate;
        private String location;
        private String specialRequirements;
        private List<String> requiredQualifications;

        // Getters and Setters
        public UUID getFacilityId() { return facilityId; }
        public void setFacilityId(UUID facilityId) { this.facilityId = facilityId; }

        public String getShift() { return shift; }
        public void setShift(String shift) { this.shift = shift; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }

        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getSpecialRequirements() { return specialRequirements; }
        public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }

        public List<String> getRequiredQualifications() { return requiredQualifications; }
        public void setRequiredQualifications(List<String> requiredQualifications) { this.requiredQualifications = requiredQualifications; }
    }

    public static class UpdateBookingRequest {
        private Map<String, Object> updates;
        private String modificationReason;

        public Map<String, Object> getUpdates() { return updates; }
        public void setUpdates(Map<String, Object> updates) { this.updates = updates; }

        public String getModificationReason() { return modificationReason; }
        public void setModificationReason(String modificationReason) { this.modificationReason = modificationReason; }
    }

    public static class CancelBookingRequest {
        private String cancellationReason;
        private String cancelledBy;

        public String getCancellationReason() { return cancellationReason; }
        public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

        public String getCancelledBy() { return cancelledBy; }
        public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }
    }

    public static class BookCarerRequest {
        private UUID carerId;
        private String bookedBy;

        public UUID getCarerId() { return carerId; }
        public void setCarerId(UUID carerId) { this.carerId = carerId; }

        public String getBookedBy() { return bookedBy; }
        public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }
    }

    public static class PulloutCarerRequest {
        private UUID carerId;
        private String pulloutReason;
        private String pulloutBy;

        public UUID getCarerId() { return carerId; }
        public void setCarerId(UUID carerId) { this.carerId = carerId; }

        public String getPulloutReason() { return pulloutReason; }
        public void setPulloutReason(String pulloutReason) { this.pulloutReason = pulloutReason; }

        public String getPulloutBy() { return pulloutBy; }
        public void setPulloutBy(String pulloutBy) { this.pulloutBy = pulloutBy; }
    }
}
