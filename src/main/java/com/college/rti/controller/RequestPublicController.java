package com.college.rti.controller;

import com.college.rti.dto.RequestSubmissionDto;
import com.college.rti.dto.StatusTrackingResponseDto;
import com.college.rti.dto.SubmissionResponseDto;
import com.college.rti.service.RequestService;
import com.college.rti.service.StatusInquiryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST Controller exposing citizen RTI request submission and status lookup endpoints.
 */
@RestController
@RequestMapping("/api/v1/requests")
public class RequestPublicController {

    private final RequestService requestService;
    private final StatusInquiryService statusInquiryService;

    public RequestPublicController(RequestService requestService, StatusInquiryService statusInquiryService) {
        this.requestService = requestService;
        this.statusInquiryService = statusInquiryService;
    }

    /**
     * Submit a new public RTI request.
     */
    @PostMapping
    public ResponseEntity<SubmissionResponseDto> submitRequest(@Valid @RequestBody RequestSubmissionDto dto) {
        SubmissionResponseDto response = requestService.submitRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Look up RTI request status and public history timeline by unique tracking identifier.
     */
    @GetMapping("/track/{trackingId}")
    public ResponseEntity<StatusTrackingResponseDto> trackRequest(@PathVariable String trackingId) {
        StatusTrackingResponseDto response = statusInquiryService.trackRequest(trackingId);
        return ResponseEntity.ok(response);
    }
}
