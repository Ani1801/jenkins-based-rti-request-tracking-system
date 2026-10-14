package com.college.rti.service;

import com.college.rti.model.RequestStatus;
import com.college.rti.model.RtiRequest;
import com.college.rti.model.StatusHistory;
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
class ReviewerWorkflowRollbackTests {

    @Autowired
    private ReviewerWorkflowService reviewerWorkflowService;

    @Autowired
    private RtiRequestRepository requestRepository;

    @Autowired
    private ReviewDecisionRepository decisionRepository;

    @MockBean
    private StatusHistoryRepository historyRepositoryMock;

    @Test
    @DisplayName("Should rollback decision and request update if status history persistence fails")
    void shouldRollbackWhenStatusHistoryFails() {
        RtiRequest req = new RtiRequest(
                "RTI-2026-888001",
                "Rollback User",
                "rollback@example.com",
                "9876543210",
                "ACADEMIC",
                "Testing rollback on decision update."
        );
        req.setCurrentStatus(RequestStatus.UNDER_REVIEW);
        req = requestRepository.save(req);

        // Simulate DB failure when writing status history
        doThrow(new RuntimeException("Simulated status history failure on review decision"))
                .when(historyRepositoryMock).save(any(StatusHistory.class));

        long decisionCountBefore = decisionRepository.count();
        Long reqId = req.getId();

        assertThatThrownBy(() -> reviewerWorkflowService.processDecision(
                reqId,
                RequestStatus.APPROVED,
                "officer_test",
                "Approval remarks"
        )).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Simulated status history failure on review decision");

        // Assert that ReviewDecision was rolled back
        assertThat(decisionRepository.count()).isEqualTo(decisionCountBefore);

        // Assert that request status was rolled back and remains UNDER_REVIEW
        RtiRequest refreshed = requestRepository.findById(reqId).orElseThrow();
        assertThat(refreshed.getCurrentStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
    }
}
