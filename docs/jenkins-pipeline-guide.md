# Jenkins CI/CD Pipeline Guide: RTI Request Tracking System (MVP-14)

This guide documents the Continuous Integration and Continuous Delivery (CI/CD) pipeline for the **Jenkins-Based RTI Request Tracking System** orchestrated via the declarative [`Jenkinsfile`](../Jenkinsfile).

---

## 1. Pipeline Overview & Architectural Lifecycle

The pipeline establishes an automated quality and packaging gate from source checkout to deployable artifact and container validation:

```
[ Git Checkout ]
       │
       ▼
[ Maven Build ] (mvn compile -B)
       │
       ▼
[ Unit & Integration Tests ] (mvn test -B) ──► Publish JUnit Test Reports
       │
       ▼
[ Selenium E2E Tests ] (mvn test -Pselenium) ──► Publish Selenium Reports (if Chrome present)
       │
       ▼
[ Package JAR ] (mvn package -DskipTests) ──► Archive target/*.jar Artifact
       │
       ▼
[ Docker Validation & Build ] (docker compose config && docker compose build)
       │
       ▼
[ Post-Build Reporting ] (Console status, notifications, cleanup)
```

---

## 2. Jenkins Agent Capabilities & Prerequisites

To execute the entire pipeline without warnings or skips, the Jenkins build agent should provide:

| Tool / Capability | Minimum Version | Pipeline Stage Used | Notes / Fallback Behavior |
|---|---|---|---|
| **Java Development Kit (JDK)** | OpenJDK / Eclipse Temurin 17 LTS | `Build`, `Tests`, `Package` | Required. Pipeline fails if `java`/`mvn` is missing. |
| **Apache Maven** | 3.8.x or 3.9.x | `Build`, `Tests`, `Package` | Required. Manages dependencies and build lifecycle. |
| **Google Chrome / Chromium** | Recent Stable Version | `Selenium E2E Tests` | If missing on agent, stage prints a warning and marks build `UNSTABLE` without breaking fast unit tests. |
| **Docker CLI & Compose** | Docker CLI 24.0+, Compose v2 | `Docker Validation` | Used to validate `docker compose config`. |
| **Docker Daemon Engine** | Active Docker daemon | `Docker Validation` | If daemon is offline, compose configuration syntax is still validated, and image build is marked `UNSTABLE`. |

---

## 3. Configuring the Jenkins Job

### Option A: Pipeline Job (Single Branch)

1. Open Jenkins Dashboard $\rightarrow$ **New Item**.
2. Enter Item Name: `rti-request-tracking-system`.
3. Select **Pipeline** and click **OK**.
4. In the job configuration:
   - Check **GitHub project** $\rightarrow$ Project url: `https://github.com/Ani1801/jenkins-based-rti-request-tracking-system`
   - Under **Build Triggers**, select **GitHub hook trigger for GITScm polling** (or Poll SCM).
   - Under **Pipeline**:
     - Definition: `Pipeline script from SCM`
     - SCM: `Git`
     - Repository URL: `https://github.com/Ani1801/jenkins-based-rti-request-tracking-system.git`
     - Credentials: None required for public repository. (If private, select standard Jenkins Username/Password or GitHub Personal Access Token credential).
     - Branch Specifier: `*/main` (or `*/feature/*` for feature verification).
     - Script Path: `Jenkinsfile`
5. Click **Save**.

### Option B: Multibranch Pipeline (Recommended for Feature Branches)

1. Open Jenkins Dashboard $\rightarrow$ **New Item** $\rightarrow$ **Multibranch Pipeline**.
2. Under **Branch Sources**, add **Git**:
   - Repository URL: `https://github.com/Ani1801/jenkins-based-rti-request-tracking-system.git`
3. Under **Build Configuration**:
   - Mode: `by Jenkinsfile`
   - Script Path: `Jenkinsfile`
4. Click **Save**. Jenkins automatically scans and indexes branches (`main`, `feature/*`) and executes builds upon new commits.

---

## 4. Pipeline Parameters

When triggering the job manually (**Build with Parameters**), the following options are available:

- `BRANCH_NAME` (String): Git branch or tag to build (default: `main`).
- `RUN_SELENIUM_TESTS` (Boolean, default: `true`): Executes headless Chrome Selenium browser tests. Set to `false` on lightweight agents lacking Chrome.
- `RUN_DOCKER_BUILD` (Boolean, default: `true`): Executes Docker compose validation and container image builds. Set to `false` on agents without Docker.

---

## 5. Test Reports & Archived Artifacts

### Test Reports
The pipeline automatically parses Surefire XML test reports from:
```
target/surefire-reports/*.xml
```
- Available in the Jenkins UI via **Test Result** (`/job/<name>/<build>/testReport/`).
- Shows pass/fail history, stack traces for any regressions, and execution durations.

### Archived Build Artifacts
Upon successful completion of the `Package` stage, Jenkins archives:
```
target/*.jar
```
- The production fat JAR (`rti-request-tracking-system-1.0.0-SNAPSHOT.jar`) is fingerprinted and available for download directly from the Jenkins build summary page under **Last Successful Artifacts**.

---

## 6. Docker Stage Behavior & Agent Requirements

The `Docker Validation & Build` stage implements a resilient multi-tier check:

1. **CLI Presence Check:** Verifies `docker` command is on the agent's `PATH`. If missing, the stage fails with an actionable error message.
2. **Configuration Validation:** Validates `docker compose config` using `.env.example` as a fallback if no local `.env` is present.
3. **Daemon Check:** Runs `docker info`.
   - If daemon is responsive $\rightarrow$ Runs `docker compose build`.
   - If daemon is offline $\rightarrow$ Warns in build console and sets stage status to `UNSTABLE`, allowing upstream Java artifacts and test reports to remain accessible.

---

## 7. Known Environment Limitations

In the local development environment:
- **Local Machine State:** Windows 11 host with Java 21/17 and Google Chrome.
- **Docker Daemon:** Docker Desktop CLI is present, but the Docker daemon engine is not active because Windows Subsystem for Linux (WSL) is not installed.
- **Static vs Live Execution:** The `Jenkinsfile` has been authored and statically verified. Jenkins pipeline execution occurs on a dedicated Jenkins server or CI agent where Jenkins LTS is running.
