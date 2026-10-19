package com.college.rti.service;

import com.college.rti.dto.PublicStatusHistoryDto;
import com.college.rti.dto.StatusTrackingResponseDto;
import com.college.rti.exception.RequestNotFoundException;
import com.college.rti.model.RequestStatus;
import com.college.rti.model.RtiRequest;
import com.college.rti.model.StatusHistory;
import com.college.rti.repository.RtiRequestRepository;
import com.college.rti.repository.StatusHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class StatusInquiryServiceUnitTest {

    @Mock
    private RtiRequestRepository requestRepository;

    @Mock
    private StatusHistoryRepository historyRepository;

    @InjectMocks
    private StatusInquiryService statusInquiryService;

    @Test
    @DisplayName("Unit: Should retrieve request, load chronological history, and map to sanitized public DTO")
    void shouldRetrieveAndMapSanitizedPublicDto() {
        String trackingId = "RTI-2026-112233";
        RtiRequest mockRequest = new RtiRequest(
                trackingId,
                "Secret Applicant",
                "secret@example.com",
                "9876543210",
                "EXAMINATION",
                "Exam details request"
        );
        mockRequest.setId(101L);
        mockRequest.setCurrentStatus(RequestStatus.UNDER_REVIEW);
        mockRequest.setCreatedAt(LocalDateTime.of(2026, 1, 15, 10, 0));

        StatusHistory h1 = new StatusHistory(mockRequest, null, RequestStatus.SUBMITTED, "APPLICANT", "Initial submit");
        h1.setTimestamp(LocalDateTime.of(2026, 1, 15, 10, 0));
        StatusHistory h2 = new StatusHistory(mockRequest, RequestStatus.SUBMITTED, RequestStatus.UNDER_REVIEW, "REVIEWER:officer", "Under review");
        h2.setTimestamp(LocalDateTime.of(2026, 1, 16, 11, 30));

        when(requestRepository.findByTrackingId(trackingId)).thenReturn(Optional.of(mockRequest));
        when(historyRepository.findByRequestIdOrderByTimestampAsc(101L)).thenReturn(List.of(h1, h2));

        StatusTrackingResponseDto response = statusInquiryService.trackRequest(trackingId);

        assertThat(response.getTrackingId()).isEqualTo(trackingId);
        assertThat(response.getCategory()).isEqualTo("EXAMINATION");
        assertThat(response.getStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(response.getSubmittedAt()).isEqualTo(LocalDateTime.of(2026, 1, 15, 10, 0));

        List<PublicStatusHistoryDto> history = response.getHistory();
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getFromStatus()).isNull();
        assertThat(history.get(0).getToStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(history.get(0).getRemarks()).isEqualTo("Initial submit");
        assertThat(history.get(1).getFromStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(history.get(1).getToStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
        assertThat(history.get(1).getRemarks()).isEqualTo("Under review");

        verify(requestRepository).findByTrackingId(trackingId);
        verify(historyRepository).findByRequestIdOrderByTimestampAsc(101L);
    }

    @Test
    @DisplayName("Unit: Should throw RequestNotFoundException when request repository returns empty")
    void shouldThrowWhenRequestNotFound() {
        String trackingId = "RTI-2026-000000";
        when(requestRepository.findByTrackingId(trackingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statusInquiryService.trackRequest(trackingId))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessageContaining(trackingId);

        verify(historyRepository, never()).findByRequestIdOrderByTimestampAsc(anyLong());
    }

    @Test
    @DisplayName("Unit: Should throw RequestNotFoundException immediately for null or blank tracking IDs without DB query")
    void shouldThrowForNullOrBlankTrackingIdWithoutDbQuery() {
        assertThatThrownBy(() -> statusInquiryService.trackRequest(null))
                .isInstanceOf(RequestNotFoundException.class);

        assertThatThrownBy(() -> statusInquiryService.trackRequest(""))
                .isInstanceOf(RequestNotFoundException.class);

        assertThatThrownBy(() -> statusInquiryService.trackRequest("   "))
                .isInstanceOf(RequestNotFoundException.class);

        verify(requestRepository, never()).findByTrackingId(any());
    }

    private Long anyLong() {
        return org.mockito.ArgumentMatchers.anyLong();
    }
}
