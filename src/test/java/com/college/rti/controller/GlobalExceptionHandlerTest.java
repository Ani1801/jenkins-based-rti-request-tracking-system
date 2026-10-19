package com.college.rti.controller;

import com.college.rti.exception.InvalidStateTransitionException;
import com.college.rti.exception.RequestNotFoundException;
import com.college.rti.model.RequestStatus;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleRequestNotFound: returns 404 with REQUEST_NOT_FOUND")
    void testHandleRequestNotFound() {
        RequestNotFoundException ex = new RequestNotFoundException("RTI-123");
        ResponseEntity<Map<String, Object>> response = handler.handleRequestNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo("REQUEST_NOT_FOUND");
    }

    @Test
    @DisplayName("handleInvalidStateTransition: returns 400 with INVALID_STATE_TRANSITION")
    void testHandleInvalidStateTransition() {
        InvalidStateTransitionException ex = new InvalidStateTransitionException(RequestStatus.SUBMITTED, RequestStatus.APPROVED);
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidStateTransition(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo("INVALID_STATE_TRANSITION");
        assertThat(response.getBody().get("currentStatus")).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(response.getBody().get("targetStatus")).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    @DisplayName("handleConstraintViolation: returns 400 with VALIDATION_FAILED")
    void testHandleConstraintViolation() {
        ConstraintViolationException ex = new ConstraintViolationException("Invalid field", Set.of());
        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("handleIllegalArgument: returns 400 with BAD_REQUEST")
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Username blank");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().get("message")).isEqualTo("Username blank");
    }
}
