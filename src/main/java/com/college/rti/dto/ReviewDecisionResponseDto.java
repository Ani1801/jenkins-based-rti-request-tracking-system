package com.college.rti.dto;

import com.college.rti.model.RequestStatus;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the result of a reviewer state-machine transition.
 */
public class ReviewDecisionResponseDto {

    private boolean success;
    private Long requestId;
    private String trackingId;
    private RequestStatus previousStatus;
    private RequestStatus newStatus;
    private String reviewerUsername;
    private LocalDateTime updatedAt;
    private String message;

    public ReviewDecisionResponseDto() {
    }

    public ReviewDecisionResponseDto(boolean success, Long requestId, String trackingId,
                                    RequestStatus previousStatus, RequestStatus newStatus,
                                    String reviewerUsername, LocalDateTime updatedAt, String message) {
        this.success = success;
        this.requestId = requestId;
        this.trackingId = trackingId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reviewerUsername = reviewerUsername;
        this.updatedAt = updatedAt;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public RequestStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(RequestStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public RequestStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(RequestStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public void setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
