package com.college.rti.controller;

import com.college.rti.dto.PublicStatusHistoryDto;
import com.college.rti.dto.RequestSubmissionDto;
import com.college.rti.dto.StatusTrackingResponseDto;
import com.college.rti.dto.SubmissionResponseDto;
import com.college.rti.exception.RequestNotFoundException;
import com.college.rti.model.RequestStatus;
import com.college.rti.service.RequestService;
import com.college.rti.service.StatusInquiryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestPublicController.class)
class RequestPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestService requestService;

    @MockBean
    private StatusInquiryService statusInquiryService;

    @Test
    @DisplayName("POST /api/v1/requests: valid payload returns HTTP 201 with tracking ID")
    void testSubmitRequestValid() throws Exception {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "Jane Doe",
                "jane@example.com",
                "9876543210",
                "ACADEMIC",
                "Requesting undergraduate transcript records from university."
        );

        SubmissionResponseDto responseDto = new SubmissionResponseDto(
                true,
                "RTI-2026-123456",
                RequestStatus.SUBMITTED,
                "ACADEMIC",
                LocalDateTime.now(),
                "Success"
        );

        when(requestService.submitRequest(any(RequestSubmissionDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.trackingId").value("RTI-2026-123456"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("POST /api/v1/requests: invalid payload returns HTTP 400 validation error")
    void testSubmitRequestInvalid() throws Exception {
        RequestSubmissionDto dto = new RequestSubmissionDto(
                "", // Blank name
                "invalid-email",
                "123",
                "",
                "short"
        );

        mockMvc.perform(post("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("GET /api/v1/requests/track/{id}: found request returns HTTP 200 with public history")
    void testTrackRequestFound() throws Exception {
        PublicStatusHistoryDto historyItem = new PublicStatusHistoryDto(
                null,
                RequestStatus.SUBMITTED,
                LocalDateTime.now(),
                "Initial submission"
        );

        StatusTrackingResponseDto trackingDto = new StatusTrackingResponseDto(
                "RTI-2026-123456",
                "ACADEMIC",
                RequestStatus.SUBMITTED,
                LocalDateTime.now(),
                List.of(historyItem)
        );

        when(statusInquiryService.trackRequest("RTI-2026-123456")).thenReturn(trackingDto);

        mockMvc.perform(get("/api/v1/requests/track/RTI-2026-123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId").value("RTI-2026-123456"))
                .andExpect(jsonPath("$.category").value("ACADEMIC"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.history[0].toStatus").value("SUBMITTED"));
    }

    @Test
    @DisplayName("GET /api/v1/requests/track/{id}: unknown request returns HTTP 404")
    void testTrackRequestNotFound() throws Exception {
        when(statusInquiryService.trackRequest("RTI-UNKNOWN"))
                .thenThrow(new RequestNotFoundException("RTI-UNKNOWN"));

        mockMvc.perform(get("/api/v1/requests/track/RTI-UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("REQUEST_NOT_FOUND"));
    }
}
