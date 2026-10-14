# Faculty Task 1: Problem Definition and Scope Baseline

**Project Title:** Jenkins-Based RTI Request Tracking System  
**Document Version:** 1.0.0-DRAFT  
**Status:** Frozen Scope Baseline (Academic Project)  
**Date:** October 14, 2026  

---

## 1. Project Title and Overview

### 1.1 Project Title
**Jenkins-Based Right to Information (RTI) Request Tracking System**

### 1.2 System Overview
The **Jenkins-Based RTI Request Tracking System** is an educational, end-to-end DevOps demonstration project. It couples a full-lifecycle web application—built to simulate public administrative workflows under a simplified Right to Information (RTI) paradigm—with an automated Continuous Integration and Continuous Deployment (CI/CD) pipeline managed via Jenkins.

The primary aim of this project is twofold:
1. Provide a realistic multi-tier web application domain where citizens can file information requests, track review lifecycle events via unique alphanumeric tracking tokens, and where designated reviewers can triage, approve, or reject submissions with historical auditing.
2. Serve as the concrete application under test and deployment for an automated DevOps pipeline incorporating automated build verification, unit/integration testing, functional browser test suites (Selenium WebDriver), container packaging (Docker), and configuration/deployment orchestration (Ansible).

---

## 2. Problem Statement

### 2.1 Context and Real-World Friction
The Right to Information mechanism represents a cornerstone of civic transparency. However, practical RTI implementations often encounter procedural bottlenecks:
- **Lack of Transparent Status Visibility:** Citizens face uncertainty regarding whether an application has been received, assigned, reviewed, or resolved.
- **Traceability Gaps:** Manual or poorly digitized tracking systems lack immutable status progression audit trails, making accountability difficult to verify.
- **Data Integrity and Validation Overhead:** Reviewers frequently encounter incomplete or improperly formatted requests, causing unnecessary back-and-forth communication.

### 2.2 Pedagogical DevOps Need
In academic computer science and software engineering curricula, DevOps principles (continuous integration, automated test gates, containerized runtime parity, automated configuration management) are often taught abstractly using trivial "Hello World" micro-services. Such minimal targets fail to expose students to:
- Multi-tier state persistence (database migrations and relational consistency).
- Asynchronous operational health telemetry and endpoint readiness.
- End-to-end test execution where real UI flows (form submission, validation warnings, reviewer dashboards) must pass automated gates before artifacts reach target environments.

Simulating an RTI tracking workflow provides a bounded yet authentic operational model featuring user roles, state machine transitions, strict validation rules, and lifecycle auditing, thereby providing an ideal candidate for demonstrating modern automated DevOps workflows.

---

## 3. Scope Boundary and Academic Nature

### 3.1 Academic Simulation Declaration
> **IMPORTANT NOTICE:**  
> This system is strictly an **academic engineering simulation** created for coursework, evaluation, and pedagogical DevOps demonstrations. It is **not** an official public portal, does not interface with any government portal (such as `rtionline.gov.in` or state-specific equivalents), and is legally and operationally incapable of processing binding legal requests under the Right to Information Act, 2005 or any other regulatory framework.

### 3.2 Operational Boundaries
- The application will run entirely within simulated local, virtualized, or isolated educational lab infrastructure.
- Citizen identification, address records, and submitted queries are considered mock demonstration data. No Personally Identifiable Information (PII) or sensitive government communications should be entered or processed.
- No legally enforceable statutory deadlines, official government appeals, or regulatory oversight procedures are enacted by this software.

---

## 4. Stakeholder Roles and Responsibilities

| Stakeholder Role | Description | Core Responsibilities in System Lifecycle |
|---|---|---|
| **Citizen (Applicant)** | Public user seeking information under the simulated RTI framework. | - Submits RTI request with personal details (mock), category, description, and supporting document attachments.<br>- Receives and stores a unique alphanumeric Tracking ID.<br>- Checks real-time request status and chronological history via the public tracking portal. |
| **Public Information Officer (Reviewer / Admin)** | Administrative official responsible for reviewing and processing submissions. | - Accesses an authenticated review interface.<br>- Inspects pending applications, validated inputs, and uploaded documents.<br>- Updates request status (Approve, Reject, Request Clarification, Close) with mandatory reviewer remarks. |
| **DevOps Engineer (Student Developer Team)** | Engineering team designing, provisioning, and maintaining the system. | - Manages source code repository and branching strategies.<br>- Configures and maintains Jenkins CI/CD pipelines, Docker container images, and Ansible playbooks.<br>- Implements automated build, test execution, and deployment verification scripts. |
| **Faculty / Evaluator** | Academic instructor and project evaluator. | - Reviews architectural artifacts, documentation baselines, and code quality.<br>- Assesses automated pipeline execution and test coverage reports.<br>- Evaluates project milestones against predefined success criteria. |

