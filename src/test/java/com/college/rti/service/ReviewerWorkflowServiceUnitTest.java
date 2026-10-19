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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewerWorkflowServiceUnitTest {

    @Mock
    private RtiRequestRepository requestRepository;

    @Mock
    private ReviewDecisionRepository decisionRepository;

    @Mock
    private StatusHistoryRepository historyRepository;

    @InjectMocks
    private ReviewerWorkflowService reviewerWorkflowService;

    @Test
    @DisplayName("Unit: Should return pending queue mapped to DTOs in FIFO order")
    void shouldReturnPendingQueueMapped() {
        RtiRequest r1 = new RtiRequest("RTI-2026-000001", "Alice", "alice@example.com", "9876543210", "ACADEMIC", "Desc 1");
        r1.setId(1L);
        r1.setCurrentStatus(RequestStatus.SUBMITTED);
        r1.setCreatedAt(LocalDateTime.now().minusHours(2));

        RtiRequest r2 = new RtiRequest("RTI-2026-000002", "Bob", "bob@example.com", "9876543211", "FINANCIAL", "Desc 2");
        r2.setId(2L);
        r2.setCurrentStatus(RequestStatus.UNDER_REVIEW);
        r2.setCreatedAt(LocalDateTime.now().minusHours(1));

        List<RequestStatus> activeStatuses = List.of(RequestStatus.SUBMITTED, RequestStatus.UNDER_REVIEW);
        when(requestRepository.findByCurrentStatusInOrderByCreatedAtAsc(activeStatuses)).thenReturn(List.of(r1, r2));

        List<ReviewerQueueItemDto> queue = reviewerWorkflowService.getPendingReviewerQueue();

        assertThat(queue).hasSize(2);
        assertThat(queue.get(0).getTrackingId()).isEqualTo("RTI-2026-000001");
        assertThat(queue.get(0).getApplicantName()).isEqualTo("Alice");
        assertThat(queue.get(1).getTrackingId()).isEqualTo("RTI-2026-000002");
        assertThat(queue.get(1).getApplicantName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("Unit: Should transition SUBMITTED to UNDER_REVIEW without creating ReviewDecision")
    void shouldProcessTriageTransition() {
        RtiRequest req = new RtiRequest("RTI-2026-000001", "Alice", "alice@example.com", "9876543210", "ACADEMIC", "Desc 1");
        req.setId(10L);
        req.setCurrentStatus(RequestStatus.SUBMITTED);

        when(requestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(req));

        ReviewDecisionResponseDto response = reviewerWorkflowService.processDecision(
                10L,
                RequestStatus.UNDER_REVIEW,
                "officer_triage",
                "Assigned for scrutiny"
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPreviousStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(response.getNewStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(req.getCurrentStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);

        verify(requestRepository).saveAndFlush(req);
        verify(decisionRepository, never()).save(any());

        ArgumentCaptor<StatusHistory> histCaptor = ArgumentCaptor.forClass(StatusHistory.class);
        verify(historyRepository).save(histCaptor.capture());
        StatusHistory hist = histCaptor.getValue();
        assertThat(hist.getFromStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(hist.getToStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(hist.getActionBy()).isEqualTo("REVIEWER:officer_triage");
        assertThat(hist.getRemarks()).isEqualTo("Assigned for scrutiny");
    }

    @Test
    @DisplayName("Unit: Should transition UNDER_REVIEW to APPROVED and persist ReviewDecision")
    void shouldProcessApprovalTransition() {
        RtiRequest req = new RtiRequest("RTI-2026-000002", "Bob", "bob@example.com", "9876543211", "FINANCIAL", "Desc 2");
        req.setId(20L);
        req.setCurrentStatus(RequestStatus.UNDER_REVIEW);

        when(requestRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(req));

        ReviewDecisionResponseDto response = reviewerWorkflowService.processDecision(
                20L,
                RequestStatus.APPROVED,
                "officer_lead",
                "Information disclosed"
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPreviousStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(response.getNewStatus()).isEqualTo(RequestStatus.APPROVED);

        ArgumentCaptor<ReviewDecision> decCaptor = ArgumentCaptor.forClass(ReviewDecision.class);
        verify(decisionRepository).save(decCaptor.capture());
        ReviewDecision dec = decCaptor.getValue();
        assertThat(dec.getDecision()).isEqualTo(RequestStatus.APPROVED);
        assertThat(dec.getReviewerUsername()).isEqualTo("officer_lead");
        assertThat(dec.getRemarks()).isEqualTo("Information disclosed");

        verify(requestRepository).saveAndFlush(req);
        verify(historyRepository).save(any(StatusHistory.class));
    }

    @Test
    @DisplayName("Unit: Should transition UNDER_REVIEW to REJECTED and persist ReviewDecision")
    void shouldProcessRejectionTransition() {
        RtiRequest req = new RtiRequest("RTI-2026-000003", "Charlie", "charlie@example.com", "9876543212", "ADMIN", "Desc 3");
        req.setId(30L);
        req.setCurrentStatus(RequestStatus.UNDER_REVIEW);

        when(requestRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(req));

        ReviewDecisionResponseDto response = reviewerWorkflowService.processDecision(
                30L,
                RequestStatus.REJECTED,
                "officer_lead",
                "Exempt from disclosure"
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getNewStatus()).isEqualTo(RequestStatus.REJECTED);

        ArgumentCaptor<ReviewDecision> decCaptor = ArgumentCaptor.forClass(ReviewDecision.class);
        verify(decisionRepository).save(decCaptor.capture());
        assertThat(decCaptor.getValue().getDecision()).isEqualTo(RequestStatus.REJECTED);
    }

    @Test
    @DisplayName("Unit: Should throw InvalidStateTransitionException on forbidden state changes")
    void shouldThrowOnInvalidTransition() {
        RtiRequest req = new RtiRequest("RTI-2026-000001", "Alice", "alice@example.com", "9876543210", "ACADEMIC", "Desc 1");
        req.setId(10L);
        req.setCurrentStatus(RequestStatus.SUBMITTED);

        when(requestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(req));

        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(
                10L,
                RequestStatus.APPROVED,
                "officer1",
                "Direct approval attempt"
        )).isInstanceOf(InvalidStateTransitionException.class);

        verify(decisionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
        verify(requestRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Unit: Should throw RequestNotFoundException when request id does not exist")
    void shouldThrowWhenRequestNotFound() {
        when(requestRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(
                999L,
                RequestStatus.UNDER_REVIEW,
                "officer1",
                "Remarks"
        )).isInstanceOf(RequestNotFoundException.class);
    }

    @Test
    @DisplayName("Unit: Should throw IllegalArgumentException for blank reviewer, blank remarks, or null target")
    void shouldThrowForInvalidArguments() {
        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(1L, RequestStatus.UNDER_REVIEW, null, "remarks"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(1L, RequestStatus.UNDER_REVIEW, "  ", "remarks"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(1L, RequestStatus.UNDER_REVIEW, "officer", null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(1L, RequestStatus.UNDER_REVIEW, "officer", "   "))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(1L, null, "officer", "remarks"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
