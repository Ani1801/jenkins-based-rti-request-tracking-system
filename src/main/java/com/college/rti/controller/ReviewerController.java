package com.college.rti.controller;

import com.college.rti.dto.DecisionRequestDto;
import com.college.rti.dto.ReviewDecisionResponseDto;
import com.college.rti.dto.ReviewerQueueItemDto;
import com.college.rti.exception.RequestNotFoundException;
import com.college.rti.model.RtiRequest;
import com.college.rti.repository.RtiRequestRepository;
import com.college.rti.service.ReviewerWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for administrative reviewer operations:
 * viewing queue, inspecting submissions, and applying state-machine transitions.
 */
@RestController
@RequestMapping("/api/v1/reviewer")
public class ReviewerController {

    private final ReviewerWorkflowService reviewerWorkflowService;
    private final RtiRequestRepository requestRepository;

    public ReviewerController(ReviewerWorkflowService reviewerWorkflowService,
                              RtiRequestRepository requestRepository) {
        this.reviewerWorkflowService = reviewerWorkflowService;
        this.requestRepository = requestRepository;
    }

    /**
     * Retrieve all pending RTI requests in SUBMITTED or UNDER_REVIEW state.
     */
    @GetMapping("/requests")
    public ResponseEntity<List<ReviewerQueueItemDto>> getReviewerQueue() {
        List<ReviewerQueueItemDto> queue = reviewerWorkflowService.getPendingReviewerQueue();
        return ResponseEntity.ok(queue);
    }

    /**
     * Retrieve full request details by internal ID.
     */
    @GetMapping("/requests/{id}")
    public ResponseEntity<ReviewerQueueItemDto> getRequestDetails(@PathVariable Long id) {
        RtiRequest r = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("ID: " + id));

        ReviewerQueueItemDto dto = new ReviewerQueueItemDto(
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
        return ResponseEntity.ok(dto);
    }

    /**
     * Apply an administrative state-machine transition (SUBMITTED -> UNDER_REVIEW -> APPROVED/REJECTED).
     */
    @PostMapping("/requests/{id}/decision")
    public ResponseEntity<ReviewDecisionResponseDto> processDecision(
            @PathVariable Long id,
            @Valid @RequestBody DecisionRequestDto dto) {
        ReviewDecisionResponseDto response = reviewerWorkflowService.processDecision(
                id,
                dto.getTargetStatus(),
                dto.getReviewerUsername(),
                dto.getRemarks()
        );
        return ResponseEntity.ok(response);
    }
}
