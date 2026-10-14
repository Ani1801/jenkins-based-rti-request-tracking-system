package com.college.rti.service;

import com.college.rti.dto.PublicStatusHistoryDto;
import com.college.rti.dto.StatusTrackingResponseDto;
import com.college.rti.exception.RequestNotFoundException;
import com.college.rti.model.RequestStatus;
import com.college.rti.model.RtiRequest;
import com.college.rti.model.StatusHistory;
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
class StatusInquiryServiceTests {

    @Autowired
    private StatusInquiryService statusInquiryService;

    @Autowired
    private RtiRequestRepository requestRepository;

    @Autowired
    private StatusHistoryRepository historyRepository;

    private RtiRequest testRequest;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        requestRepository.deleteAll();

        testRequest = new RtiRequest(
                "RTI-2026-999001",
                "Alice Wonder",
                "alice.wonder@example.com",
                "9876543210",
                "INFRASTRUCTURE",
                "Requesting budget and planning documentation for hostel library renovation."
        );
    }

    @Test
    @DisplayName("Should return correct current status and metadata for existing tracking ID")
    void shouldReturnCorrectStatusForExistingTrackingId() {
        RtiRequest saved = requestRepository.save(testRequest);

        StatusTrackingResponseDto response = statusInquiryService.trackRequest("RTI-2026-999001");

        assertThat(response).isNotNull();
        assertThat(response.getTrackingId()).isEqualTo("RTI-2026-999001");
        assertThat(response.getCategory()).isEqualTo("INFRASTRUCTURE");
        assertThat(response.getStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(response.getSubmittedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should return status history in strict chronological order")
    void shouldReturnChronologicalStatusHistory() throws InterruptedException {
        RtiRequest saved = requestRepository.save(testRequest);

        // Step 1: Initial submission
        StatusHistory step1 = new StatusHistory(
                saved,
                null,
                RequestStatus.SUBMITTED,
                "APPLICANT",
                "Application filed"
        );
        historyRepository.save(step1);

        Thread.sleep(10); // Ensure timestamp divergence

        // Step 2: Transition to UNDER_REVIEW
        StatusHistory step2 = new StatusHistory(
                saved,
                RequestStatus.SUBMITTED,
                RequestStatus.UNDER_REVIEW,
                "REVIEWER:officer1",
                "Assigned to PIO"
        );
        historyRepository.save(step2);

        Thread.sleep(10);

        // Step 3: Transition to APPROVED
        StatusHistory step3 = new StatusHistory(
                saved,
                RequestStatus.UNDER_REVIEW,
                RequestStatus.APPROVED,
                "REVIEWER:officer1",
                "Documentation validated and approved"
        );
        historyRepository.save(step3);

        saved.setCurrentStatus(RequestStatus.APPROVED);
        requestRepository.save(saved);

        StatusTrackingResponseDto response = statusInquiryService.trackRequest("RTI-2026-999001");

        assertThat(response.getStatus()).isEqualTo(RequestStatus.APPROVED);
        List<PublicStatusHistoryDto> history = response.getHistory();
        assertThat(history).hasSize(3);

        assertThat(history.get(0).getFromStatus()).isNull();
        assertThat(history.get(0).getToStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(history.get(0).getRemarks()).isEqualTo("Application filed");

        assertThat(history.get(1).getFromStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(history.get(1).getToStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(history.get(1).getRemarks()).isEqualTo("Assigned to PIO");

        assertThat(history.get(2).getFromStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(history.get(2).getToStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(history.get(2).getRemarks()).isEqualTo("Documentation validated and approved");

        // Verify chronological timestamp ordering
        assertThat(history.get(0).getTimestamp()).isBeforeOrEqualTo(history.get(1).getTimestamp());
        assertThat(history.get(1).getTimestamp()).isBeforeOrEqualTo(history.get(2).getTimestamp());
    }

    @Test
    @DisplayName("Should throw RequestNotFoundException for unknown tracking ID")
    void shouldThrowNotFoundForUnknownTrackingId() {
        assertThatThrownBy(() -> statusInquiryService.trackRequest("RTI-2026-NONEXISTENT"))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessageContaining("No RTI request found matching tracking identifier RTI-2026-NONEXISTENT");
    }

    @Test
    @DisplayName("Should throw RequestNotFoundException for null or empty tracking ID")
    void shouldThrowNotFoundForNullOrEmptyTrackingId() {
        assertThatThrownBy(() -> statusInquiryService.trackRequest(""))
                .isInstanceOf(RequestNotFoundException.class);

        assertThatThrownBy(() -> statusInquiryService.trackRequest("   "))
                .isInstanceOf(RequestNotFoundException.class);

        assertThatThrownBy(() -> statusInquiryService.trackRequest(null))
                .isInstanceOf(RequestNotFoundException.class);
    }

    @Test
    @DisplayName("Public DTOs must omit applicant contact information and internal identifiers")
    void shouldOmitApplicantContactAndInternalIds() {
        RtiRequest saved = requestRepository.save(testRequest);
        StatusHistory step = new StatusHistory(
                saved,
                null,
                RequestStatus.SUBMITTED,
                "REVIEWER:internal_admin_user",
                "Submission logged"
        );
        historyRepository.save(step);

        StatusTrackingResponseDto response = statusInquiryService.trackRequest("RTI-2026-999001");

        // Verify public DTO does NOT expose applicant PII
        assertThat(response.getClass().getDeclaredFields())
                .noneMatch(field -> field.getName().equals("applicantName")
                        || field.getName().equals("applicantEmail")
                        || field.getName().equals("applicantPhone")
                        || field.getName().equals("id"));

        // Verify public history DTO does NOT expose internal actor username
        assertThat(response.getHistory().get(0).getClass().getDeclaredFields())
                .noneMatch(field -> field.getName().equals("actionBy")
                        || field.getName().equals("id")
                        || field.getName().equals("request"));
    }

    @Test
    @DisplayName("Should handle a request with no history records safely")
    void shouldHandleRequestWithNoHistorySafely() {
        requestRepository.save(testRequest);

        StatusTrackingResponseDto response = statusInquiryService.trackRequest("RTI-2026-999001");

        assertThat(response).isNotNull();
        assertThat(response.getTrackingId()).isEqualTo("RTI-2026-999001");
        assertThat(response.getHistory()).isNotNull().isEmpty();
    }
}
