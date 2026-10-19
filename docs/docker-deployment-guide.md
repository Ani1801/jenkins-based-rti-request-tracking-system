# Docker Deployment Guide: RTI Request Tracking System (MVP-13)

This guide documents the containerized deployment procedure for the **Jenkins-Based RTI Request Tracking System** using Docker, Docker Compose, and Nginx.

---

## 1. Prerequisites

Before deploying the containerized stack, ensure the following host tools are installed:

- **Docker Engine:** Version 24.0+ (or Docker Desktop)
- **Docker Compose:** Version 2.20+ (included with modern Docker Desktop and CLI as `docker compose`)
- **Git:** Version 2.30+
- **Host Ports:** Port `80` (or configured `NGINX_PORT`) free of port conflicts (e.g. no local Apache or IIS listening on port 80).

---

## 2. Environment Variable Setup

The stack externalizes all database names and user credentials so that sensitive secrets are never baked into container images or committed to version control.

1. Create a local `.env` file from the provided template:
   - **Linux / macOS:**
     ```bash
     cp .env.example .env
     ```
   - **Windows (PowerShell):**
     ```powershell
     Copy-Item .env.example .env
     ```

2. Open `.env` and supply secure credentials:
   ```env
   DB_NAME=rti_db
   DB_USER=rti_user
   DB_PASSWORD=YOUR_STRONG_USER_PASSWORD
   DB_ROOT_PASSWORD=YOUR_STRONG_ROOT_PASSWORD
   NGINX_PORT=80
   ```

> [!CAUTION]
> The `.env` file is excluded from Git via `.gitignore`. Never commit `.env` or plain-text passwords to source control.

---

## 3. Container Stack Architecture

The multi-container stack consists of three isolated services interconnected via a dedicated Docker bridge network (`rti-network`):

```
+-------------------------------------------------------------------------+
| Host Machine                                                            |
|                                                                         |
|   Client Browser / Ingress                                              |
|         │                                                               |
|         ▼ (Port 80)                                                     |
|   ┌──────────────┐                                                      |
|   │  rti-nginx   │  Reverse Proxy (Port 80)                             |
|   └──────┬───────┘  Restricts /actuator & /reviewer.html                |
|          │                                                              |
|          ▼ (Port 8080 - Internal)                                       |
|   ┌──────────────┐                                                      |
|   │   rti-app    │  Spring Boot 3.x (Java 17 JRE - Non-root rtiuser)    |
|   └──────┬───────┘  Actuator health check at /actuator/health           |
|          │                                                              |
|          ▼ (Port 3306 - Internal)                                       |
|   ┌──────────────┐                                                      |
|   │  rti-mysql   │  MySQL 8.x (Health check via mysqladmin ping)        |
|   └──────┬───────┘                                                      |
|          │                                                              |
|          ▼                                                              |
|   [ rti_mysql_data ] (Named Docker Volume for Persistence)              |
+-------------------------------------------------------------------------+
```

### Ingress & Security Isolation
- **Nginx (`rti-nginx`):** Acts as the single public entry point on port 80. Routes public citizen requests (`/`, `/index.html`, `/track.html`, `/api/v1/requests/**`) to the application backend.
- **Actuator Endpoint Protection:** `/actuator/**` paths are blocked with HTTP 403 at Nginx. The `rti-app` container health check queries `http://localhost:8080/actuator/health` internally over loopback.
- **Reviewer Portal Boundary:** In this MVP, administrative reviewer screens (`/reviewer.html`) and transition APIs (`/api/v1/reviewer/**`) are blocked at Nginx ingress with HTTP 403, preventing public exposure prior to Spring Security role binding.
- **MySQL (`rti-mysql`):** Database port 3306 is not published to the host machine. All database traffic is strictly confined to `rti-network`.

---

## 4. Commands to Build and Start the Stack

### Step 1: Validate Compose Configuration
Verify that environment variables resolve cleanly and the YAML syntax is valid:
```bash
docker compose config
```

### Step 2: Build Container Images
Build the multi-stage Spring Boot image using the project Dockerfile:
```bash
docker compose build
```

### Step 3: Launch Stack in Detached Mode
Start all services in background:
```bash
docker compose up -d
```

The startup sequence guarantees deterministic dependency ordering:
1. `rti-mysql` starts first and executes health checks (`mysqladmin ping`).
2. `rti-app` waits for `rti-mysql` to reach `healthy` state, then boots Spring Boot and runs Flyway/Hibernate schema migrations.
3. `rti-nginx` waits for `rti-app` to reach `healthy` state before accepting client connections.

---

## 5. Inspecting Container Status and Logs

### Check Container Status and Health
```bash
docker compose ps
```
Expected output:
```text
NAME         IMAGE                               COMMAND                  SERVICE   CREATED          STATUS                    PORTS
rti-app      rti-request-tracking-system:1.0.0   "sh -c 'exec java $J…"   app       40 seconds ago   Up 40 seconds (healthy)   8080/tcp
rti-mysql    mysql:8.0                           "docker-entrypoint.s…"   mysql     55 seconds ago   Up 55 seconds (healthy)   3306/tcp
rti-nginx    nginx:alpine                        "/docker-entrypoint.…"   nginx     25 seconds ago   Up 25 seconds (healthy)   0.0.0.0:80->80/tcp
```

### Stream Container Logs
- All services:
  ```bash
  docker compose logs -f
  ```
- Application logs only:
  ```bash
  docker compose logs -f app
  ```
- Database logs only:
  ```bash
  docker compose logs -f mysql
  ```

---

## 6. Verifying Application Through Browser

1. **Citizen Portal (Request Filing):** Open `http://localhost/` or `http://localhost/index.html`. Fill out the form, submit an inquiry, and copy the assigned tracking identifier (`RTI-YYYY-XXXXXX`).
2. **Public Status Inquiry:** Open `http://localhost/track.html`. Search using the generated tracking ID to verify that the application details and chronological audit timeline are rendered.
3. **Security Boundary Verification:**
   - Navigate to `http://localhost/actuator/health` $\rightarrow$ Returns **HTTP 403 Forbidden** (blocked by Nginx reverse proxy).
   - Navigate to `http://localhost/reviewer.html` $\rightarrow$ Returns **HTTP 403 Forbidden** (blocked by Nginx reverse proxy).

---

## 7. Stopping and Restarting Without Data Loss

To stop containers while preserving database state:
```bash
docker compose stop
```
Or to stop and remove containers and network while preserving data:
```bash
docker compose down
```

### Verifying Data Persistence
When the stack is restarted with:
```bash
docker compose up -d
```
All previously submitted RTI applications, tracking identifiers, and status histories stored in the named volume `rti_mysql_data` remain intact.

---

## 8. Removing Containers and Volume (Permanent Deletion)

> [!WARNING]
> Deleting the persistent database volume permanently destroys all stored RTI requests, audit logs, and sequences. This action cannot be undone.

To completely tear down containers, networks, and the database volume:
```bash
docker compose down -v
```

---

## 9. Troubleshooting

### Port 80 Already in Use
If another service on your host (e.g., IIS, Skype, or local Apache) is listening on port 80:
1. Open `.env`.
2. Change `NGINX_PORT` to an alternate port, such as `NGINX_PORT=8081`.
3. Restart: `docker compose up -d`.
4. Access via `http://localhost:8081/`.

### Database Connection Failure on Startup
If `rti-app` fails to connect to MySQL:
- Verify that `DB_HOST=mysql` (the Compose service name), not `localhost`.
- Check MySQL logs: `docker compose logs mysql`.
- Verify credentials in `.env` match `DB_USER` and `DB_PASSWORD`.
