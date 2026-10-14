package com.college.rti.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
    name = "review_decisions",
    indexes = {
        @Index(name = "idx_review_decisions_request_id", columnList = "request_id")
    }
)
public class ReviewDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private RtiRequest request;

    @NotBlank
    @Size(max = 50)
    @Column(name = "reviewer_username", nullable = false, length = 50)
    private String reviewerUsername;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 30)
    private RequestStatus decision;

    @NotBlank
    @Column(name = "remarks", nullable = false, columnDefinition = "TEXT")
    private String remarks;

    @NotNull
    @Column(name = "decided_at", nullable = false, updatable = false)
    private LocalDateTime decidedAt;

    public ReviewDecision() {
    }

    public ReviewDecision(RtiRequest request, String reviewerUsername, RequestStatus decision, String remarks) {
        this.request = request;
        this.reviewerUsername = reviewerUsername;
        this.decision = decision;
        this.remarks = remarks;
    }

    @PrePersist
    protected void onCreate() {
        if (this.decidedAt == null) {
            this.decidedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RtiRequest getRequest() {
        return request;
    }

    public void setRequest(RtiRequest request) {
        this.request = request;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public void setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
    }

    public RequestStatus getDecision() {
        return decision;
    }

    public void setDecision(RequestStatus decision) {
        this.decision = decision;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReviewDecision that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
