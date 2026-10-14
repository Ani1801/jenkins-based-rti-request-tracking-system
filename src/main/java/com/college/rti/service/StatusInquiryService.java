package com.college.rti.service;

import com.college.rti.dto.PublicStatusHistoryDto;
import com.college.rti.dto.StatusTrackingResponseDto;
import com.college.rti.exception.RequestNotFoundException;
import com.college.rti.model.RtiRequest;
import com.college.rti.model.StatusHistory;
import com.college.rti.repository.RtiRequestRepository;
import com.college.rti.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service providing public RTI request status inquiry and audit timeline retrieval.
 * Enforces privacy boundaries by stripping applicant PII and internal credentials.
 */
@Service
public class StatusInquiryService {

    private final RtiRequestRepository requestRepository;
    private final StatusHistoryRepository historyRepository;

    public StatusInquiryService(RtiRequestRepository requestRepository,
                                StatusHistoryRepository historyRepository) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Looks up an RTI application by its public Tracking ID and maps it to a sanitized response DTO.
     *
     * @param trackingId Public tracking token (e.g. RTI-YYYY-XXXXXX)
     * @return Sanitized StatusTrackingResponseDto
     * @throws RequestNotFoundException if no request matches the tracking ID
     */
    @Transactional(readOnly = true)
    public StatusTrackingResponseDto trackRequest(String trackingId) {
        if (trackingId == null || trackingId.trim().isEmpty()) {
            throw new RequestNotFoundException(trackingId);
        }

        String normalizedId = trackingId.trim();

        // Step 1: Retrieve RtiRequest by trackingId
        RtiRequest request = requestRepository.findByTrackingId(normalizedId)
                .orElseThrow(() -> new RequestNotFoundException(normalizedId));

        // Step 2: Retrieve chronological status history
        List<StatusHistory> histories = historyRepository.findByRequestIdOrderByTimestampAsc(request.getId());

        // Step 3: Map to public-safe history projection
        List<PublicStatusHistoryDto> historyDtos = histories.stream()
                .map(this::toPublicHistoryDto)
                .collect(Collectors.toList());

        // Step 4: Construct and return response omitting all applicant PII
        return new StatusTrackingResponseDto(
                request.getTrackingId(),
                request.getCategory(),
                request.getCurrentStatus(),
                request.getCreatedAt(),
                historyDtos
        );
    }

    private PublicStatusHistoryDto toPublicHistoryDto(StatusHistory history) {
        return new PublicStatusHistoryDto(
                history.getFromStatus(),
                history.getToStatus(),
                history.getTimestamp(),
                history.getRemarks()
        );
    }
}
