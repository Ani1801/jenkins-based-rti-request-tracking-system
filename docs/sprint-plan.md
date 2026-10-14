# Proposed Three-Sprint Plan: Jenkins-Based RTI Request Tracking System

**Document Version:** 1.0.0-PROPOSED  
**Baseline Reference:** [task-01-problem-definition.md](task-01-problem-definition.md) | [product-backlog.md](product-backlog.md)  
**Status:** Proposed Sprint Plan (Awaiting Faculty Review & Capacity Calibration)  
**Date:** October 14, 2026  

---

## 1. Sprint Planning Overview

This document outlines a **realistic, proposed three-sprint plan** designed to deliver the complete, frozen 15-task Minimum Viable Product (MVP). 

> **Notice:** This document constitutes a proposed scheduling roadmap. Exact sprint durations, start dates, and velocity estimates will be finalized following formal review and guidance from the project faculty mentor. No sprint work has commenced yet.

### Overall Sprint Allocation Summary

```
+-----------------------------------------------------------------------------------------+
|                               PROPOSED SPRINT TIMELINE                                  |
+-----------------------------------------------------------------------------------------+
| SPRINT 1: Application Foundation & Core Ingestion                                       |
| Tasks: MVP-01, MVP-02, MVP-03, MVP-04, MVP-05                                           |
| Points: 16 | Goal: Establish Maven build, schema, domain models, submission & tracking ID |
+-----------------------------------------------------------------------------------------+
| SPRINT 2: Business Workflow, Reviewer Operations & UI Presentation                      |
| Tasks: MVP-06, MVP-07, MVP-09, MVP-10                                                   |
| Points: 18 | Goal: Implement status tracking, reviewer approval/rejection & web UIs     |
+-----------------------------------------------------------------------------------------+
| SPRINT 3: Verification, Automation, Health Checks & Release Readiness                   |
| Tasks: MVP-08, MVP-11, MVP-12, MVP-13, MVP-14, MVP-15                                   |
| Points: 18 | Goal: Health checks, unit/Selenium suites, Docker, Jenkins CI & Ansible    |
+-----------------------------------------------------------------------------------------+
Total: 15 MVP Tasks | 52 Story Points
```

---

## 2. Sprint 1: Application Foundation, Git Workflow, and Request Ingestion

- **Sprint Goal:** Establish the engineering repository baseline, database persistence foundation, and the core request ingestion service that persists valid citizen RTI requests and generates collision-free tracking IDs.
- **Estimated Effort:** 16 Story Points
- **Planned Tasks:**

| Task ID | Task Name | Est. Pts | Dependencies | Sprint 1 Acceptance Criteria |
|---|---|---|---|---|
| **MVP-01** | Initialize Maven Project Structure and Dependencies | 2 | None | Root `pom.xml` configured with Spring Boot 3.x, JPA, Web, Actuator, MySQL, and Test libraries; project compiles with 0 errors. |
| **MVP-02** | Design Relational Schema & Database Migrations | 3 | MVP-01 | SQL DDL scripts create `rti_requests`, `request_status_history`, and `reviewer_users` tables with foreign keys and unique constraints. |
| **MVP-03** | Implement Core Domain Entities and Repository Layer | 3 | MVP-01, MVP-02 | JPA entities mapped with relational annotations; Spring Data repositories support basic queries by tracking ID and status. |
| **MVP-04** | Implement Request Submission Service & Input Validation | 5 | MVP-02, MVP-03 | Service validates input constraints (name, email, 20–2000 char description, $\le$ 2MB attachment) and saves `SUBMITTED` record. |
| **MVP-05** | Implement Unique Tracking ID Generation Logic | 3 | MVP-03 | Thread-safe ID generator formats identifiers as `RTI-YYYY-XXXXXX` and guarantees zero duplicates in multi-threaded simulation. |

- **Sprint Review & Demo Checkpoint:**
  - Demonstrate a clean `mvn clean compile` execution.
  - Execute a local test or script demonstrating database schema creation and sample entity persistence.
  - Demonstrate calling the ingestion service with a mock RTI payload, showing that a record is saved in the database with a uniquely generated `RTI-YYYY-XXXXXX` identifier.

---

## 3. Sprint 2: Request Validation, Reviewer Dashboard, and Status Tracking

- **Sprint Goal:** Implement the citizen search query service, reviewer state-machine transition engine, and both citizen and reviewer web interfaces to complete the interactive application lifecycle.
- **Estimated Effort:** 18 Story Points
- **Planned Tasks:**

