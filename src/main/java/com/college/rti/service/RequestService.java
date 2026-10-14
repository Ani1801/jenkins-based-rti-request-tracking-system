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
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service orchestrating RTI application ingestion, validation,
 * tracking ID generation, and atomic status-history auditing.
 */
@Service
public class RequestService {

    private final RtiRequestRepository requestRepository;
    private final StatusHistoryRepository historyRepository;
    private final TrackingIdGenerator trackingIdGenerator;
    private final Validator validator;

    public RequestService(RtiRequestRepository requestRepository,
                          StatusHistoryRepository historyRepository,
                          TrackingIdGenerator trackingIdGenerator,
                          Validator validator) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.trackingIdGenerator = trackingIdGenerator;
        this.validator = validator;
    }

    /**
     * Ingests a new RTI request atomically:
     * 1. Validates the submission DTO constraints.
     * 2. Generates a unique tracking ID.
     * 3. Persists the RtiRequest in SUBMITTED state.
     * 4. Persists the initial StatusHistory record.
     * 5. Returns a sanitized SubmissionResponseDto without sensitive applicant contact data.
     */
    @Transactional
    public SubmissionResponseDto submitRequest(RequestSubmissionDto dto) {
        // Step 1: Validate incoming DTO
        Set<ConstraintViolation<RequestSubmissionDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Invalid RTI request submission payload: ");
            for (ConstraintViolation<RequestSubmissionDto> v : violations) {
                sb.append(v.getPropertyPath()).append(" - ").append(v.getMessage()).append("; ");
            }
            throw new ConstraintViolationException(sb.toString().trim(), violations);
        }

        // Step 2: Generate unique tracking identifier (RTI-YYYY-XXXXXX)
        String trackingId = trackingIdGenerator.generateNextTrackingId();

        // Step 3: Create and persist RtiRequest entity
        RtiRequest request = new RtiRequest(
                trackingId,
                dto.getApplicantName().trim(),
                dto.getApplicantEmail().trim().toLowerCase(),
                dto.getApplicantPhone().trim(),
                dto.getCategory().trim(),
                dto.getDescription().trim()
        );
        request.setCurrentStatus(RequestStatus.SUBMITTED);
        RtiRequest savedRequest = requestRepository.save(request);

        // Step 4: Create and persist initial StatusHistory in the same atomic transaction
        StatusHistory initialHistory = new StatusHistory(
                savedRequest,
                null,
                RequestStatus.SUBMITTED,
                "APPLICANT",
                "Application submitted via public portal"
        );
        historyRepository.save(initialHistory);

        // Step 5: Return sanitized public response
        return new SubmissionResponseDto(
                true,
                savedRequest.getTrackingId(),
                savedRequest.getCurrentStatus(),
                savedRequest.getCategory(),
                savedRequest.getCreatedAt(),
                "Your RTI request has been submitted successfully. Please save your Tracking ID for future status inquiries."
        );
    }
}
