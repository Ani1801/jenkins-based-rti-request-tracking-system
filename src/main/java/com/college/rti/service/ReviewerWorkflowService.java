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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service orchestrating reviewer queue queries and state-machine transitions:
 * SUBMITTED -> UNDER_REVIEW -> APPROVED / REJECTED
 */
@Service
public class ReviewerWorkflowService {

    private final RtiRequestRepository requestRepository;
    private final ReviewDecisionRepository decisionRepository;
    private final StatusHistoryRepository historyRepository;

    public ReviewerWorkflowService(RtiRequestRepository requestRepository,
                                   ReviewDecisionRepository decisionRepository,
                                   StatusHistoryRepository historyRepository) {
        this.requestRepository = requestRepository;
        this.decisionRepository = decisionRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Retrieves all pending requests in SUBMITTED or UNDER_REVIEW state,
     * ordered deterministically by creation timestamp ascending (oldest first).
     */
    @Transactional(readOnly = true)
    public List<ReviewerQueueItemDto> getPendingReviewerQueue() {
        List<RequestStatus> activeStatuses = List.of(RequestStatus.SUBMITTED, RequestStatus.UNDER_REVIEW);
        List<RtiRequest> requests = requestRepository.findByCurrentStatusInOrderByCreatedAtAsc(activeStatuses);

        return requests.stream()
                .map(this::toQueueItemDto)
                .collect(Collectors.toList());
    }

    /**
     * Transition a request through the administrative state machine.
     * Uses pessimistic write locking (SELECT ... FOR UPDATE) to prevent race conditions.
     *
     * @param requestId        Internal primary key of the target RtiRequest
     * @param targetStatus     Desired state (UNDER_REVIEW, APPROVED, REJECTED)
     * @param reviewerUsername Identifier of the reviewer making the decision
     * @param remarks          Mandatory justification remarks
     * @return ReviewDecisionResponseDto
     */
    @Transactional
    public ReviewDecisionResponseDto processDecision(Long requestId,
                                                     RequestStatus targetStatus,
                                                     String reviewerUsername,
                                                     String remarks) {
        // Validate reviewer username
        if (reviewerUsername == null || reviewerUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Reviewer username must not be blank");
        }

        // Validate remarks
        if (remarks == null || remarks.trim().isEmpty()) {
            throw new IllegalArgumentException("Decision remarks must not be blank");
        }

        if (targetStatus == null) {
            throw new IllegalArgumentException("Target status must not be null");
        }

        // Load request with pessimistic write lock (SELECT ... FOR UPDATE)
        RtiRequest request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new RequestNotFoundException("ID: " + requestId));

        RequestStatus currentStatus = request.getCurrentStatus();

        // Enforce state transition rules
        validateTransition(currentStatus, targetStatus);

        String trimmedReviewer = reviewerUsername.trim();
        String trimmedRemarks = remarks.trim();

        // If target is APPROVED or REJECTED, persist a ReviewDecision record
        if (targetStatus == RequestStatus.APPROVED || targetStatus == RequestStatus.REJECTED) {
            ReviewDecision decision = new ReviewDecision(
                    request,
                    trimmedReviewer,
                    targetStatus,
                    trimmedRemarks
            );
            decisionRepository.save(decision);
        }

        // Update request current status
        request.setCurrentStatus(targetStatus);
        requestRepository.saveAndFlush(request);

        // Append StatusHistory audit record in the same atomic transaction
        StatusHistory history = new StatusHistory(
                request,
                currentStatus,
                targetStatus,
                "REVIEWER:" + trimmedReviewer,
                trimmedRemarks
        );
        historyRepository.save(history);

        return new ReviewDecisionResponseDto(
                true,
                request.getId(),
                request.getTrackingId(),
                currentStatus,
                targetStatus,
                trimmedReviewer,
                LocalDateTime.now(),
                String.format("Request %s successfully transitioned to %s", request.getTrackingId(), targetStatus)
        );
    }

    /**
     * Validates whether a state transition is permitted.
     * Allowed transitions:
     * - SUBMITTED -> UNDER_REVIEW
     * - UNDER_REVIEW -> APPROVED
     * - UNDER_REVIEW -> REJECTED
     */
    private void validateTransition(RequestStatus current, RequestStatus target) {
        if (current == RequestStatus.SUBMITTED && target == RequestStatus.UNDER_REVIEW) {
            return; // Valid triage
        }

        if (current == RequestStatus.UNDER_REVIEW && (target == RequestStatus.APPROVED || target == RequestStatus.REJECTED)) {
            return; // Valid final decision
        }

        // All other transitions are forbidden (e.g. SUBMITTED -> APPROVED directly, or terminal transitions)
        throw new InvalidStateTransitionException(current, target);
    }

    private ReviewerQueueItemDto toQueueItemDto(RtiRequest r) {
        return new ReviewerQueueItemDto(
                r.getId(),
                r.getTrackingId(),
                r.getApplicantName(),
                r.getApplicantEmail(),
                r.getApplicantPhone(),
                r.getCategory(),
                r.getDescription(),
                r.getAttachmentPath(),
                r.getAttachmentOriginalName(),
                r.getCurrentStatus(),
                r.getCreatedAt()
        );
    }
}
