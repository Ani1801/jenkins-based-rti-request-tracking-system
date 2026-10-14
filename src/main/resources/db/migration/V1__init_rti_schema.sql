-- =========================================================================
-- Schema Migration: V1__init_rti_schema.sql
-- Jenkins-Based RTI Request Tracking System (Academic Simulation)
-- Tables: rti_requests, review_decisions, status_history
-- =========================================================================

-- 1. Table: rti_requests
CREATE TABLE IF NOT EXISTS rti_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_id VARCHAR(32) NOT NULL,
    applicant_name VARCHAR(100) NOT NULL,
    applicant_email VARCHAR(150) NOT NULL,
    applicant_phone VARCHAR(20) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    attachment_path VARCHAR(255) NULL,
    attachment_original_name VARCHAR(255) NULL,
    current_status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_rti_requests_tracking_id UNIQUE (tracking_id)
);

CREATE INDEX idx_rti_requests_tracking_id ON rti_requests (tracking_id);
CREATE INDEX idx_rti_requests_applicant_email ON rti_requests (applicant_email);
CREATE INDEX idx_rti_requests_current_status ON rti_requests (current_status);

-- 2. Table: review_decisions
CREATE TABLE IF NOT EXISTS review_decisions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    reviewer_username VARCHAR(50) NOT NULL,
    decision VARCHAR(30) NOT NULL,
    remarks TEXT NOT NULL,
    decided_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_decisions_request FOREIGN KEY (request_id)
        REFERENCES rti_requests (id) ON DELETE CASCADE
);

CREATE INDEX idx_review_decisions_request_id ON review_decisions (request_id);

-- 3. Table: status_history
CREATE TABLE IF NOT EXISTS status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    from_status VARCHAR(30) NULL,
    to_status VARCHAR(30) NOT NULL,
    action_by VARCHAR(50) NOT NULL,
    remarks TEXT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_status_history_request FOREIGN KEY (request_id)
        REFERENCES rti_requests (id) ON DELETE CASCADE
);

CREATE INDEX idx_status_history_request_id ON status_history (request_id);

-- 4. Table: rti_tracking_sequences
CREATE TABLE IF NOT EXISTS rti_tracking_sequences (
    sequence_year INT NOT NULL PRIMARY KEY,
    current_val BIGINT NOT NULL DEFAULT 0
);
