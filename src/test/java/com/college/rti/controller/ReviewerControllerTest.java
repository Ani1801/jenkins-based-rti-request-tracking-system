package com.college.rti.controller;

import com.college.rti.dto.DecisionRequestDto;
import com.college.rti.dto.ReviewDecisionResponseDto;
import com.college.rti.dto.ReviewerQueueItemDto;
import com.college.rti.exception.InvalidStateTransitionException;
import com.college.rti.exception.RequestNotFoundException;
import com.college.rti.model.RequestStatus;
import com.college.rti.model.RtiRequest;
import com.college.rti.repository.RtiRequestRepository;
import com.college.rti.service.ReviewerWorkflowService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewerController.class)
class ReviewerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewerWorkflowService reviewerWorkflowService;

    @MockBean
    private RtiRequestRepository requestRepository;

    @Test
    @DisplayName("GET /api/v1/reviewer/requests: returns active reviewer queue items")
    void testGetReviewerQueue() throws Exception {
        ReviewerQueueItemDto item = new ReviewerQueueItemDto(
                1L,
                "RTI-2026-000001",
                "Alice",
                "alice@example.com",
                "9876543210",
                "INFRASTRUCTURE",
                "Hostel maintenance query",
                null,
                null,
                RequestStatus.SUBMITTED,
                LocalDateTime.now()
        );

        when(reviewerWorkflowService.getPendingReviewerQueue()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/reviewer/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].trackingId").value("RTI-2026-000001"))
                .andExpect(jsonPath("$[0].currentStatus").value("SUBMITTED"));
    }

    @Test
    @DisplayName("GET /api/v1/reviewer/requests/{id}: returns full details when found")
    void testGetRequestDetailsFound() throws Exception {
        RtiRequest req = new RtiRequest(
                "RTI-2026-000001",
                "Alice",
                "alice@example.com",
                "9876543210",
                "INFRASTRUCTURE",
                "Hostel maintenance query"
        );
        req.setCurrentStatus(RequestStatus.SUBMITTED);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));

        mockMvc.perform(get("/api/v1/reviewer/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId").value("RTI-2026-000001"))
                .andExpect(jsonPath("$.applicantName").value("Alice"));
    }

    @Test
    @DisplayName("GET /api/v1/reviewer/requests/{id}: throws 404 when not found")
    void testGetRequestDetailsNotFound() throws Exception {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/reviewer/requests/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("REQUEST_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/reviewer/requests/{id}/decision: valid transition returns HTTP 200")
    void testProcessDecisionValid() throws Exception {
        DecisionRequestDto dto = new DecisionRequestDto(
                RequestStatus.UNDER_REVIEW,
                "officer_sharma",
                "Transitioning application to review"
        );

        ReviewDecisionResponseDto responseDto = new ReviewDecisionResponseDto(
                true,
                1L,
                "RTI-2026-000001",
                RequestStatus.SUBMITTED,
                RequestStatus.UNDER_REVIEW,
                "officer_sharma",
                LocalDateTime.now(),
                "Request RTI-2026-000001 successfully transitioned to UNDER_REVIEW"
        );

        when(reviewerWorkflowService.processDecision(eq(1L), eq(RequestStatus.UNDER_REVIEW), eq("officer_sharma"), any()))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/reviewer/requests/1/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.newStatus").value("UNDER_REVIEW"));
    }

    @Test
    @DisplayName("POST /api/v1/reviewer/requests/{id}/decision: invalid transition returns HTTP 400")
    void testProcessDecisionInvalidTransition() throws Exception {
        DecisionRequestDto dto = new DecisionRequestDto(
                RequestStatus.APPROVED,
                "officer_sharma",
                "Skipping triage"
        );

        when(reviewerWorkflowService.processDecision(eq(1L), eq(RequestStatus.APPROVED), eq("officer_sharma"), any()))
                .thenThrow(new InvalidStateTransitionException(RequestStatus.SUBMITTED, RequestStatus.APPROVED));

        mockMvc.perform(post("/api/v1/reviewer/requests/1/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STATE_TRANSITION"));
    }
}
