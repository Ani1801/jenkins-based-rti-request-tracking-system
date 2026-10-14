# Faculty Task 2: Agile Planning and DevOps Workflow

**Project Title:** Jenkins-Based RTI Request Tracking System  
**Document Version:** 1.0.0-PROPOSED  
**Baseline Reference:** [task-01-problem-definition.md](task-01-problem-definition.md)  
**Status:** Agile Governance & DevOps Baseline (Academic Project)  
**Date:** October 14, 2026  

---

## 1. Agile Methodology Selection and Justification

For the **Jenkins-Based RTI Request Tracking System**, an integrated **Scrum-Kanban hybrid (Scrumban)** methodology has been selected:

- **Time-Boxed Sprints (Scrum Foundation):** The academic evaluation cycle requires structured milestones and checkpoints. A three-sprint timeline provides predictable delivery increments suitable for college faculty reviews, with planned sprint demos at the end of each sprint.
- **Visual Flow and WIP Limits (Kanban Workflow):** DevOps and CI/CD pipelines require continuous feedback, peer reviews, and rigorous automated quality checks. Kanban boards with explicit Work-in-Progress (WIP) limits govern how work moves from `To Do` to `Done`, preventing bottleneck formation at code review and automated test validation stages.
- **Fast Feedback Loops:** Daily syncs and automated pipeline triggers ensure that regressions in code, broken container images, or failed Selenium test suites are immediately returned to development before merging.

---

## 2. Project Roles and Responsibilities

In an academic project setting, a small engineering team must fulfill distinct software lifecycle responsibilities. These roles represent **project governance functions** rather than segregated headcount; individual students can assume overlapping responsibilities:

| Project Role | Operational Governance Function | Primary Accountabilities |
|---|---|---|
| **Product Owner (PO)** | Scope guardian and stakeholder liaison | - Maintains and prioritizes the Product Backlog.<br>- Validates that user stories align with academic guidelines and scope constraints from Task 1.<br>- Signs off on sprint acceptance criteria and demo deliverables during Sprint Reviews. |
| **Development Team (Frontend / Backend)** | Software construction and unit verification | - Designs Spring Boot domain entities, repositories, and REST/MVC controllers.<br>- Develops responsive HTML/CSS citizen and reviewer web interfaces.<br>- Implements robust unit tests (JUnit 5, Mockito) and maintains clean code standards. |
| **Reviewer / QA Engineer** | Peer review and test automation | - Conducts peer code reviews on all GitHub Pull Requests against coding standards.<br>- Develops and executes automated browser test suites using Selenium WebDriver.<br>- Validates testability and verifiable error messages on web views. |
| **DevOps Engineer** | Automation pipeline and infrastructure orchestration | - Authors and maintains the Declarative `Jenkinsfile` for CI/CD stages.<br>- Configures multi-stage `Dockerfile` and `docker-compose.yml` definitions.<br>- Develops Ansible playbooks for automated host provisioning, deployment, and health verification. |

---

## 3. User Stories

The following 10 user stories define the user, administrative, and operational expectations of the system. Each story includes testable acceptance criteria formatted in Given-When-Then structure:

### US-01: Citizen Request Submission
- **Actor:** Citizen (Applicant)
- **Goal:** Submit a new RTI application online with personal details, subject, and description.
- **Business Value:** Enables citizens to file inquiries anytime without visiting physical offices.
- **Acceptance Criteria:**
  - *Given* a citizen accesses the submission page, *when* all required fields (name, email, phone, category, description) are validly populated, *then* the system persists the request with an initial status of `SUBMITTED`.
  - *Given* an applicant submits an invalid email or a description outside 20–2,000 characters, *then* the system displays descriptive field-level error messages and prevents database insertion.

### US-02: Supporting Document Attachment
- **Actor:** Citizen (Applicant)
- **Goal:** Attach an optional supporting document (PDF or image) during request submission.
- **Business Value:** Provides supporting evidence and context to substantiate the request.
- **Acceptance Criteria:**
  - *Given* an applicant uploads a valid `.pdf`, `.png`, or `.jpg` file under 2 MB, *when* submitting, *then* the file is stored securely on the server with a unique sanitized filename linked to the request.
  - *Given* an applicant uploads an unapproved file extension (e.g., `.exe`, `.sh`) or a file exceeding 2 MB, *then* the submission is rejected with an explicit validation error.

