package com.college.rti.service;

import com.college.rti.dto.ReviewDecisionResponseDto;
import com.college.rti.dto.ReviewerQueueItemDto;
import com.college.rti.exception.InvalidStateTransitionException;
import com.college.rti.exception.RequestNotFoundException;
import com.college.rti.model.RequestStatus;
import com.college.rti.model.ReviewDecision;
import com.college.rti.model.RtiRequest;
import com.college.rti.model.StatusHistory;
import com.college.rti.repository.ReviewDecisionRepository;
import com.college.rti.repository.RtiRequestRepository;
import com.college.rti.repository.StatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ReviewerWorkflowServiceTests {

    @Autowired
    private ReviewerWorkflowService reviewerWorkflowService;

    @Autowired
    private RtiRequestRepository requestRepository;

    @Autowired
    private ReviewDecisionRepository decisionRepository;

    @Autowired
    private StatusHistoryRepository historyRepository;

    private RtiRequest request1;
    private RtiRequest request2;
    private RtiRequest closedRequest;

    @BeforeEach
    void setUp() throws InterruptedException {
        historyRepository.deleteAll();
        decisionRepository.deleteAll();
        requestRepository.deleteAll();

        request1 = new RtiRequest(
                "RTI-2026-111001",
                "Alice First",
                "alice@example.com",
                "9876543210",
                "ACADEMIC",
                "First request filed."
        );
        request1 = requestRepository.save(request1);

        Thread.sleep(10); // Ensure timestamp divergence for deterministic queue order

        request2 = new RtiRequest(
                "RTI-2026-111002",
                "Bob Second",
                "bob@example.com",
                "9876543211",
                "FINANCIAL",
                "Second request filed."
        );
        request2.setCurrentStatus(RequestStatus.UNDER_REVIEW);
        request2 = requestRepository.save(request2);

        closedRequest = new RtiRequest(
                "RTI-2026-111003",
                "Charlie Closed",
                "charlie@example.com",
                "9876543212",
                "ADMINISTRATIVE",
                "Resolved request."
        );
        closedRequest.setCurrentStatus(RequestStatus.CLOSED);
        requestRepository.save(closedRequest);
    }

    @Test
    @DisplayName("Queue should include only SUBMITTED and UNDER_REVIEW requests ordered oldest first")
    void shouldReturnOnlyActiveRequestsInDeterministicOrder() {
        List<ReviewerQueueItemDto> queue = reviewerWorkflowService.getPendingReviewerQueue();

        assertThat(queue).hasSize(2);
        assertThat(queue.get(0).getTrackingId()).isEqualTo("RTI-2026-111001");
        assertThat(queue.get(0).getCurrentStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(queue.get(1).getTrackingId()).isEqualTo("RTI-2026-111002");
        assertThat(queue.get(1).getCurrentStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);

        // Verify CLOSED request is excluded
        assertThat(queue).noneMatch(item -> item.getTrackingId().equals("RTI-2026-111003"));
    }

    @Test
    @DisplayName("Should allow valid triage transition from SUBMITTED to UNDER_REVIEW")
    void shouldAllowTriageTransitionToUnderReview() {
        ReviewDecisionResponseDto response = reviewerWorkflowService.processDecision(
                request1.getId(),
                RequestStatus.UNDER_REVIEW,
                "officer_patil",
                "Assigned for document scrutiny"
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPreviousStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(response.getNewStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);

        RtiRequest updated = requestRepository.findById(request1.getId()).orElseThrow();
        assertThat(updated.getCurrentStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);

        // Triage does NOT create a ReviewDecision record
        List<ReviewDecision> decisions = decisionRepository.findByRequestIdOrderByDecidedAtAsc(request1.getId());
        assertThat(decisions).isEmpty();

        // But DOES create a StatusHistory record
        List<StatusHistory> history = historyRepository.findByRequestIdOrderByTimestampAsc(request1.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getFromStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(history.get(0).getToStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(history.get(0).getActionBy()).isEqualTo("REVIEWER:officer_patil");
    }

    @Test
    @DisplayName("Should allow valid decision transition from UNDER_REVIEW to APPROVED with ReviewDecision")
    void shouldAllowTransitionFromUnderReviewToApproved() {
        ReviewDecisionResponseDto response = reviewerWorkflowService.processDecision(
                request2.getId(),
                RequestStatus.APPROVED,
                "officer_patil",
                "Syllabus documents verified and public disclosure approved"
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPreviousStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(response.getNewStatus()).isEqualTo(RequestStatus.APPROVED);

        // ReviewDecision record MUST be persisted
        List<ReviewDecision> decisions = decisionRepository.findByRequestIdOrderByDecidedAtAsc(request2.getId());
        assertThat(decisions).hasSize(1);
        assertThat(decisions.get(0).getDecision()).isEqualTo(RequestStatus.APPROVED);
        assertThat(decisions.get(0).getReviewerUsername()).isEqualTo("officer_patil");

        // StatusHistory record MUST be persisted
        List<StatusHistory> history = historyRepository.findByRequestIdOrderByTimestampAsc(request2.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getToStatus()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    @DisplayName("Should allow valid decision transition from UNDER_REVIEW to REJECTED with ReviewDecision")
    void shouldAllowTransitionFromUnderReviewToRejected() {
        ReviewDecisionResponseDto response = reviewerWorkflowService.processDecision(
                request2.getId(),
                RequestStatus.REJECTED,
                "officer_patil",
                "Requested records are exempt under section 8(1)(j)"
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getNewStatus()).isEqualTo(RequestStatus.REJECTED);

        List<ReviewDecision> decisions = decisionRepository.findByRequestIdOrderByDecidedAtAsc(request2.getId());
        assertThat(decisions).hasSize(1);
        assertThat(decisions.get(0).getDecision()).isEqualTo(RequestStatus.REJECTED);
    }

    @Test
    @DisplayName("Should reject skipped transitions (e.g. SUBMITTED directly to APPROVED without UNDER_REVIEW)")
    void shouldRejectDirectApprovalFromSubmitted() {
        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(
                request1.getId(),
                RequestStatus.APPROVED,
                "officer_patil",
                "Direct approval attempt"
        )).isInstanceOf(InvalidStateTransitionException.class)
          .hasMessageContaining("Invalid state transition from SUBMITTED to APPROVED");

        // Verify database state remains unchanged
        RtiRequest unchanged = requestRepository.findById(request1.getId()).orElseThrow();
        assertThat(unchanged.getCurrentStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(decisionRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should reject transitions from terminal states")
    void shouldRejectTransitionFromTerminalState() {
        // request2 transition to APPROVED
        reviewerWorkflowService.processDecision(
                request2.getId(),
                RequestStatus.APPROVED,
                "officer_patil",
                "Approval"
        );

        // Attempt transition from APPROVED to REJECTED
        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(
                request2.getId(),
                RequestStatus.REJECTED,
                "officer_patil",
                "Attempting to reject approved request"
        )).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("Should reject blank reviewer username or blank remarks")
    void shouldRejectBlankReviewerOrRemarks() {
        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(
                request1.getId(),
                RequestStatus.UNDER_REVIEW,
                "", // Blank reviewer
                "Remarks"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Reviewer username must not be blank");

        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(
                request1.getId(),
                RequestStatus.UNDER_REVIEW,
                "officer1",
                "   " // Blank remarks
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Decision remarks must not be blank");
    }

    @Test
    @DisplayName("Should throw RequestNotFoundException when request ID does not exist")
    void shouldThrowNotFoundForInvalidRequestId() {
        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(
                999999L,
                RequestStatus.UNDER_REVIEW,
                "officer1",
                "Valid remarks"
        )).isInstanceOf(RequestNotFoundException.class);
    }
}
