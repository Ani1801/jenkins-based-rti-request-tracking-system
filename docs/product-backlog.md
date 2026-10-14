# Product Backlog: Jenkins-Based RTI Request Tracking System

**Document Version:** 1.0.0-PROPOSED  
**Baseline Reference:** [task-01-problem-definition.md](task-01-problem-definition.md)  
**Status:** Frozen MVP Backlog (All items currently `To Do`)  
**Date:** October 14, 2026  

---

## 1. Prioritization Rationale
The backlog prioritizes foundation and persistence capabilities first, followed by core business transaction logic, public/reviewer presentation layers, automated testing gates, and finally containerization and automated DevOps pipeline orchestration.

No tasks have commenced; all 15 tasks are initially marked **`To Do`**.

---

## 2. Frozen MVP Implementation Backlog Table

| Task ID | Task Name (Task 1 Baseline) | Related User Story ID | Priority | Story Points | Dependencies | Acceptance Criteria | Verification Method | Status |
|---|---|---|---|---|---|---|---|---|
| **MVP-01** | Initialize Maven Project Structure and Dependencies | US-08 | Must | 2 | None | Project compiles cleanly with Java 17, Spring Boot 3.x dependencies (Web, Data JPA, Validation, Actuator, MySQL, Lombok, Test). | `mvn clean compile` succeeds with zero errors in local terminal. | To Do |
| **MVP-02** | Design Relational Schema & Database Migrations | US-01, US-05, US-07 | Must | 3 | MVP-01 | DDL scripts define `rti_requests`, `request_status_history`, and `reviewer_users` with primary keys, indexes, and foreign keys. | SQL scripts execute against MySQL instance without syntax or constraint errors. | To Do |
| **MVP-03** | Implement Core Domain Entities and Repository Layer | US-01, US-04, US-06 | Must | 3 | MVP-01, MVP-02 | JPA entities mapped to database schema; Spring Data JPA repositories expose lookup by Tracking ID and status query methods. | Unit tests verify entity persistence and CRUD repository execution against H2. | To Do |
| **MVP-04** | Implement Request Submission Service & Input Validation | US-01, US-02 | Must | 5 | MVP-02, MVP-03 | Service validates applicant details (name, email format, phone, description 20–2000 chars) and file attachment ($\le$ 2MB PDF/PNG/JPG). | Unit tests verify validation rejection on malformed inputs and successful storage on valid payloads. | To Do |
| **MVP-05** | Implement Unique Tracking ID Generation Logic | US-03 | Must | 3 | MVP-03 | Generates collision-free tracking IDs matching pattern `RTI-YYYY-XXXXXX` and associates them atomically with submissions. | Automated test generates 1,000 IDs verifying 100% uniqueness and pattern conformity. | To Do |
| **MVP-06** | Implement Public Status Tracking & Audit History Service | US-04, US-05 | Must | 3 | MVP-03, MVP-05 | Service returns current request status, metadata, and full chronological status audit log for a given Tracking ID. | Integration tests verify JSON response contains accurate timeline matching seeded history. | To Do |
| **MVP-07** | Implement Reviewer Workflow & State Transition Engine | US-06, US-07 | Must | 5 | MVP-03, MVP-06 | Service enforces valid state transitions (`SUBMITTED` $\rightarrow$ `UNDER_REVIEW` $\rightarrow$ `APPROVED`/`REJECTED`) with mandatory justification remarks. | Service unit tests verify invalid transitions are blocked and valid transitions append audit records. | To Do |
| **MVP-08** | Expose Actuator / Health Check Diagnostic Endpoints | US-10 | Should | 1 | MVP-01 | `/actuator/health` endpoint responds with HTTP 200 and JSON payload verifying database connectivity and service state. | Automated HTTP GET probe to `/actuator/health` returns status `UP`. | To Do |
| **MVP-09** | Develop Citizen Web UI (Submission & Search Views) | US-01, US-02, US-04 | Must | 5 | MVP-04, MVP-05, MVP-06 | Clean, responsive HTML/CSS views for application submission and tracking search with unique DOM IDs for testing. | Browser renders pages cleanly; form inputs accept values and display error messages. | To Do |
| **MVP-10** | Develop Reviewer Dashboard UI with Action Modals | US-06, US-07 | Must | 5 | MVP-07 | Reviewer interface displaying pending request queues, detailed view modals, and status update submission forms. | Manual inspection of UI in browser validating tabular queue rendering and action modal popups. | To Do |
| **MVP-11** | Implement Comprehensive Unit and Integration Test Suite | US-01, US-04, US-07 | Must | 5 | MVP-04, MVP-05, MVP-06, MVP-07 | Test suite achieves $\ge$ 70% line coverage across service and validation layers using JUnit 5 and Mockito. | `mvn test` execution generates JaCoCo/Surefire reports demonstrating $\ge$ 70% coverage. | To Do |
| **MVP-12** | Build Automated Selenium WebDriver End-to-End Test Suite | US-09 | Should | 5 | MVP-09, MVP-10 | Automated headless Chrome test suite executes end-to-end journey: submit request $\rightarrow$ track ID $\rightarrow$ update reviewer status. | `mvn verify` runs headless Selenium suite with 100% passing tests without flaky failures. | To Do |
| **MVP-13** | Author Multi-Stage Dockerfiles & Compose Manifests | US-10 | Should | 3 | MVP-01, MVP-08 | Multi-stage Dockerfile packages Spring Boot application on minimal JRE; `docker-compose.yml` links App and MySQL. | `docker compose up -d` boots both services successfully and application reaches `UP` state. | To Do |
| **MVP-14** | Write Declarative Jenkinsfile for Automated CI Pipeline | US-08, US-09 | Must | 5 | MVP-11, MVP-12, MVP-13 | Jenkins pipeline automates stages: Checkout $\rightarrow$ Build $\rightarrow$ Unit Test $\rightarrow$ Selenium Test $\rightarrow$ Docker Package $\rightarrow$ Archive. | Jenkins pipeline execution completes with Blue/Green build status and archived artifacts. | To Do |
| **MVP-15** | Create Ansible Playbooks for Automated Deployment Provisioning | US-10 | Could | 4 | MVP-13, MVP-14 | Playbooks configure host prerequisites, pull images, deploy containers, and run health check verification probes. | `ansible-playbook -i inventory deploy.yml` completes with `failed=0` and health probe verified. | To Do |

---

## 3. Summary of Backlog Metrics
- **Total MVP Tasks:** Exactly 15 tasks (100% matched with Task 1).
- **Total Estimated Story Points:** 52 Points.
- **Priority Distribution:**
  - **Must Have:** 11 tasks (39 Points) — Essential domain workflow, UI, unit tests, and CI pipeline.
  - **Should Have:** 3 tasks (9 Points) — Actuator health checks, Selenium browser automation, Docker containerization.
  - **Could Have:** 1 task (4 Points) — Ansible automated deployment provisioning.