---

## 5. Project Objectives

1. **Simulate a Robust Core RTI Lifecycle:** Implement a deterministic state-machine workflow covering submission, unique tracking identifier generation, reviewer triage, state transition updates, and chronological audit history.
2. **Implement Automated Continuous Integration:** Establish an automated pipeline triggered by code repository updates that executes static analysis, compilation, and automated test suites without manual intervention.
3. **Execute Automated Quality Gates:** Integrate unit, integration, and UI acceptance tests (Selenium WebDriver) directly into the delivery pipeline to halt builds on regression.
4. **Standardize Runtime Environments with Containerization:** Package the web application and its dependencies into standardized Docker images to eliminate environment divergence between development and deployment targets.
5. **Automate Environment Configuration and Deployment:** Use Ansible playbooks to provision target host prerequisites and orchestrate zero-touch deployment of the application containers.
6. **Provide Observability and Health Telemetry:** Implement standardized operational health endpoints (`/actuator/health` or `/api/health`) to enable automated post-deployment smoke verification.

---

## 6. Measurable Success Criteria and Verification Methods

| # | Measurable Success Criteria | Target Threshold | Verification Method |
|---|---|---|---|
| **SC-01** | Automated Build & Packaging | Clean compilation and JAR packaging with 0 errors | Jenkins pipeline console output from `mvn clean package` |
| **SC-02** | Unit & Integration Test Coverage | Minimum 70% line coverage across service and domain layers | Maven Surefire/JaCoCo coverage reports archived in Jenkins |
| **SC-03** | End-to-End Automated Regression | 100% pass rate on core UI test paths (Submit $\rightarrow$ Track $\rightarrow$ Review) | Headless Selenium WebDriver test execution stage in pipeline |
| **SC-04** | Container Build Reproducibility | Multi-stage Docker image builds with consistent digest tag generation | Docker build logs and image inspection via automated pipeline stage |
| **SC-05** | Configuration Management Idempotence | Ansible playbooks run cleanly across multiple passes | Ansible playbook execution logs verifying `changed=0` on repeated runs |
| **SC-06** | System Health Verification | HTTP 200 OK with `UP` status from health endpoint within 30 seconds of launch | Automated cURL or HTTP probe in deployment verification stage |
| **SC-07** | Tracking ID Uniqueness & Lookups | Zero collisions across generated tracking IDs; 100% retrieval rate | Automated database constraint tests and integration test suites |

---

## 7. Functional Scope

The functional scope of the core application is constrained to essential workflow primitives:

```
+------------------+         Submit Form          +-----------------------------+
|                  | ---------------------------> |  Input Validation & Storage |
|  Public Citizen  |                              +-----------------------------+
|                  | <--------------------------- | Generates Unique RTI-XXXXXX |
|                  |      Returns Tracking ID     +-----------------------------+
|                  |
|                  |         Enters ID            +-----------------------------+
|                  | ---------------------------> | Fetch Status & Audit Trail  |
+------------------+                              +-----------------------------+

+------------------+        Authenticate          +-----------------------------+
| Reviewer / PIO   | ---------------------------> | Review Dashboard (Pending)  |
|                  |                              +-----------------------------+
|                  |        Review & Update       +-----------------------------+
|                  | ---------------------------> | Transition: APPROVED/REJECT |
+------------------+       Remarks Logged         +-----------------------------+

+------------------+        Health Probe          +-----------------------------+
| Jenkins / Monitor| ---------------------------> | GET /api/health (HTTP 200)  |
+------------------+                              +-----------------------------+
```

### 7.1 Request Submission
- Public web form permitting citizens to input applicant name, email, contact number, subject category, and narrative request text.
- Support for an optional single document upload (PDF/PNG/JPEG) representing supporting documents (stored locally in an application-managed directory or volume).

### 7.2 Input and Document Validation
- Client-side and strict server-side validation on all form fields:
  - Required field completeness.
  - Email format compliance and contact phone number formatting.
  - Character bounds (e.g., description length between 20 and 2,000 characters).
  - Document file type restriction (e.g., `.pdf`, `.png`, `.jpg`) and strict size enforcement (maximum 2 MB).

