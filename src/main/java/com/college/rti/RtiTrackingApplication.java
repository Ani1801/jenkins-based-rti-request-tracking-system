package com.college.rti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Jenkins-Based RTI Request Tracking System.
 * Academic simulation of a Right to Information (RTI) administrative workflow.
 */
@SpringBootApplication
public class RtiTrackingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RtiTrackingApplication.class, args);
    }
}
