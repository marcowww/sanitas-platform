package com.healthcare.staffing.shared.events.carer;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public class CarerUpdated extends CarerEvent {
    @NotNull
    private Map<String, Object> changedFields;
    
    @NotNull
    private String updateReason;

    // Default constructor for Jackson
    public CarerUpdated() {
        super();
    }

    public CarerUpdated(UUID carerId, Map<String, Object> changedFields, String updateReason) {
        super(carerId, "CarerUpdated");
        this.changedFields = changedFields;
        this.updateReason = updateReason;
    }

    public Map<String, Object> getChangedFields() { return changedFields; }
    public String getUpdateReason() { return updateReason; }
    
    // Setters for Jackson
    public void setChangedFields(Map<String, Object> changedFields) { this.changedFields = changedFields; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
}
