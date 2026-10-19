package com.college.rti.service;

import com.college.rti.model.RequestStatus;
import com.college.rti.model.ReviewDecision;
import com.college.rti.model.RtiRequest;
import com.college.rti.repository.ReviewDecisionRepository;
import com.college.rti.repository.RtiRequestRepository;
import com.college.rti.repository.StatusHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class ReviewDecisionRollbackTests {

    @Autowired
    private ReviewerWorkflowService reviewerWorkflowService;

    @Autowired
    private RtiRequestRepository requestRepository;

    @Autowired
    private StatusHistoryRepository historyRepository;

    @MockBean
    private ReviewDecisionRepository decisionRepositoryMock;

    @Test
    @DisplayName("Should rollback request status and status history if ReviewDecision persistence fails")
    void shouldRollbackWhenDecisionRepositoryFails() {
        RtiRequest req = new RtiRequest(
                "RTI-2026-777001",
                "Decision Rollback User",
                "decision.rollback@example.com",
                "9876543210",
                "FINANCIAL",
                "Testing rollback when review decision table write fails."
        );
        req.setCurrentStatus(RequestStatus.UNDER_REVIEW);
        req = requestRepository.save(req);

        // Simulate DB failure when persisting ReviewDecision
        doThrow(new RuntimeException("Simulated review decision DB write failure"))
                .when(decisionRepositoryMock).save(any(ReviewDecision.class));

        long historyCountBefore = historyRepository.count();
        Long reqId = req.getId();

        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(
                reqId,
                RequestStatus.APPROVED,
                "officer_test",
                "Attempted approval remarks"
        )).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Simulated review decision DB write failure");

        // Status history must NOT have been persisted
        assertThat(historyRepository.count()).isEqualTo(historyCountBefore);

        // Request status must remain UNDER_REVIEW
        RtiRequest refreshed = requestRepository.findById(reqId).orElseThrow();
        assertThat(refreshed.getCurrentStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
    }
}
