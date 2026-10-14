# Proposed Project Directory Structure

This document outlines the standard Maven multi-tier directory structure that will be established during subsequent implementation tasks (MVP-01).

```
rti-request-tracking-system/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── feature_request.yml
│   │   └── bug_report.yml
│   └── pull_request_template.md
├── docs/
│   ├── task-01-problem-definition.md
│   ├── task-02-agile-planning.md
│   ├── product-backlog.md
│   ├── kanban-board.md
│   ├── sprint-plan.md
│   ├── devops-workflow.md
│   ├── task-03-requirements-architecture.md
│   └── proposed-folder-structure.md
├── src/
│   ├── main/
│   │   ├── java/com/college/rti/
│   │   │   ├── RtiTrackingApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── RequestPublicController.java
│   │   │   │   └── ReviewerController.java
│   │   │   ├── service/
│   │   │   │   ├── RequestService.java
│   │   │   │   ├── TrackingIdGenerator.java
│   │   │   │   └── ReviewerService.java
│   │   │   ├── repository/
│   │   │   │   ├── RtiRequestRepository.java
│   │   │   │   ├── StatusHistoryRepository.java
│   │   │   │   └── ReviewDecisionRepository.java
│   │   │   ├── model/
│   │   │   │   ├── RtiRequest.java
│   │   │   │   ├── StatusHistory.java
│   │   │   │   ├── ReviewDecision.java
│   │   │   │   └── RequestStatus.java (Enum)
│   │   │   └── dto/
│   │   │       ├── RequestSubmissionDto.java
│   │   │       ├── StatusResponseDto.java
│   │   │       └── DecisionRequestDto.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-docker.yml
│   │       ├── db/migration/ (or schema.sql)
│   │       ├── static/ (CSS/JS assets)
│   │       └── templates/ (Thymeleaf/HTML citizen & reviewer views)
│   └── test/
│       ├── java/com/college/rti/
│       │   ├── unit/ (Service & validation unit tests)
│       │   ├── integration/ (Repository & Web layer integration tests)
│       │   └── selenium/ (Headless Chrome E2E browser tests)
│       └── resources/
│           └── application-test.yml
├── docker/
│   ├── nginx/
│   │   └── nginx.conf
│   └── Dockerfile
├── ansible/
│   ├── inventory/
│   │   └── hosts.yml
│   ├── roles/
│   └── deploy.yml
├── .gitignore
├── Jenkinsfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

> **Note:** Directory stubs and code files under `src/`, `docker/`, and `ansible/` will be generated in their respective MVP implementation tasks to maintain a clean Git history.
