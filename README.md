# Jenkins-Based RTI Request Tracking System

> **ACADEMIC SIMULATION DISCLAIMER**  
> This project is an **educational engineering simulation** designed exclusively for coursework, faculty evaluation, and demonstration of automated CI/CD practices. It is **not** an official government portal, does not connect to any official government RTI service (e.g., `rtionline.gov.in`), and possesses no legal standing under the Right to Information Act, 2005.

---

## 1. Project Overview

The **Jenkins-Based RTI Request Tracking System** is a full-lifecycle web application and automated DevOps pipeline. It demonstrates modern continuous integration, automated browser regression, containerization, and configuration management by simulating a public administrative workflow:
- **Citizens** file administrative information inquiries and track their progression using non-colliding tracking identifiers (`RTI-YYYY-XXXXXX`).
- **Administrative Reviewers** access a secured dashboard to review submissions, inspect attached documents, and approve or reject applications with mandatory remarks.
- **Audit Logging** provides an immutable chronological timeline of state transitions.
- **DevOps Toolchain** automates compilation, unit testing, Selenium end-to-end regression, Docker container packaging, and Ansible deployment orchestration.

---

## 2. Core MVP Features

1. **Public Request Ingestion:** Web form with client/server validation for applicant details and optional document uploads ($\le$ 2 MB PDF/PNG/JPG).
2. **Collision-Free Tracking IDs:** Formatted alphanumeric tokens generated atomically upon submission.
3. **Public Status Tracking:** Privacy-safe status lookup view showing application progression without exposing personal contact details.
4. **Chronological Audit History:** Append-only timeline recording all state changes and reviewer remarks.
5. **Reviewer Triage Dashboard:** Authenticated interface to inspect queues, download documents, and record decisions (`APPROVED` / `REJECTED`).
6. **Telemetry & Health Monitoring:** Spring Boot Actuator diagnostic endpoints (`/actuator/health`) for automated deployment gating.

---

## 3. Technology Stack

- **Backend:** Java 17 LTS, Spring Boot 3.x, Spring Data JPA, Spring Boot Actuator
- **Build Engine:** Apache Maven 3.8+
- **Persistence:** MySQL 8.x
- **Reverse Proxy:** Nginx
- **Continuous Integration:** Jenkins LTS (Declarative Pipeline)
- **Automated Testing:** JUnit 5, Mockito, Selenium WebDriver (Headless Chrome)
- **Containerization:** Docker & Docker Compose
- **Configuration Management:** Ansible (via Linux / WSL2)
- **Source Control:** Git & GitHub

---

## 4. Architecture Summary

The system follows a classic **Three-Tier Architecture** fronted by an Nginx reverse proxy:
```
[Applicant / Reviewer Browser]
           │
           ▼
[Nginx Reverse Proxy :80] ───► [Spring Boot App (Embedded Tomcat) :8080]
                                      │
                                      ├──► [MySQL 8.x Database :3306]
                                      └──► [Local Volume /var/rti/uploads]
```
The automated CI/CD lifecycle is orchestrated by **Jenkins**:
$$\text{Git Push} \rightarrow \text{Maven Build} \rightarrow \text{JUnit/Mockito Tests} \rightarrow \text{Selenium E2E Tests} \rightarrow \text{Docker Build} \rightarrow \text{Ansible Deploy} \rightarrow \text{Health Check}$$

---

## 5. Documentation Directory

Detailed project documentation is available in the [`docs/`](docs/) directory:

- [docs/task-01-problem-definition.md](docs/task-01-problem-definition.md) — Problem definition, scope boundaries, 15 MVP tasks, and acceptance criteria.
- [docs/task-02-agile-planning.md](docs/task-02-agile-planning.md) — Agile methodology, roles, 10 user stories, DoR, DoD, and traceability.
- [docs/product-backlog.md](docs/product-backlog.md) — Prioritized product backlog with the 15 approved MVP tasks (`MVP-01` to `MVP-15`).
- [docs/kanban-board.md](docs/kanban-board.md) — Kanban board with WIP limits and transition state machine.
- [docs/sprint-plan.md](docs/sprint-plan.md) — Proposed 3-sprint implementation schedule.
- [docs/devops-workflow.md](docs/devops-workflow.md) — CI/CD workflow lifecycle diagram and stage definitions.
- [docs/task-03-requirements-architecture.md](docs/task-03-requirements-architecture.md) — SRS, use cases, ER diagram, API specification, and technology setup.

---

## 6. Prerequisites and Planned Setup

Before building or running the project, verify that the required development tools are installed:

- **Java Development Kit (JDK):** Version 17 LTS
- **Apache Maven:** Version 3.8+
- **Git:** Version 2.30+
- **Docker Engine & Docker Compose:** Docker Engine 24+
- **WSL2 / Linux Environment:** Required for Ansible control node execution on Windows

### Verification Commands (PowerShell)
```powershell
java -version
mvn -version
git --version
docker --version
docker compose version
```

---

## 7. Planned Build, Test, and Run Commands

> *Note: Application source code implementation begins in subsequent project tasks.*

```powershell
# 1. Clean and compile
mvn clean compile

# 2. Run unit and integration test suites
mvn clean test

# 3. Package executable JAR (skipping tests)
mvn clean package -DskipTests

# 4. Run application locally with environment parameters
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="rti_db"
$env:DB_USER="rti_user"
$env:DB_PASSWORD="<SECURE_PASSWORD>"
java -jar target/rti-request-tracking-system-1.0.0-SNAPSHOT.jar

# 5. Launch full stack via Docker Compose
docker compose up -d --build

# 6. Verify health endpoint
curl http://localhost:8080/actuator/health
```

---

## 8. Development and Git Branching Workflow

This project adheres to a structured branch policy:

| Branch Pattern | Purpose | Merging Policy |
|---|---|---|
| `main` | Production-ready stable release baseline. | Protected; PR only; requires passing CI build & reviewer approval. |
| `develop` | Active integration branch for sprint work. | Protected; PR only; merges feature branches. |
| `feature/<short-desc>` | Development of specific MVP backlog tasks. | Branched from `develop`; PR into `develop`. |
| `fix/<short-desc>` | Bug fixes and remediation tasks. | Branched from `develop` or `main`; PR into target. |

### Commit Message Convention
Commits must include structured prefixes referencing task IDs:
- `docs: <message>` for documentation updates
- `feat(MVP-XX): <message>` for new functional features
- `test(MVP-XX): <message>` for test additions or updates
- `fix(MVP-XX): <message>` for defect resolution
- `ci: <message>` for pipeline, Docker, or Ansible updates

---

## 9. Current Project Status

- **Task 1 (Problem Definition & Scope):** Completed & Approved.
- **Task 2 (Agile Planning & DevOps Workflow):** Completed & Approved.
- **Task 3 (Requirements & Architecture):** Completed & Approved.
- **Task 4 (Git Repository & Baseline Setup):** In Progress.
- **Application Implementation & CI/CD Pipelines:** Not yet started / unverified. (Scheduled for Tasks 5+).
