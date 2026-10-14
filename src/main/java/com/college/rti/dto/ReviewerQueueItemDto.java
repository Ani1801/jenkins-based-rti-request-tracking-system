package com.college.rti.dto;

import com.college.rti.model.RequestStatus;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an RTI Request item in the reviewer queue.
 * Contains operational applicant and request information needed by an authorized reviewer.
 */
public class ReviewerQueueItemDto {

    private Long id;
    private String trackingId;
    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;
    private String category;
    private String description;
    private String attachmentPath;
    private String attachmentOriginalName;
    private RequestStatus currentStatus;
    private LocalDateTime createdAt;

    public ReviewerQueueItemDto() {
    }

    public ReviewerQueueItemDto(Long id, String trackingId, String applicantName, String applicantEmail,
                                String applicantPhone, String category, String description,
                                String attachmentPath, String attachmentOriginalName,
                                RequestStatus currentStatus, LocalDateTime createdAt) {
        this.id = id;
        this.trackingId = trackingId;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.applicantPhone = applicantPhone;
        this.category = category;
        this.description = description;
        this.attachmentPath = attachmentPath;
        this.attachmentOriginalName = attachmentOriginalName;
        this.currentStatus = currentStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }

    public String getApplicantPhone() {
        return applicantPhone;
    }

    public void setApplicantPhone(String applicantPhone) {
        this.applicantPhone = applicantPhone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getAttachmentOriginalName() {
        return attachmentOriginalName;
    }

    public void setAttachmentOriginalName(String attachmentOriginalName) {
        this.attachmentOriginalName = attachmentOriginalName;
    }

    public RequestStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(RequestStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
