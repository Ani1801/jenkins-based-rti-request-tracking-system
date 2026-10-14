package com.college.rti.repository;

import com.college.rti.model.RequestStatus;
import com.college.rti.model.ReviewDecision;
import com.college.rti.model.RtiRequest;
import com.college.rti.model.StatusHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class RtiDomainRepositoryTests {

    @Autowired
    private RtiRequestRepository requestRepository;

    @Autowired
    private ReviewDecisionRepository decisionRepository;

    @Autowired
    private StatusHistoryRepository historyRepository;

    private RtiRequest sampleRequest;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        decisionRepository.deleteAll();
        requestRepository.deleteAll();

        sampleRequest = new RtiRequest(
                "RTI-2026-000001",
                "Alice Smith",
                "alice.smith@example.com",
                "9876543210",
                "ACADEMIC",
                "Requesting official syllabus and course outline for Computer Science 2026."
        );
    }

    @Test
    @DisplayName("Should successfully save and retrieve an RTI Request by internal ID and tracking ID")
    void shouldSaveAndRetrieveRtiRequest() {
        RtiRequest saved = requestRepository.save(sampleRequest);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCurrentStatus()).isEqualTo(RequestStatus.SUBMITTED);

        Optional<RtiRequest> foundByTrackingId = requestRepository.findByTrackingId("RTI-2026-000001");
        assertThat(foundByTrackingId).isPresent();
        assertThat(foundByTrackingId.get().getApplicantEmail()).isEqualTo("alice.smith@example.com");
        assertThat(foundByTrackingId.get().getCategory()).isEqualTo("ACADEMIC");
        assertThat(requestRepository.existsByTrackingId("RTI-2026-000001")).isTrue();
        assertThat(requestRepository.existsByTrackingId("RTI-2026-NONEXISTENT")).isFalse();
    }

    @Test
    @DisplayName("Should enforce uniqueness constraint on tracking_id")
    void shouldEnforceUniqueTrackingIdConstraint() {
        requestRepository.saveAndFlush(sampleRequest);

        RtiRequest duplicateRequest = new RtiRequest(
                "RTI-2026-000001", // Duplicate tracking ID
                "Bob Jones",
                "bob.jones@example.com",
                "9123456780",
                "ADMINISTRATIVE",
                "Duplicate tracking ID submission attempt."
        );

        assertThatThrownBy(() -> requestRepository.saveAndFlush(duplicateRequest))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should save and retrieve related StatusHistory records in chronological order")
    void shouldSaveAndRetrieveStatusHistoryChronologically() {
        RtiRequest saved = requestRepository.save(sampleRequest);

        StatusHistory step1 = new StatusHistory(
                saved,
                null,
                RequestStatus.SUBMITTED,
                "APPLICANT",
                "Application filed via public portal"
        );
        historyRepository.save(step1);

        StatusHistory step2 = new StatusHistory(
                saved,
                RequestStatus.SUBMITTED,
                RequestStatus.UNDER_REVIEW,
                "REVIEWER:officer1",
                "Assigned to PIO for department verification"
        );
        historyRepository.save(step2);

        List<StatusHistory> history = historyRepository.findByRequestIdOrderByTimestampAsc(saved.getId());
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getToStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(history.get(1).getToStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(history.get(1).getActionBy()).isEqualTo("REVIEWER:officer1");

        // Verify retrieval by public Tracking ID
        List<StatusHistory> historyByToken = historyRepository.findByRequestTrackingIdOrderByTimestampAsc("RTI-2026-000001");
        assertThat(historyByToken).hasSize(2);
    }

    @Test
    @DisplayName("Should save and retrieve ReviewDecision linked to an RTI request")
    void shouldSaveAndRetrieveReviewDecision() {
        RtiRequest saved = requestRepository.save(sampleRequest);

        ReviewDecision decision = new ReviewDecision(
                saved,
                "reviewer_admin",
                RequestStatus.APPROVED,
                "Information is public under section 4. Request approved and response sent."
        );
        ReviewDecision savedDecision = decisionRepository.save(decision);

        assertThat(savedDecision.getId()).isNotNull();
        assertThat(savedDecision.getDecidedAt()).isNotNull();

        List<ReviewDecision> decisions = decisionRepository.findByRequestIdOrderByDecidedAtAsc(saved.getId());
        assertThat(decisions).hasSize(1);
        assertThat(decisions.get(0).getReviewerUsername()).isEqualTo("reviewer_admin");
        assertThat(decisions.get(0).getDecision()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    @DisplayName("Should query requests by status for reviewer queues")
    void shouldQueryRequestsByStatus() {
        requestRepository.save(sampleRequest);

        RtiRequest secondRequest = new RtiRequest(
                "RTI-2026-000002",
                "Charlie Brown",
                "charlie@example.com",
                "9998887776",
                "FINANCIAL",
                "Requesting audit reports."
        );
        secondRequest.setCurrentStatus(RequestStatus.UNDER_REVIEW);
        requestRepository.save(secondRequest);

        List<RtiRequest> submitted = requestRepository.findByCurrentStatusOrderByCreatedAtAsc(RequestStatus.SUBMITTED);
        assertThat(submitted).hasSize(1);
        assertThat(submitted.get(0).getTrackingId()).isEqualTo("RTI-2026-000001");

        List<RtiRequest> activeQueue = requestRepository.findByCurrentStatusInOrderByCreatedAtAsc(
                List.of(RequestStatus.SUBMITTED, RequestStatus.UNDER_REVIEW)
        );
        assertThat(activeQueue).hasSize(2);
    }
}
