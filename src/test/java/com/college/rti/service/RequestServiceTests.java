package com.college.rti.service;

import com.college.rti.dto.RequestSubmissionDto;
import com.college.rti.dto.SubmissionResponseDto;
import com.college.rti.model.RequestStatus;
import com.college.rti.model.RtiRequest;
import com.college.rti.model.StatusHistory;
import com.college.rti.repository.RtiRequestRepository;
import com.college.rti.repository.StatusHistoryRepository;
import com.college.rti.repository.TrackingSequenceRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class RequestServiceTests {

    @Autowired
    private RequestService requestService;

    @Autowired
    private RtiRequestRepository requestRepository;

    @Autowired
    private StatusHistoryRepository historyRepository;

    @Autowired
    private TrackingSequenceRepository sequenceRepository;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        requestRepository.deleteAll();
        sequenceRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully ingest valid RTI request and create initial status history atomically")
    void shouldSuccessfullyIngestValidRequest() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "Jane Doe",
                "jane.doe@example.com",
                "9876543210",
                "ACADEMIC",
                "Requesting official syllabus for Computer Science 2026."
        );

        SubmissionResponseDto response = requestService.submitRequest(dto);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTrackingId()).startsWith("RTI-");
        assertThat(response.getStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(response.getCategory()).isEqualTo("ACADEMIC");
        assertThat(response.getSubmittedAt()).isNotNull();

        // Verify RtiRequest persisted in DB
        Optional<RtiRequest> saved = requestRepository.findByTrackingId(response.getTrackingId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getApplicantName()).isEqualTo("Jane Doe");
        assertThat(saved.get().getApplicantEmail()).isEqualTo("jane.doe@example.com");

        // Verify StatusHistory persisted in DB
        List<StatusHistory> history = historyRepository.findByRequestTrackingIdOrderByTimestampAsc(response.getTrackingId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getFromStatus()).isNull();
        assertThat(history.get(0).getToStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(history.get(0).getActionBy()).isEqualTo("APPLICANT");
    }

    @Test
    @DisplayName("Should reject submission with invalid email format")
    void shouldRejectInvalidEmail() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "Jane Doe",
                "invalid-email-format",
                "9876543210",
                "ACADEMIC",
                "Requesting official syllabus for Computer Science 2026."
        );

        assertThatThrownBy(() -> requestService.submitRequest(dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Must be a well-formed email address");

        assertThat(requestRepository.count()).isZero();
        assertThat(historyRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should reject submission with invalid phone number")
    void shouldRejectInvalidPhone() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "Jane Doe",
                "jane@example.com",
                "12345", // Not 10 digits
                "ACADEMIC",
                "Requesting official syllabus for Computer Science 2026."
        );

        assertThatThrownBy(() -> requestService.submitRequest(dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Phone number must be a valid 10-digit number");

        assertThat(requestRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should reject submission with description under 20 characters")
    void shouldRejectShortDescription() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "Jane Doe",
                "jane@example.com",
                "9876543210",
                "ACADEMIC",
                "Too short"
        );

        assertThatThrownBy(() -> requestService.submitRequest(dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Description must be between 20 and 2000 characters");

        assertThat(requestRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should reject submission with blank applicant name")
    void shouldRejectBlankApplicantName() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "   ",
                "jane@example.com",
                "9876543210",
                "ACADEMIC",
                "Requesting official syllabus for Computer Science 2026."
        );

        assertThatThrownBy(() -> requestService.submitRequest(dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Applicant name is required");

        assertThat(requestRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should accept phone number prefixed with valid +91 country code")
    void shouldAcceptPhoneWithPlus91Prefix() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "John Smith",
                "john.smith@example.com",
                "+919876543210",
                "EXAMINATION",
                "Requesting marks re-evaluation criteria for Semester 5."
        );

        SubmissionResponseDto response = requestService.submitRequest(dto);

        assertThat(response.isSuccess()).isTrue();
        RtiRequest saved = requestRepository.findByTrackingId(response.getTrackingId()).orElseThrow();
        assertThat(saved.getApplicantPhone()).isEqualTo("+919876543210");
    }

    @Test
    @DisplayName("Should reject phone number starting with digit lower than 6")
    void shouldRejectPhoneStartingWithInvalidDigit() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "John Smith",
                "john.smith@example.com",
                "5876543210", // starts with 5
                "EXAMINATION",
                "Requesting marks re-evaluation criteria for Semester 5."
        );

        assertThatThrownBy(() -> requestService.submitRequest(dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Phone number must be a valid 10-digit number");
    }

    @Test
    @DisplayName("Should reject submission with blank category")
    void shouldRejectBlankCategory() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "John Smith",
                "john.smith@example.com",
                "9876543210",
                "   ",
                "Requesting marks re-evaluation criteria for Semester 5."
        );

        assertThatThrownBy(() -> requestService.submitRequest(dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Request category is required");
    }

    @Test
    @DisplayName("Should reject description exceeding 2000 characters")
    void shouldRejectLongDescription() {
        String longDescription = "A".repeat(2001);
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "John Smith",
                "john.smith@example.com",
                "9876543210",
                "ACADEMIC",
                longDescription
        );

        assertThatThrownBy(() -> requestService.submitRequest(dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Description must be between 20 and 2000 characters");
    }

    @Test
    @DisplayName("Should trim strings and lowercase applicant email upon persistence")
    void shouldTrimAndNormalizeInputFields() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "   Jane Doe   ",
                "JANE.DOE@Example.COM",
                "9876543210",
                "  ACADEMIC  ",
                "   Requesting official syllabus for Computer Science 2026.   "
        );

        SubmissionResponseDto response = requestService.submitRequest(dto);

        RtiRequest saved = requestRepository.findByTrackingId(response.getTrackingId()).orElseThrow();
        assertThat(saved.getApplicantName()).isEqualTo("Jane Doe");
        assertThat(saved.getApplicantEmail()).isEqualTo("jane.doe@example.com");
        assertThat(saved.getCategory()).isEqualTo("ACADEMIC");
        assertThat(saved.getDescription()).isEqualTo("Requesting official syllabus for Computer Science 2026.");
    }
}
