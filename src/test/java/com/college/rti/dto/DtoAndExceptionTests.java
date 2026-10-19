package com.college.rti.dto;

import com.college.rti.exception.InvalidStateTransitionException;
import com.college.rti.exception.RequestNotFoundException;
import com.college.rti.model.RequestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoAndExceptionTests {

    @Test
    @DisplayName("RequestSubmissionDto: should support no-arg, full constructor, getters and setters")
    void testRequestSubmissionDto() {
        RequestSubmissionDto dto = new RequestSubmissionDto();
        dto.setApplicantName("Jane");
        dto.setApplicantEmail("jane@example.com");
        dto.setApplicantPhone("9876543210");
        dto.setCategory("ACADEMIC");
        dto.setDescription("Description text");

        assertThat(dto.getApplicantName()).isEqualTo("Jane");
        assertThat(dto.getApplicantEmail()).isEqualTo("jane@example.com");
        assertThat(dto.getApplicantPhone()).isEqualTo("9876543210");
        assertThat(dto.getCategory()).isEqualTo("ACADEMIC");
        assertThat(dto.getDescription()).isEqualTo("Description text");

        RequestSubmissionDto full = new RequestSubmissionDto("John", "john@example.com", "9123456789", "FINANCIAL", "Desc");
        assertThat(full.getApplicantName()).isEqualTo("John");
    }

    @Test
    @DisplayName("SubmissionResponseDto: should support no-arg, full constructor, getters and setters")
    void testSubmissionResponseDto() {
        LocalDateTime now = LocalDateTime.now();
        SubmissionResponseDto dto = new SubmissionResponseDto();
        dto.setSuccess(true);
        dto.setTrackingId("RTI-2026-000001");
        dto.setStatus(RequestStatus.SUBMITTED);
        dto.setCategory("ACADEMIC");
        dto.setSubmittedAt(now);
        dto.setMessage("Submitted successfully");

        assertThat(dto.isSuccess()).isTrue();
        assertThat(dto.getTrackingId()).isEqualTo("RTI-2026-000001");
        assertThat(dto.getStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(dto.getCategory()).isEqualTo("ACADEMIC");
        assertThat(dto.getSubmittedAt()).isEqualTo(now);
        assertThat(dto.getMessage()).isEqualTo("Submitted successfully");

        SubmissionResponseDto full = new SubmissionResponseDto(false, "RTI-2026-000002", RequestStatus.REJECTED, "FINANCIAL", now, "Error");
        assertThat(full.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("PublicStatusHistoryDto: should support getters and setters")
    void testPublicStatusHistoryDto() {
        LocalDateTime now = LocalDateTime.now();
        PublicStatusHistoryDto dto = new PublicStatusHistoryDto();
        dto.setFromStatus(RequestStatus.SUBMITTED);
        dto.setToStatus(RequestStatus.UNDER_REVIEW);
        dto.setTimestamp(now);
        dto.setRemarks("Moved to review");

        assertThat(dto.getFromStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(dto.getToStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(dto.getTimestamp()).isEqualTo(now);
        assertThat(dto.getRemarks()).isEqualTo("Moved to review");

        PublicStatusHistoryDto full = new PublicStatusHistoryDto(RequestStatus.UNDER_REVIEW, RequestStatus.APPROVED, now, "Approved");
        assertThat(full.getToStatus()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    @DisplayName("StatusTrackingResponseDto: should support getters and setters")
    void testStatusTrackingResponseDto() {
        LocalDateTime now = LocalDateTime.now();
        StatusTrackingResponseDto dto = new StatusTrackingResponseDto();
        dto.setTrackingId("RTI-2026-000001");
        dto.setCategory("ACADEMIC");
        dto.setStatus(RequestStatus.APPROVED);
        dto.setSubmittedAt(now);
        dto.setHistory(List.of());

        assertThat(dto.getTrackingId()).isEqualTo("RTI-2026-000001");
        assertThat(dto.getCategory()).isEqualTo("ACADEMIC");
        assertThat(dto.getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(dto.getSubmittedAt()).isEqualTo(now);
        assertThat(dto.getHistory()).isEmpty();

        StatusTrackingResponseDto full = new StatusTrackingResponseDto("RTI-2026-000002", "FINANCIAL", RequestStatus.SUBMITTED, now, List.of());
        assertThat(full.getTrackingId()).isEqualTo("RTI-2026-000002");
    }

    @Test
    @DisplayName("ReviewerQueueItemDto: should support getters and setters")
    void testReviewerQueueItemDto() {
        LocalDateTime now = LocalDateTime.now();
        ReviewerQueueItemDto dto = new ReviewerQueueItemDto();
        dto.setId(10L);
        dto.setTrackingId("RTI-2026-000001");
        dto.setApplicantName("Alice");
        dto.setApplicantEmail("alice@example.com");
        dto.setApplicantPhone("9876543210");
        dto.setCategory("ACADEMIC");
        dto.setDescription("Description");
        dto.setAttachmentPath("/path/file.pdf");
        dto.setAttachmentOriginalName("file.pdf");
        dto.setCurrentStatus(RequestStatus.SUBMITTED);
        dto.setCreatedAt(now);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTrackingId()).isEqualTo("RTI-2026-000001");
        assertThat(dto.getApplicantName()).isEqualTo("Alice");
        assertThat(dto.getApplicantEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getApplicantPhone()).isEqualTo("9876543210");
        assertThat(dto.getCategory()).isEqualTo("ACADEMIC");
        assertThat(dto.getDescription()).isEqualTo("Description");
        assertThat(dto.getAttachmentPath()).isEqualTo("/path/file.pdf");
        assertThat(dto.getAttachmentOriginalName()).isEqualTo("file.pdf");
        assertThat(dto.getCurrentStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(dto.getCreatedAt()).isEqualTo(now);

        ReviewerQueueItemDto full = new ReviewerQueueItemDto(20L, "RTI-2026-000002", "Bob", "bob@example.com", "9123456780", "FIN", "Desc", null, null, RequestStatus.UNDER_REVIEW, now);
        assertThat(full.getId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("ReviewDecisionResponseDto: should support getters and setters")
    void testReviewDecisionResponseDto() {
        LocalDateTime now = LocalDateTime.now();
        ReviewDecisionResponseDto dto = new ReviewDecisionResponseDto();
        dto.setSuccess(true);
        dto.setRequestId(1L);
        dto.setTrackingId("RTI-2026-000001");
        dto.setPreviousStatus(RequestStatus.SUBMITTED);
        dto.setNewStatus(RequestStatus.UNDER_REVIEW);
        dto.setReviewerUsername("officer1");
        dto.setUpdatedAt(now);
        dto.setMessage("Transition complete");

        assertThat(dto.isSuccess()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(1L);
        assertThat(dto.getTrackingId()).isEqualTo("RTI-2026-000001");
        assertThat(dto.getPreviousStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(dto.getNewStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(dto.getReviewerUsername()).isEqualTo("officer1");
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
        assertThat(dto.getMessage()).isEqualTo("Transition complete");

        ReviewDecisionResponseDto full = new ReviewDecisionResponseDto(true, 2L, "RTI-2026-000002", RequestStatus.UNDER_REVIEW, RequestStatus.APPROVED, "admin", now, "Msg");
        assertThat(full.getRequestId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("InvalidStateTransitionException: should return formatted message and state getters")
    void testInvalidStateTransitionException() {
        InvalidStateTransitionException ex = new InvalidStateTransitionException(RequestStatus.SUBMITTED, RequestStatus.APPROVED);

        assertThat(ex.getMessage()).isEqualTo("Invalid state transition from SUBMITTED to APPROVED");
        assertThat(ex.getCurrentStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(ex.getTargetStatus()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    @DisplayName("RequestNotFoundException: should return formatted message and trackingId getter")
    void testRequestNotFoundException() {
        RequestNotFoundException ex = new RequestNotFoundException("RTI-2026-000999");

        assertThat(ex.getMessage()).isEqualTo("No RTI request found matching tracking identifier RTI-2026-000999");
        assertThat(ex.getTrackingId()).isEqualTo("RTI-2026-000999");
    }

    @Test
    @DisplayName("DecisionRequestDto: should support getters, setters, and decision helper")
    void testDecisionRequestDto() {
        DecisionRequestDto dto = new DecisionRequestDto();
        dto.setTargetStatus(RequestStatus.UNDER_REVIEW);
        dto.setReviewerUsername("officer1");
        dto.setRemarks("Valid remarks");

        assertThat(dto.getTargetStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(dto.getReviewerUsername()).isEqualTo("officer1");
        assertThat(dto.getRemarks()).isEqualTo("Valid remarks");

        DecisionRequestDto full = new DecisionRequestDto(RequestStatus.APPROVED, "officer2", "Approved records");
        assertThat(full.getTargetStatus()).isEqualTo(RequestStatus.APPROVED);

        dto.setDecision("REJECTED");
        assertThat(dto.getTargetStatus()).isEqualTo(RequestStatus.REJECTED);
    }
}