### US-03: Collision-Free Tracking ID Issuance
- **Actor:** Citizen (Applicant)
- **Goal:** Receive an immediate, unique tracking identifier upon successful application submission.
- **Business Value:** Delivers unambiguous proof of receipt and an index for subsequent progress checks.
- **Acceptance Criteria:**
  - *Given* a valid submission is committed, *when* the confirmation view renders, *then* a unique identifier matching the pattern `RTI-YYYY-XXXXXX` is presented prominently.
  - *Given* simultaneous submissions occur, *then* the database constraints guarantee zero duplicate tracking IDs.

### US-04: Public Real-Time Status Inquiry
- **Actor:** Citizen (Applicant)
- **Goal:** Track the current status of a submitted RTI request by entering the Tracking ID.
- **Business Value:** Eliminates administrative opacity and empowers applicants with self-service updates.
- **Acceptance Criteria:**
  - *Given* an applicant inputs a valid Tracking ID, *when* clicking "Track Status", *then* the system displays submission date, subject category, current state (`SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, or `REJECTED`), and reviewer remarks.
  - *Given* an applicant enters a non-existent Tracking ID, *then* the system displays an alert stating "No RTI request found for the provided Tracking ID".

### US-05: Complete Chronological Status Audit Trail
- **Actor:** Citizen / Auditor
- **Goal:** View the complete historical sequence of status transitions for a specific request.
- **Business Value:** Provides transparent procedural accountability and transition auditability.
- **Acceptance Criteria:**
  - *Given* a user views the tracking detail page, *when* status changes have occurred, *then* an audit log timeline is displayed showing transition timestamps, old state, new state, and associated remarks.

### US-06: Reviewer Queue Inspection
- **Actor:** Public Information Officer (Reviewer)
- **Goal:** View an authenticated dashboard displaying pending and active RTI submissions.
- **Business Value:** Allows administrative personnel to triage and prioritize processing queues efficiently.
- **Acceptance Criteria:**
  - *Given* an authenticated reviewer logs into the dashboard, *when* the view loads, *then* all requests are rendered in a tabular queue with filters for `SUBMITTED` and `UNDER_REVIEW`.
  - *Given* an unauthenticated user attempts to view the reviewer dashboard, *then* the system redirects to the login screen with an unauthorized warning.

### US-07: Reviewer Decision and Remarks Capture
- **Actor:** Public Information Officer (Reviewer)
- **Goal:** Transition an RTI application to `UNDER_REVIEW`, `APPROVED`, or `REJECTED` with mandatory remarks.
- **Business Value:** Enforces administrative accountability for final request resolutions.
- **Acceptance Criteria:**
  - *Given* an open request under review, *when* the reviewer selects `APPROVED` or `REJECTED` and inputs justification remarks ($\ge$ 10 characters), *then* the request state is updated and an immutable history entry is recorded.
  - *Given* a reviewer attempts to submit a decision without remarks, *then* the system rejects the update and prompts for justification.

### US-08: Automated Continuous Integration Verification
- **Actor:** DevOps Engineer / Developer
- **Goal:** Automatically compile, analyze, and test code on every commit pushed to GitHub.
- **Business Value:** Detects regressions early, preventing defective code from reaching main branches.
- **Acceptance Criteria:**
  - *Given* a new pull request or push to GitHub, *when* the Jenkins webhook triggers, *then* Jenkins checks out the branch, executes `mvn clean test`, and archives test/coverage reports.
  - *Given* any unit or integration test fails, *then* the build is marked as FAILED and notifications are posted.

### US-09: Automated End-to-End Browser Regression
- **Actor:** QA / Reviewer
- **Goal:** Validate core end-to-end user workflows using automated Selenium WebDriver tests in the pipeline.
- **Business Value:** Ensures UI interaction, form validation, and status transitions function cohesively in a real browser environment.
- **Acceptance Criteria:**
  - *Given* the application artifact is packaged, *when* the Jenkins pipeline executes the Selenium stage, *then* a headless Chrome runner files a mock RTI, tracks the generated ID, logs in as reviewer, and validates the updated status.
  - *Given* any selector breaks or expected DOM text is missing, *then* the pipeline halts before packaging.

### US-10: Automated Deployment and Diagnostic Health Verification
- **Actor:** DevOps Engineer / Evaluator
- **Goal:** Deploy the packaged Docker container via Ansible and verify operational readiness automatically.
- **Business Value:** Guarantees zero-touch, reproducible deployments verified by operational telemetry.
- **Acceptance Criteria:**
  - *Given* a successfully built Docker container image, *when* the Ansible deployment playbook executes, *then* target host dependencies are verified, old containers are replaced, and new containers are launched.
  - *Given* the application container starts, *when* the deployment stage polls `/actuator/health`, *then* an HTTP 200 response with `{"status":"UP"}` must be received within 30 seconds to pass the deployment gate.

---

## 4. Definition of Ready (DoR)

A product backlog item is deemed **Ready** to enter a sprint iteration when it satisfies the following preconditions:
1. **User Story Structure:** The story clearly identifies the actor, intent, business value, and specific acceptance criteria.
2. **Explicit Scope Boundary:** The story contains no dependencies on out-of-scope elements (e.g., government APIs, payment gateways).
3. **Traceability:** The story is mapped directly to one or more of the 15 approved MVP tasks from Task 1.
4. **Estimated:** Story points have been agreed upon by the development and DevOps roles.
5. **DOM / API Contract Specified:** Target endpoints, DTO models, or UI element IDs are identified so that testing plans can be formulated concurrently.
6. **No Blocking Preceding Tasks:** All architectural or schema prerequisites are completed and merged into the main branch.

---

## 5. Definition of Done (DoD)

A task or user story is deemed **Done** and acceptable for sprint review only when all of the following gates are satisfied:
1. **Code Complete:** Source code follows established naming standards and architectural layering (Controller $\rightarrow$ Service $\rightarrow$ Repository).
2. **Peer Review:** Code has undergone pull request review with approval from at least one reviewer.
3. **Unit & Integration Test Coverage:** Automated unit/integration tests pass with 0 failures, maintaining $\ge$ 70% coverage on core services.
4. **Static Code Quality:** Build compiles clean with 0 compiler warnings or linter errors.
5. **Selenium Compatibility:** Web views contain stable element IDs and pass headless browser test execution without flaky selectors.
6. **Containerization & Health:** Application builds cleanly inside a multi-stage Docker container and responds with HTTP 200 on `/actuator/health`.
7. **Documentation Synchronized:** README, API schema, and backlog statuses are updated to reflect the completed functionality.

---

## 6. Task Estimation Approach

Effort is estimated using modified **Fibonacci Story Points** (1, 2, 3, 5, 8, 13) based on relative complexity, risk, and verification overhead:

- **1 Point:** Trivial configuration or static view creation (e.g., standard Actuator health check exposure).
- **2 Points:** Straightforward entity/repository setup or read-only service queries.
- **3 Points:** Service logic with business validations, tracking generator algorithms, or transactional updates.
- **5 Points:** Multi-component feature involving UI forms, client-side validation, server persistence, and error handling.
- **8 Points:** High-complexity cross-cutting orchestration tasks, such as multi-stage CI pipeline definitions with Selenium browser execution or Ansible deployment orchestration.

*Estimated total project capacity across the 15 MVP tasks: 52 Story Points.*

---

## 7. Sprint Review and Retrospective Approach

### 7.1 Sprint Review (Demo Checkpoint)
At the conclusion of each sprint:
- **Demonstration:** Working software running either locally or in a containerized test environment is presented to faculty/peers.
- **Verification against Acceptance Criteria:** Each user story scheduled for the sprint is evaluated against its Given-When-Then criteria.
- **Backlog Grooming:** Outstanding items or feedback items are reprioritized for subsequent sprints.

### 7.2 Sprint Retrospective
Following the review, the team conducts a retrospective focusing on three questions:
1. *What went well in our development and CI automation?*
2. *Where did our pipeline or test suites fail or cause friction?*
3. *What specific actionable improvements will we implement in the next sprint?*

---

## 8. Risk and Dependency Management

| Dependency / Risk | Impact Area | Mitigation Strategy |
|---|---|---|
| **MySQL Container Availability** | Integration tests and local run failure | Use an embedded H2 database for unit/integration testing stages; use MySQL Docker containers for deployment environments. |
| **Selenium Headless Flakiness** | Jenkins build failure on UI stage | Use explicit WebDriverWait conditions rather than thread sleeps; configure Chrome with `--headless=new` and `--disable-gpu`. |
| **Windows vs. Linux Path Separators** | Docker volume mount and Ansible execution | Maintain POSIX-compliant paths in Dockerfiles and Compose files; execute Ansible in WSL2 or Docker-in-Docker containers. |
| **Port Conflicts (8080/3306)** | Application startup crash on shared hosts | Configure application container port to expose `8081` on host and MySQL to `3307` if local services exist. |

---

## 9. Traceability Matrix: User Stories to 15 MVP Implementation Tasks

The table below demonstrates 100% bidirectional traceability between the 10 User Stories and the exact 15 approved MVP tasks from [task-01-problem-definition.md](task-01-problem-definition.md):

| MVP Task ID | Task Name (from Task 1 Baseline) | Related User Story ID | Implementation Focus |
|---|---|---|---|
| **MVP-01** | Initialize Maven Project Structure and Dependencies | US-08 | Configure root build descriptor, plugins, and dependencies. |
| **MVP-02** | Design Relational Schema & Database Migrations | US-01, US-05, US-07 | Create DDL schemas, indexes, and initial reference tables. |
| **MVP-03** | Implement Core Domain Entities and Repository Layer | US-01, US-04, US-06 | JPA entities (`RtiRequest`, `StatusHistory`, `User`) and Spring Data repositories. |
| **MVP-04** | Implement Request Submission Service & Input Validation | US-01, US-02 | DTO bean validation, file upload validation, and persistence service. |
| **MVP-05** | Implement Unique Tracking ID Generation Logic | US-03 | Thread-safe, non-colliding formatted tracking identifier generator. |
| **MVP-06** | Implement Public Status Tracking & Audit History Service | US-04, US-05 | Read-only query service returning current status and chronological timeline. |
| **MVP-07** | Implement Reviewer Workflow & State Transition Engine | US-06, US-07 | Status state machine logic, mandatory remarks enforcement, and history logging. |
| **MVP-08** | Expose Actuator / Health Check Diagnostic Endpoints | US-10 | Health endpoint exposure and automated DB connectivity check. |
| **MVP-09** | Develop Citizen Web UI (Submission & Search Views) | US-01, US-02, US-04 | Responsive HTML/CSS views with element IDs for submission and lookup. |
| **MVP-10** | Develop Reviewer Dashboard UI with Action Modals | US-06, US-07 | Administrative table view, status action modals, and document download links. |
| **MVP-11** | Implement Comprehensive Unit and Integration Test Suite | US-01, US-04, US-07 | JUnit 5 and Mockito coverage across services, validators, and entities. |
| **MVP-12** | Build Automated Selenium WebDriver End-to-End Test Suite | US-09 | Headless browser tests simulating end-to-end citizen and reviewer interactions. |
| **MVP-13** | Author Multi-Stage Dockerfiles & Compose Manifests | US-10 | Multi-stage container builds and Docker Compose service definitions. |
| **MVP-14** | Write Declarative Jenkinsfile for Automated CI Pipeline | US-08, US-09 | Multi-stage Jenkins pipeline code automating checkout, build, test, and packaging. |
| **MVP-15** | Create Ansible Playbooks for Automated Deployment Provisioning | US-10 | Configuration management playbooks for Docker orchestration and health gating. |