### 7.3 Unique Tracking ID Generation
- Deterministic, collision-free tracking identifier generated upon successful database insertion (format pattern: `RTI-YYYY-XXXXXX` where `XXXXXX` is a zero-padded sequential or cryptographic pseudorandom token).
- Immediate presentation of the tracking ID to the applicant upon successful submission confirmation.

### 7.4 Public Status Tracking
- Public search interface accepting a Tracking ID.
- Displays current status (`SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `CLOSED`).
- Displays public details: Submission Date, Subject Category, Current Status, and Public Remarks from the reviewer.

### 7.5 Status History and Audit Trail
- Chronological timeline tracking each lifecycle event:
  - Timestamp of state change.
  - Old state $\rightarrow$ New state transition.
  - Reviewer ID or actor role responsible for the change.
  - Accompanying operational remarks.

### 7.6 Reviewer Actions and Dashboard
- Basic authenticated portal for administrative reviewers.
- Tabular display of submitted requests filtered by status (defaulting to `SUBMITTED` / `UNDER_REVIEW`).
- Modal or detail view permitting the reviewer to:
  - Transition status to `UNDER_REVIEW`, `APPROVED`, or `REJECTED`.
  - Enter mandatory reviewer justification remarks.
  - Download/inspect uploaded supporting documents.

### 7.7 System Health Checks
- Dedicated unauthenticated diagnostic endpoints (e.g., `/actuator/health` or `/api/health`) returning JSON payload indicating application readiness and database connectivity status.

---

## 8. Non-Functional Requirements (NFRs)

### 8.1 Security
- **Role-Based Authorization:** Separate public access (submission, tracking) from reviewer access (dashboard, state changes) using basic session- or token-based authentication.
- **Input Sanitization:** Prevent SQL Injection via parameterized queries (Spring Data JPA) and Cross-Site Scripting (XSS) via HTML entity escaping.
- **File Upload Security:** Enforce strict file extension whitelisting, MIME type validation, and file renaming upon disk write to eliminate path traversal vulnerabilities.
- **Credential Hygiene:** Avoid hardcoding database credentials or administrative secrets in source control; utilize externalized configuration or environment variables.

### 8.2 Maintainability
- **Layered Architecture:** Clear separation of concerns (Controller, Service, Repository, DTO/Entity layers).
- **Standardized Build Tooling:** Maven-standard project directory structure and lifecycle phases.
- **Infrastructure as Code (IaC):** Modular Ansible roles/playbooks and documented Dockerfiles.

### 8.3 Usability
- **Responsive Layout:** Clean, intuitive UI built with standard HTML/CSS rendering properly on standard desktop viewports (1280x720 minimum).
- **Clear Error Messaging:** Explicit, human-readable form validation alerts and 404/Invalid ID feedback.

### 8.4 Reliability
- **Transactional State Integrity:** Database state transitions and history logging must execute within atomic transactions (`@Transactional`).
- **Graceful Failure Handling:** Catch database connection errors and present standard error responses rather than unhandled stack traces.

### 8.5 Testability
- **Decoupled Architecture:** Business logic isolatable in unit tests using mock objects (Mockito).
- **Predictable DOM Selectors:** HTML templates must feature unique and stable DOM element IDs (`id="submit-rti-btn"`, `id="tracking-id-input"`, etc.) specifically designed to facilitate robust Selenium WebDriver locators.

---

## 9. Constraints, Assumptions, Dependencies, and Risks

### 9.1 Technical Constraints
- **Host Platform:** Development and simulated target runtime hosted on Windows x64 workstation environment using PowerShell and virtualized/containerized sub-environments.
- **Tech Stack Baseline:**
  - **Backend:** Java 17 LTS, Spring Boot 3.x
  - **Build Engine:** Apache Maven 3.8+
  - **Database:** MySQL 8.x
  - **Version Control:** Git & GitHub
  - **Continuous Integration:** Jenkins LTS
  - **Automated Testing:** JUnit 5, Mockito, Selenium WebDriver
  - **Containerization:** Docker & Docker Compose
  - **Configuration Orchestration:** Ansible

### 9.2 Project Assumptions
- The system will be evaluated within an isolated local or private network subnet.
- The volume of concurrent requests will remain low (< 50 simultaneous users), representative of an academic project demonstration.
- Reviewer authentication can utilize a pre-seeded administrative user database rather than external SSO or LDAP.

### 9.3 System Dependencies
- Docker Engine / Docker Desktop runtime available on the evaluation host.
- Jenkins master instance configured with Java, Maven, and Docker pipeline agent capabilities.
- Chrome or Chromium headless driver binaries available for Selenium test runner nodes.

### 9.4 Risk Register

| Risk ID | Risk Description | Severity | Likelihood | Mitigation Strategy |
|---|---|---|---|---|
| **R-01** | Jenkins agent lack of headless browser support breaking Selenium stage | High | Medium | Execute Selenium tests inside a containerized browser environment or configure Chrome in explicit headless mode (`--headless=new`, `--no-sandbox`). |
| **R-02** | Ansible execution hurdles on native Windows hosts | Medium | High | Target Linux-based Docker containers or WSL2 for Ansible control-node / managed-node execution. |
| **R-03** | Database schema desynchronization across pipeline runs | High | Medium | Utilize Flyway/Liquibase database migration scripts or clean schema initialization scripts per pipeline test run. |
| **R-04** | Port collision on host machine (MySQL: 3306, App: 8080, Jenkins: 8080) | Medium | High | Explicitly remap host container port bindings (e.g., App to 8081 or 8090, Jenkins on 8080). |

---

## 10. Frozen MVP Implementation Backlog (15 Tasks)

The following 15 tasks represent the complete, frozen backlog for the Minimum Viable Product (MVP). No additional features will be admitted until all 15 tasks are completed and verified.

```
+-------------------------------------------------------------------------------+
|                        FROZEN MVP BACKLOG ROADMAP                             |
+-------------------------------------------------------------------------------+
| [Phase 1: Project Setup & Domain Modeling]                                    |
|   1. Initialize Maven Project Structure and Dependencies                     |
|   2. Design Relational Schema & Database Migrations                           |
|   3. Implement Core Domain Entities and Repository Layer                     |
|                                                                               |
| [Phase 2: Core Logic & Application Layer]                                     |
|   4. Implement Request Submission Service & Input Validation                 |
|   5. Implement Unique Tracking ID Generation Logic                           |
|   6. Implement Public Status Tracking & Audit History Service                |
|   7. Implement Reviewer Workflow & State Transition Engine                   |
|   8. Expose Actuator / Health Check Diagnostic Endpoints                     |
|                                                                               |
| [Phase 3: Web UI & Automated Testing]                                         |
|   9. Develop Citizen Web UI (Submission & Search Views)                      |
|  10. Develop Reviewer Dashboard UI with Action Modals                         |
|  11. Implement Comprehensive Unit and Integration Test Suite                  |
|  12. Build Automated Selenium WebDriver End-to-End Test Suite                |
|                                                                               |
| [Phase 4: CI/CD, Containerization & Orchestration]                            |
|  13. Author Multi-Stage Dockerfiles & Compose Manifests                      |
|  14. Write Declarative Jenkinsfile for Automated CI Pipeline                  |
|  15. Create Ansible Playbooks for Automated Deployment Provisioning           |
+-------------------------------------------------------------------------------+
```

### Detailed Task Specifications

1. **TASK-01: Initialize Maven Project Structure and Dependencies**  
   Configure `pom.xml` with Spring Boot Starter Web, Starter Data JPA, Starter Validation, Starter Actuator, MySQL Connector, Lombok, and test dependencies.
2. **TASK-02: Design Relational Schema & Database Migrations**  
   Define DDL scripts for `rti_requests`, `request_status_history`, and `reviewer_users` tables including foreign keys, indexes, and enum state definitions.
3. **TASK-03: Implement Core Domain Entities and Repository Layer**  
   Write JPA entities (`RtiRequest`, `StatusHistory`, `User`) and corresponding Spring Data repositories with query methods for tracking ID resolution.
4. **TASK-04: Implement Request Submission Service & Input Validation**  
   Create submission DTOs with bean validation annotations (`@NotBlank`, `@Size`, `@Email`) and a service method to persist incoming requests with local document storage.
5. **TASK-05: Implement Unique Tracking ID Generation Logic**  
   Develop a thread-safe, formatted tracking ID generator service guaranteeing non-colliding identifiers (e.g., `RTI-2026-XXXXXX`).
6. **TASK-06: Implement Public Status Tracking & Audit History Service**  
   Create read-only lookup endpoints querying request details and chronological transition history by Tracking ID.
7. **TASK-07: Implement Reviewer Workflow & State Transition Engine**  
   Develop service logic enforcing valid status transitions (`SUBMITTED` $\rightarrow$ `UNDER_REVIEW` $\rightarrow$ `APPROVED`/`REJECTED`) and automatically appending records to `request_status_history`.
8. **TASK-08: Expose Actuator / Health Check Diagnostic Endpoints**  
   Configure Spring Boot Actuator to expose `/actuator/health` and verify database connectivity reporting.
9. **TASK-09: Develop Citizen Web UI (Submission & Search Views)**  
   Create clean HTML/CSS views for the public citizen: an application submission form with client-side validation and a tracking lookup view.
10. **TASK-10: Develop Reviewer Dashboard UI with Action Modals**  
    Create reviewer-facing views displaying pending requests, detailed view modals, and status update actions with remark capture.
11. **TASK-11: Implement Comprehensive Unit and Integration Test Suite**  
    Write JUnit 5 and Mockito tests covering validation rules, tracking generator, and state transition logic with an in-memory/test database.
12. **TASK-12: Build Automated Selenium WebDriver End-to-End Test Suite**  
    Develop automated browser tests covering the full critical user path: filing a request, retrieving the tracking ID, checking status, and updating status via the reviewer screen.
13. **TASK-13: Author Multi-Stage Dockerfiles & Compose Manifests**  
    Create optimized Dockerfile utilizing multi-stage builds (Maven build stage + minimal JRE runtime image) and a `docker-compose.yml` linking the application and MySQL service.
14. **TASK-14: Write Declarative Jenkinsfile for Automated CI Pipeline**  
    Define a multi-stage Jenkins pipeline: Checkout $\rightarrow$ Build $\rightarrow$ Unit/Integration Test $\rightarrow$ Selenium E2E Test $\rightarrow$ Docker Package $\rightarrow$ Artifact Archival.
15. **TASK-15: Create Ansible Playbooks for Automated Deployment Provisioning**  
    Write Ansible playbooks and inventory templates to automate Docker host setup, pull freshly built container images, and launch the application stack with health check verification.

---

## 11. Explicit Out-of-Scope Features

To prevent scope creep and maintain alignment with academic deadlines, the following capabilities are explicitly classified as **Out of Scope**:

- **Government API Integration:** No connectivity, data exchange, or authentication with real-world Indian government portals (`rtionline.gov.in`, DigiLocker, Aadhaar API, etc.).
- **Payment Gateway Processing:** No monetary transactions, treasury challans, or online payment gateways (e.g., Razorpay, UPI, Bharatkosh); mock applications are processed free of charge.
- **Optical Character Recognition (OCR) and Content Extraction:** No automated parsing or content inspection of uploaded documents; attachments are treated as static binary files.
- **Advanced Machine Learning / NLP Analytics:** No automated classification, sentiment analysis, or predictive request routing.
- **Multi-Factor Authentication (MFA) & Complex Identity Providers:** No OAuth2/OIDC integration or SMS OTP verification; reviewer access uses standard credential checks.
- **SMS / Automated External Email Gateways:** No third-party notification services (Twilio, SendGrid); notifications are limited to web interface displays and audit log entries.
- **Multi-Tenancy & Cross-Departmental Routing:** The system models a single department simulation and does not support complex inter-ministerial transfers.

---

## 12. Task 1 Acceptance Checklist

This checklist defines the completion criteria for Faculty Task 1 before progressing to architecture design and implementation:

- [x] **Clear Project Title and Scope Defined:** Explicitly titled "Jenkins-Based RTI Request Tracking System" and documented as an academic simulation.
- [x] **Problem Statement Formulated:** Clearly articulates both the domain workflow friction and the pedagogical DevOps requirements.
- [x] **Academic Boundary Enforced:** Clear disclaimer confirming non-integration with real-world government services.
- [x] **Stakeholders Documented:** Roles and operational responsibilities specified for Citizen, Reviewer, DevOps Team, and Faculty.
- [x] **Quantifiable Objectives & Success Criteria:** 7 measurable criteria with concrete verification methods established.
- [x] **Functional Scope Itemized:** Submission, validation, tracking ID, status checking, history audit, reviewer triage, and health checks detailed.
- [x] **NFRs Specified:** Security, maintainability, usability, reliability, and testability constraints established.
- [x] **Constraints and Risks Cataloged:** Technical stack identified, dependencies enumerated, and risk register populated.
- [x] **Frozen Backlog Defined:** Exactly 15 structured, numbered, sequential implementation tasks established.
- [x] **Out-of-Scope Guardrails Established:** 7 key enterprise features explicitly demarcated as out-of-scope to ensure project delivery within schedule.
- [x] **No premature implementation claimed:** No software installation, repository setup, or execution results fabricated.
