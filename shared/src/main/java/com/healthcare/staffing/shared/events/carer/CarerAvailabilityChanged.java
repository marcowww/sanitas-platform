package com.healthcare.staffing.shared.events.carer;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class CarerAvailabilityChanged extends CarerEvent {
    @NotNull
    private List<AvailabilitySlot> availabilitySlots;

    // Default constructor for Jackson
    public CarerAvailabilityChanged() {
        super();
    }

    public CarerAvailabilityChanged(UUID carerId, List<AvailabilitySlot> availabilitySlots) {
        super(carerId, "CarerAvailabilityChanged");
        this.availabilitySlots = availabilitySlots;
    }

    public List<AvailabilitySlot> getAvailabilitySlots() { return availabilitySlots; }
    
    // Setter for Jackson
    public void setAvailabilitySlots(List<AvailabilitySlot> availabilitySlots) { this.availabilitySlots = availabilitySlots; }

    public static class AvailabilitySlot {
        @NotNull
        private LocalDate date;
        
        @NotNull
        private LocalTime startTime;
        
        @NotNull
        private LocalTime endTime;
        
        @NotNull
        private boolean available;

        // Default constructor for Jackson
        public AvailabilitySlot() {}

        public AvailabilitySlot(LocalDate date, LocalTime startTime, LocalTime endTime, boolean available) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.available = available;
        }

        public LocalDate getDate() { return date; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public boolean isAvailable() { return available; }
        
        // Setters for Jackson
        public void setDate(LocalDate date) { this.date = date; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
        public void setAvailable(boolean available) { this.available = available; }
    }
}
