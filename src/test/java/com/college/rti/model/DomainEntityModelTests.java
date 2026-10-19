package com.college.rti.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEntityModelTests {

    @Test
    @DisplayName("RtiRequest: should support getters, setters, lifecycle callbacks, and relationships")
    void testRtiRequestEntity() {
        RtiRequest request = new RtiRequest();
        request.setId(10L);
        request.setTrackingId("RTI-2026-000123");
        request.setApplicantName("John Citizen");
        request.setApplicantEmail("john@example.com");
        request.setApplicantPhone("9876543210");
        request.setCategory("ADMINISTRATIVE");
        request.setDescription("Detailed request description");
        request.setAttachmentPath("/uploads/doc.pdf");
        request.setAttachmentOriginalName("doc.pdf");
        request.setCurrentStatus(RequestStatus.SUBMITTED);
        LocalDateTime now = LocalDateTime.now();
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        request.setReviewDecisions(new ArrayList<>());
        request.setStatusHistories(new ArrayList<>());

        assertThat(request.getId()).isEqualTo(10L);
        assertThat(request.getTrackingId()).isEqualTo("RTI-2026-000123");
        assertThat(request.getApplicantName()).isEqualTo("John Citizen");
        assertThat(request.getApplicantEmail()).isEqualTo("john@example.com");
        assertThat(request.getApplicantPhone()).isEqualTo("9876543210");
        assertThat(request.getCategory()).isEqualTo("ADMINISTRATIVE");
        assertThat(request.getDescription()).isEqualTo("Detailed request description");
        assertThat(request.getAttachmentPath()).isEqualTo("/uploads/doc.pdf");
        assertThat(request.getAttachmentOriginalName()).isEqualTo("doc.pdf");
        assertThat(request.getCurrentStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(request.getCreatedAt()).isEqualTo(now);
        assertThat(request.getUpdatedAt()).isEqualTo(now);
        assertThat(request.getReviewDecisions()).isEmpty();
        assertThat(request.getStatusHistories()).isEmpty();

        // Lifecycle callbacks
        RtiRequest callbackRequest = new RtiRequest("RTI-2026-000124", "Jane", "jane@example.com", "9876543211", "ACADEMIC", "Desc");
        callbackRequest.setCurrentStatus(null);
        callbackRequest.onCreate();
        assertThat(callbackRequest.getCreatedAt()).isNotNull();
        assertThat(callbackRequest.getUpdatedAt()).isNotNull();
        assertThat(callbackRequest.getCurrentStatus()).isEqualTo(RequestStatus.SUBMITTED);

        LocalDateTime previousUpdatedAt = callbackRequest.getUpdatedAt();
        callbackRequest.onUpdate();
        assertThat(callbackRequest.getUpdatedAt()).isNotNull();

        // Bidirectional relationships
        StatusHistory history = new StatusHistory();
        callbackRequest.addStatusHistory(history);
        assertThat(callbackRequest.getStatusHistories()).contains(history);
        assertThat(history.getRequest()).isEqualTo(callbackRequest);

        ReviewDecision decision = new ReviewDecision();
        callbackRequest.addReviewDecision(decision);
        assertThat(callbackRequest.getReviewDecisions()).contains(decision);
        assertThat(decision.getRequest()).isEqualTo(callbackRequest);

        // Equals and HashCode
        RtiRequest duplicate = new RtiRequest();
        duplicate.setTrackingId("RTI-2026-000123");
        assertThat(request).isEqualTo(duplicate);
        assertThat(request.hashCode()).isEqualTo(duplicate.hashCode());
        assertThat(request).isEqualTo(request);
        assertThat(request).isNotEqualTo(null);
        assertThat(request).isNotEqualTo("some string");

        RtiRequest different = new RtiRequest();
        different.setTrackingId("RTI-2026-999999");
        assertThat(request).isNotEqualTo(different);
    }

    @Test
    @DisplayName("StatusHistory: should support getters, setters, lifecycle, equals, and hashCode")
    void testStatusHistoryEntity() {
        RtiRequest request = new RtiRequest();
        request.setId(1L);

        StatusHistory history = new StatusHistory();
        history.setId(5L);
        history.setRequest(request);
        history.setFromStatus(RequestStatus.SUBMITTED);
        history.setToStatus(RequestStatus.UNDER_REVIEW);
        history.setActionBy("REVIEWER:admin");
        history.setRemarks("Moved to under review");
        LocalDateTime time = LocalDateTime.now();
        history.setTimestamp(time);

        assertThat(history.getId()).isEqualTo(5L);
        assertThat(history.getRequest()).isEqualTo(request);
        assertThat(history.getFromStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(history.getToStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(history.getActionBy()).isEqualTo("REVIEWER:admin");
        assertThat(history.getRemarks()).isEqualTo("Moved to under review");
        assertThat(history.getTimestamp()).isEqualTo(time);

        // PrePersist lifecycle callback
        StatusHistory newHistory = new StatusHistory(request, null, RequestStatus.SUBMITTED, "APPLICANT", "Initial");
        newHistory.onCreate();
        assertThat(newHistory.getTimestamp()).isNotNull();

        // Equals and HashCode
        StatusHistory historySameId = new StatusHistory();
        historySameId.setId(5L);
        assertThat(history).isEqualTo(historySameId);
        assertThat(history.hashCode()).isEqualTo(historySameId.hashCode());
        assertThat(history).isEqualTo(history);
        assertThat(history).isNotEqualTo(null);
        assertThat(history).isNotEqualTo(new Object());

        StatusHistory historyDiffId = new StatusHistory();
        historyDiffId.setId(6L);
        assertThat(history).isNotEqualTo(historyDiffId);
    }

    @Test
    @DisplayName("ReviewDecision: should support getters, setters, lifecycle, equals, and hashCode")
    void testReviewDecisionEntity() {
        RtiRequest request = new RtiRequest();
        request.setId(2L);

        ReviewDecision decision = new ReviewDecision();
        decision.setId(15L);
        decision.setRequest(request);
        decision.setReviewerUsername("officer_smith");
        decision.setDecision(RequestStatus.APPROVED);
        decision.setRemarks("Approval remarks");
        LocalDateTime decidedAt = LocalDateTime.now();
        decision.setDecidedAt(decidedAt);

        assertThat(decision.getId()).isEqualTo(15L);
        assertThat(decision.getRequest()).isEqualTo(request);
        assertThat(decision.getReviewerUsername()).isEqualTo("officer_smith");
        assertThat(decision.getDecision()).isEqualTo(RequestStatus.APPROVED);
        assertThat(decision.getRemarks()).isEqualTo("Approval remarks");
        assertThat(decision.getDecidedAt()).isEqualTo(decidedAt);

        // PrePersist lifecycle callback
        ReviewDecision newDecision = new ReviewDecision(request, "officer_smith", RequestStatus.APPROVED, "Approved");
        newDecision.onCreate();
        assertThat(newDecision.getDecidedAt()).isNotNull();

        // Equals and HashCode
        ReviewDecision decisionSameId = new ReviewDecision();
        decisionSameId.setId(15L);
        assertThat(decision).isEqualTo(decisionSameId);
        assertThat(decision.hashCode()).isEqualTo(decisionSameId.hashCode());
        assertThat(decision).isEqualTo(decision);
        assertThat(decision).isNotEqualTo(null);
        assertThat(decision).isNotEqualTo("other");

        ReviewDecision decisionDiffId = new ReviewDecision();
        decisionDiffId.setId(16L);
        assertThat(decision).isNotEqualTo(decisionDiffId);
    }

    @Test
    @DisplayName("TrackingSequence: should support getters, setters, equals, and hashCode")
    void testTrackingSequenceEntity() {
        TrackingSequence sequence = new TrackingSequence();
        sequence.setSequenceYear(2026);
        sequence.setCurrentVal(100L);

        assertThat(sequence.getSequenceYear()).isEqualTo(2026);
        assertThat(sequence.getCurrentVal()).isEqualTo(100L);

        TrackingSequence duplicate = new TrackingSequence(2026, 100L);
        assertThat(sequence).isEqualTo(duplicate);
        assertThat(sequence.hashCode()).isEqualTo(duplicate.hashCode());
        assertThat(sequence).isEqualTo(sequence);
        assertThat(sequence).isNotEqualTo(null);
        assertThat(sequence).isNotEqualTo("diff");

        TrackingSequence differentYear = new TrackingSequence(2027, 100L);
        assertThat(sequence).isNotEqualTo(differentYear);
    }

    @Test
    @DisplayName("RequestStatus: enum should contain all expected business states")
    void testRequestStatusEnum() {
        assertThat(RequestStatus.values()).containsExactly(
                RequestStatus.SUBMITTED,
                RequestStatus.UNDER_REVIEW,
                RequestStatus.APPROVED,
                RequestStatus.REJECTED,
                RequestStatus.CLOSED
        );
        assertThat(RequestStatus.valueOf("SUBMITTED")).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(RequestStatus.valueOf("APPROVED")).isEqualTo(RequestStatus.APPROVED);
    }
}
