package com.college.rti.service;

import com.college.rti.dto.RequestSubmissionDto;
import com.college.rti.dto.SubmissionResponseDto;
import com.college.rti.model.RequestStatus;
import com.college.rti.model.RtiRequest;
import com.college.rti.model.StatusHistory;
import com.college.rti.repository.RtiRequestRepository;
import com.college.rti.repository.StatusHistoryRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceUnitTest {

    @Mock
    private RtiRequestRepository requestRepository;

    @Mock
    private StatusHistoryRepository historyRepository;

    @Mock
    private TrackingIdGenerator trackingIdGenerator;

    @Mock
    private Validator validator;

    @InjectMocks
    private RequestService requestService;

    @Test
    @DisplayName("Unit: Should submit valid request, persist request and history, and return sanitized DTO")
    void shouldSubmitValidRequestSuccessfully() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "Jane Doe",
                "Jane.Doe@Example.COM",
                "9876543210",
                "ACADEMIC",
                "Requesting syllabus copy for semester 4."
        );

        when(validator.validate(dto)).thenReturn(Collections.emptySet());
        when(trackingIdGenerator.generateNextTrackingId()).thenReturn("RTI-2026-000001");
        when(requestRepository.save(any(RtiRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(StatusHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubmissionResponseDto response = requestService.submitRequest(dto);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTrackingId()).isEqualTo("RTI-2026-000001");
        assertThat(response.getStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(response.getCategory()).isEqualTo("ACADEMIC");

        // Verify RtiRequest saved with normalized email and trimmed fields
        ArgumentCaptor<RtiRequest> reqCaptor = ArgumentCaptor.forClass(RtiRequest.class);
        verify(requestRepository).save(reqCaptor.capture());
        RtiRequest capturedReq = reqCaptor.getValue();
        assertThat(capturedReq.getApplicantName()).isEqualTo("Jane Doe");
        assertThat(capturedReq.getApplicantEmail()).isEqualTo("jane.doe@example.com");
        assertThat(capturedReq.getTrackingId()).isEqualTo("RTI-2026-000001");
        assertThat(capturedReq.getCurrentStatus()).isEqualTo(RequestStatus.SUBMITTED);

        // Verify initial StatusHistory saved
        ArgumentCaptor<StatusHistory> histCaptor = ArgumentCaptor.forClass(StatusHistory.class);
        verify(historyRepository).save(histCaptor.capture());
        StatusHistory capturedHist = histCaptor.getValue();
        assertThat(capturedHist.getRequest()).isEqualTo(capturedReq);
        assertThat(capturedHist.getFromStatus()).isNull();
        assertThat(capturedHist.getToStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(capturedHist.getActionBy()).isEqualTo("APPLICANT");
    }

    @Test
    @DisplayName("Unit: Should throw ConstraintViolationException and skip persistence when validator fails")
    @SuppressWarnings("unchecked")
    void shouldThrowExceptionWhenValidationFails() {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "",
                "invalid-email",
                "123",
                "",
                "short"
        );

        ConstraintViolation<RequestSubmissionDto> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("applicantEmail");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Must be a well-formed email address");
        when(validator.validate(dto)).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> requestService.submitRequest(dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Must be a well-formed email address");

        verify(trackingIdGenerator, never()).generateNextTrackingId();
        verify(requestRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }
}
