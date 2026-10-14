package com.college.rti.exception;

import com.college.rti.model.RequestStatus;

/**
 * Exception thrown when an illegal or unsupported state transition is requested.
 */
public class InvalidStateTransitionException extends RuntimeException {

    private final RequestStatus currentStatus;
    private final RequestStatus targetStatus;

    public InvalidStateTransitionException(RequestStatus currentStatus, RequestStatus targetStatus) {
        super(String.format("Invalid state transition from %s to %s", currentStatus, targetStatus));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public RequestStatus getCurrentStatus() {
        return currentStatus;
    }

    public RequestStatus getTargetStatus() {
        return targetStatus;
    }
}