| Task ID | Task Name | Est. Pts | Dependencies | Sprint 2 Acceptance Criteria |
|---|---|---|---|---|
| **MVP-06** | Implement Public Status Tracking & Audit History Service | 3 | MVP-03, MVP-05 | Service returns current state and full chronological audit log for any valid Tracking ID. |
| **MVP-07** | Implement Reviewer Workflow & State Transition Engine | 5 | MVP-03, MVP-06 | State transitions strictly enforced (`SUBMITTED` $\rightarrow$ `UNDER_REVIEW` $\rightarrow$ `APPROVED`/`REJECTED`); requires mandatory remarks. |
| **MVP-09** | Develop Citizen Web UI (Submission & Search Views) | 5 | MVP-04, MVP-05, MVP-06 | Responsive HTML views allowing citizen request submission and real-time tracking lookup with clear validation feedback. |
| **MVP-10** | Develop Reviewer Dashboard UI with Action Modals | 5 | MVP-07 | Reviewer interface displaying pending request table, detail views, and action forms to update status with remarks. |

- **Sprint Review & Demo Checkpoint:**
  - Demonstrate filing an RTI request via the Citizen Web UI.
  - Demonstrate retrieving that application using the Tracking ID on the public lookup page.
  - Demonstrate logging into the Reviewer Dashboard, inspecting the application, and changing the state to `APPROVED` with remarks.
  - Demonstrate that the public tracking view immediately reflects the updated status and exhibits the new chronological audit trail entry.

---

## 4. Sprint 3: Verification, Automation, Health Checks, and Release Readiness

- **Sprint Goal:** Implement diagnostic health monitoring, complete automated test suites (unit, integration, and Selenium E2E), and automate delivery via multi-stage Docker containerization, Jenkins CI pipelines, and Ansible deployment playbooks.
- **Estimated Effort:** 18 Story Points
- **Planned Tasks:**

| Task ID | Task Name | Est. Pts | Dependencies | Sprint 3 Acceptance Criteria |
|---|---|---|---|---|
| **MVP-08** | Expose Actuator / Health Check Diagnostic Endpoints | 1 | MVP-01 | `/actuator/health` exposes system readiness and MySQL connectivity returning HTTP 200 `UP`. |
| **MVP-11** | Implement Comprehensive Unit and Integration Test Suite | 5 | MVP-04, MVP-05, MVP-06, MVP-07 | Automated JUnit 5 / Mockito test suite achieves $\ge$ 70% line coverage across service and validation components. |
| **MVP-12** | Build Automated Selenium WebDriver End-to-End Test Suite | 5 | MVP-09, MVP-10 | Headless Selenium script runs complete lifecycle (submit $\rightarrow$ track $\rightarrow$ review) without errors in the pipeline. |
| **MVP-13** | Author Multi-Stage Dockerfiles & Compose Manifests | 3 | MVP-01, MVP-08 | Multi-stage Dockerfile produces lightweight JRE image; `docker-compose.yml` launches application and database. |
| **MVP-14** | Write Declarative Jenkinsfile for Automated CI Pipeline | 5 | MVP-11, MVP-12, MVP-13 | Jenkins pipeline automates checkout, build, unit testing, Selenium testing, Docker packaging, and artifact archival. |
| **MVP-15** | Create Ansible Playbooks for Automated Deployment Provisioning | 4 | MVP-13, MVP-14 | Playbooks configure host prerequisites, deploy Docker containers, and verify health check response. |

- **Sprint Review & Demo Checkpoint (Final Project Defense):**
  - Trigger a push or PR on GitHub and demonstrate the automated Jenkins pipeline execution through all stages.
  - Showcase automated Selenium test execution in headless mode passing 100% of functional tests.
  - Demonstrate Ansible playbook execution launching containerized services and asserting `UP` status from `/actuator/health`.
  - Present test coverage reports and operational logs to the faculty evaluator.

---

## 5. Summary of MVP Task Allocation Across Sprints

| Sprint | Task IDs Included | Count | Total Points |
|---|---|---|---|
| **Sprint 1** | MVP-01, MVP-02, MVP-03, MVP-04, MVP-05 | 5 | 16 |
| **Sprint 2** | MVP-06, MVP-07, MVP-09, MVP-10 | 4 | 18 |
| **Sprint 3** | MVP-08, MVP-11, MVP-12, MVP-13, MVP-14, MVP-15 | 6 | 18 |
| **Total** | **All 15 MVP Tasks Represented (0 Omitted, 0 Duplicated)** | **15** | **52** |
