package com.college.rti.dto;

import com.college.rti.model.RequestStatus;

import java.time.LocalDateTime;

/**
 * Public response DTO returned upon successful submission or status query.
 * Excludes sensitive contact information (email, phone, address).
 */
public class SubmissionResponseDto {

    private boolean success;
    private String trackingId;
    private RequestStatus status;
    private String category;
    private LocalDateTime submittedAt;
    private String message;

    public SubmissionResponseDto() {
    }

    public SubmissionResponseDto(boolean success, String trackingId, RequestStatus status,
                                 String category, LocalDateTime submittedAt, String message) {
        this.success = success;
        this.trackingId = trackingId;
        this.status = status;
        this.category = category;
        this.submittedAt = submittedAt;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
