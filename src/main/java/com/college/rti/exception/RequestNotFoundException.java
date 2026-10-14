package com.college.rti.exception;

/**
 * Exception thrown when an RTI request cannot be found for a given identifier.
 */
public class RequestNotFoundException extends RuntimeException {

    private final String trackingId;

    public RequestNotFoundException(String trackingId) {
        super(String.format("No RTI request found matching tracking identifier %s", trackingId));
        this.trackingId = trackingId;
    }

    public String getTrackingId() {
        return trackingId;
    }
}
