package com.college.rti.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for public citizen RTI application submission.
 */
public class RequestSubmissionDto {

    @NotBlank(message = "Applicant name is required")
    @Size(max = 100, message = "Applicant name must not exceed 100 characters")
    private String applicantName;

    @NotBlank(message = "Applicant email is required")
    @Email(message = "Must be a well-formed email address")
    @Size(max = 150, message = "Applicant email must not exceed 150 characters")
    private String applicantEmail;

    @NotBlank(message = "Applicant phone number is required")
    @Pattern(
        regexp = "^(\\+91)?[6-9]\\d{9}$",
        message = "Phone number must be a valid 10-digit number (optionally prefixed with +91)"
    )
    private String applicantPhone;

    @NotBlank(message = "Request category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @NotBlank(message = "Request description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;

    public RequestSubmissionDto() {
    }

    public RequestSubmissionDto(String applicantName, String applicantEmail, String applicantPhone,
                                String category, String description) {
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.applicantPhone = applicantPhone;
        this.category = category;
        this.description = description;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }

    public String getApplicantPhone() {
        return applicantPhone;
    }

    public void setApplicantPhone(String applicantPhone) {
        this.applicantPhone = applicantPhone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
