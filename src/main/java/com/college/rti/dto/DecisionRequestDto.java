package com.college.rti.dto;

import com.college.rti.model.RequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object representing an administrative reviewer decision request.
 */
public class DecisionRequestDto {

    @NotNull(message = "Target status is required")
    private RequestStatus targetStatus;

    @NotBlank(message = "Reviewer username is required")
    private String reviewerUsername;

    @NotBlank(message = "Decision remarks are required")
    private String remarks;

    public DecisionRequestDto() {
    }

    public DecisionRequestDto(RequestStatus targetStatus, String reviewerUsername, String remarks) {
        this.targetStatus = targetStatus;
        this.reviewerUsername = reviewerUsername;
        this.remarks = remarks;
    }

    public RequestStatus getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(RequestStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    /**
     * Helper to accept string decision payloads matching API documentation.
     */
    public void setDecision(String decision) {
        if (decision != null && !decision.trim().isEmpty()) {
            this.targetStatus = RequestStatus.valueOf(decision.trim().toUpperCase());
        }
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public void setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
