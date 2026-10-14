package com.college.rti.dto;

import com.college.rti.model.RequestStatus;

import java.time.LocalDateTime;

/**
 * Public projection of a status transition event in the audit timeline.
 * Excludes internal reviewer usernames, employee IDs, and internal database primary keys.
 */
public class PublicStatusHistoryDto {

    private RequestStatus fromStatus;
    private RequestStatus toStatus;
    private LocalDateTime timestamp;
    private String remarks;

    public PublicStatusHistoryDto() {
    }

    public PublicStatusHistoryDto(RequestStatus fromStatus, RequestStatus toStatus,
                                  LocalDateTime timestamp, String remarks) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.timestamp = timestamp;
        this.remarks = remarks;
    }

    public RequestStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(RequestStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public RequestStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(RequestStatus toStatus) {
        this.toStatus = toStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
