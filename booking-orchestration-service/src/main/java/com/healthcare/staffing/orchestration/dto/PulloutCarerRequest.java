package com.healthcare.staffing.orchestration.dto;

import java.util.UUID;

public class PulloutCarerRequest {
    private UUID carerId;
    private String pulloutReason;
    private String pulloutBy;

    public PulloutCarerRequest() {}

    public PulloutCarerRequest(UUID carerId, String pulloutReason, String pulloutBy) {
        this.carerId = carerId;
        this.pulloutReason = pulloutReason;
        this.pulloutBy = pulloutBy;
    }

    public UUID getCarerId() { return carerId; }
    public void setCarerId(UUID carerId) { this.carerId = carerId; }

    public String getPulloutReason() { return pulloutReason; }
    public void setPulloutReason(String pulloutReason) { this.pulloutReason = pulloutReason; }

    public String getPulloutBy() { return pulloutBy; }
    public void setPulloutBy(String pulloutBy) { this.pulloutBy = pulloutBy; }
}
