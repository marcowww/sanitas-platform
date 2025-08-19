package com.healthcare.staffing.shared.events.booking;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class BookingModified extends BookingEvent {
    @NotNull
    private Map<String, Object> changedFields;
    
    @NotNull
    private String modificationReason;

    // Default constructor for Jackson
    public BookingModified() {
        super();
    }

    public BookingModified(UUID bookingId, Map<String, Object> changedFields, String modificationReason) {
        super(bookingId, "BookingModified");
        this.changedFields = changedFields;
        this.modificationReason = modificationReason;
    }

    public Map<String, Object> getChangedFields() { return changedFields; }
    public String getModificationReason() { return modificationReason; }
    
    // Setters for Jackson
    public void setChangedFields(Map<String, Object> changedFields) { this.changedFields = changedFields; }
    public void setModificationReason(String modificationReason) { this.modificationReason = modificationReason; }
}
