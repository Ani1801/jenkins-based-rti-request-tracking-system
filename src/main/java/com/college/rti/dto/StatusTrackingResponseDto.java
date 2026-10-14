package com.college.rti.dto;

import com.college.rti.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Public response DTO for RTI status tracking queries.
 * Strictly omits applicant name, email, phone number, and internal database IDs.
 */
public class StatusTrackingResponseDto {

    private String trackingId;
    private String category;
    private RequestStatus status;
    private LocalDateTime submittedAt;
    private List<PublicStatusHistoryDto> history = new ArrayList<>();

    public StatusTrackingResponseDto() {
    }

    public StatusTrackingResponseDto(String trackingId, String category, RequestStatus status,
                                     LocalDateTime submittedAt, List<PublicStatusHistoryDto> history) {
        this.trackingId = trackingId;
        this.category = category;
        this.status = status;
        this.submittedAt = submittedAt;
        this.history = history != null ? history : new ArrayList<>();
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<PublicStatusHistoryDto> getHistory() {
        return history;
    }

    public void setHistory(List<PublicStatusHistoryDto> history) {
        this.history = history != null ? history : new ArrayList<>();
    }
}
