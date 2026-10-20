# Ansible Deployment & Operations Guide: RTI Request Tracking System (MVP-15)

This guide documents the automated deployment, configuration management, and post-deployment verification for the **Jenkins-Based RTI Request Tracking System** using Ansible and Docker Compose.

---

> [!NOTE]
> **Academic Project Notice:** This software is an isolated academic DevOps simulation developed for instructional evaluation. It does not interface with official government portals (such as `rtionline.gov.in`) and does not process real public funds or official legal filings.

---

## 1. Supported Control-Node & Target Architecture

```
┌─────────────────────────────────┐                 ┌─────────────────────────────────┐
│     Ansible Control Node        │                 │     Target Linux Host           │
│ (Linux, WSL2, or Jenkins Agent) │      SSH        │ (Ubuntu 22.04/24.04, Debian 12) │
│                                 │ ──────────────► │                                 │
│  • ansible-core 2.15+           │ (Port 22/Key)   │  • Docker Engine 24.0+          │
│  • Python 3.10+                 │                 │  • Docker Compose Plugin v2+    │
│  • OpenSSH Client               │                 │  • Python 3.10+ (target runtime)│
│  • Playbooks & Inventory        │                 │  • /opt/rti-request-tracking-sys│
└─────────────────────────────────┘                 └─────────────────────────────────┘
```

### A. Ansible Control Node Environment
- **Supported Operating Systems:** Linux (Ubuntu/Debian, Fedora, Rocky/RHEL), macOS, or Windows Subsystem for Linux (WSL2).
- **Tooling:** Python 3.10+, `ansible-core` $\ge$ 2.15.
- *Windows Host Note:* Windows cannot act directly as a standard Ansible control node without WSL2 or an Ansible execution container.

### B. Target Deployment Host Prerequisites
- **Operating System:** Supported Linux distribution (Ubuntu 22.04 LTS / 24.04 LTS, Debian 12, or Rocky Linux 9).
- **Container Runtime:** Docker Engine $\ge$ 24.0 and Docker Compose plugin (`docker compose`).
- **Python Environment:** Python 3 (`/usr/bin/python3`) for Ansible module execution.
- **Access:** SSH access for a non-root deployment user with passwordless `sudo` privileges.

---

## 2. Directory Structure

```
ansible/
├── ansible.cfg                    # Default Ansible settings (host key checking, pipelining)
├── deploy.yml                     # Top-level deployment entrypoint
├── verify.yml                     # Top-level verification entrypoint
├── inventory/
│   └── hosts.ini                  # Target host inventory (IP, port, SSH user)
├── group_vars/
│   ├── all.yml                    # Non-sensitive configuration (directories, ports, timeouts)
│   └── vault.example.yml          # Template for encrypted secrets
├── templates/
│   └── .env.j2                    # Jinja2 template for target .env configuration
└── playbooks/
    ├── deploy.yml                 # Idempotent deployment playbook
    └── verify.yml                 # Post-deployment validation playbook
```

---

## 3. Inventory & Variable Configuration

### A. Inventory Setup (`ansible/inventory/hosts.ini`)
Open `ansible/inventory/hosts.ini` and replace the placeholder documentation IP (`192.0.2.10`) with your target host's IP address or hostname:

```ini
[rti_servers]
rti-app-node-01 ansible_host=YOUR_TARGET_IP ansible_user=deploy_user ansible_port=22

[rti_servers:vars]
ansible_python_interpreter=/usr/bin/python3
ansible_ssh_common_args='-o StrictHostKeyChecking=accept-new'
```

### B. Group Variables (`ansible/group_vars/all.yml`)
Non-sensitive variables (such as application directory, Nginx ingress port, and health check retries) are configured in `group_vars/all.yml`:
- `deployment_dir`: `/opt/rti-request-tracking-system` (target path).
- `nginx_port`: `80` (public ingress port).
- `app_internal_port`: `8080`.

### C. Secret Management with Ansible Vault
Credentials (database passwords, root passwords) are never committed in plaintext.

1. Copy the example vault template:
   ```bash
   cp ansible/group_vars/vault.example.yml ansible/group_vars/vault.yml
   ```
2. Edit `ansible/group_vars/vault.yml` with secure passwords:
   ```yaml
   vault_db_password: "YOUR_STRONG_USER_PASSWORD"
   vault_db_root_password: "YOUR_STRONG_ROOT_PASSWORD"
   ```
3. Encrypt the file using Ansible Vault:
   ```bash
   ansible-vault encrypt ansible/group_vars/vault.yml
   ```
4. `ansible/group_vars/vault.yml` is ignored by Git in `.gitignore` to prevent secret leakage.

---

## 4. SSH Connectivity & Privilege Escalation

Ensure your SSH key is authorized on the target host:
```bash
ssh-copy-id -i ~/.ssh/id_rsa.pub deploy_user@YOUR_TARGET_IP
```

Test Ansible connectivity using the ad-hoc `ping` module:
```bash
ansible rti_servers -i ansible/inventory/hosts.ini -m ping
```

---

## 5. Playbook Syntax Check & Dry Run (Check Mode)

Prior to modifying target systems, perform static syntax validation and check-mode simulation:

### A. Syntax Validation
```bash
ansible-playbook -i ansible/inventory/hosts.ini ansible/deploy.yml --syntax-check
ansible-playbook -i ansible/inventory/hosts.ini ansible/verify.yml --syntax-check
```

### B. Dry-Run / Check Mode
Simulate task execution without applying changes on the remote host:
```bash
ansible-playbook -i ansible/inventory/hosts.ini ansible/deploy.yml --check --diff
```

---

## 6. Execution: Deployment & Verification

### A. Deploy the Application Stack
Execute the idempotent deployment playbook:
```bash
ansible-playbook -i ansible/inventory/hosts.ini ansible/deploy.yml
```
*(If using Ansible Vault for encrypted variables, append `--ask-vault-pass`)*

**Deployment Steps Automated by Playbook:**
1. Connects to target host and asserts Linux OS.
2. Checks Docker Engine and Docker Compose versions.
3. Creates `/opt/rti-request-tracking-system` directory structure.
4. Synchronizes `docker-compose.yml`, `Dockerfile`, `nginx.conf`, database schema `V1__init_rti_schema.sql`, and pre-packaged JAR.
5. Deploys `.env` with `0600` permissions (preserving existing target `.env` if already configured).
6. Runs `docker compose config` on the target host to validate Compose syntax.
7. Launches/updates containers via `docker compose up -d --remove-orphans`.
8. Polls Nginx on port 80 until HTTP 200 is confirmed.
9. Verifies security blocks: `/actuator/health` and `/reviewer.html` return HTTP 403 Forbidden.

### B. Run Post-Deployment Verification
Verify running containers, database volume, and UI accessibility:
```bash
ansible-playbook -i ansible/inventory/hosts.ini ansible/verify.yml
```

**Verification Tasks Performed:**
- `rti-mysql` container status is `running` and health status is `healthy`.
- `rti-app` container status is `running` and health status is `healthy`.
- `rti-nginx` container status is `running`.
- Persistent named volume `rti_mysql_data` is verified.
- Citizen submission form HTML contains required DOM fields (`applicantName`, `category`, `description`).
- Public tracking page HTML contains required DOM fields (`trackingIdInput`, `trackBtn`).
- Security boundaries: External access to `/actuator/` and `/reviewer.html` is confirmed blocked (HTTP 403).

---

## 7. Database Protection & Persistent Data Safety

- **Volume Preservation:** Normal playbook runs (`deploy.yml` and `verify.yml`) use non-destructive operations (`docker compose up -d`).
- **Data Safety:** The persistent database volume `rti_mysql_data` is **never removed** during deployment. All filed applications, tracking sequence numbers, and status audit logs survive restarts and upgrades.
- **Destructive Teardown:** If an operator needs to permanently destroy all stored data, it must be performed manually via `docker compose down -v`.

---

## 8. Jenkins CI/CD Integration Boundary

To integrate Ansible deployment into a future authorized Jenkins stage:

1. **Agent Setup:** The Jenkins agent must have Ansible installed and SSH access to the deployment target host.
2. **Credentials:** Store SSH keys and Vault passwords in Jenkins Credentials Store (`SSH Username with private key` and `Secret text`).
3. **Stage Configuration Example (Reference for Future CD Milestone):**
   ```groovy
   stage('Deploy to Staging') {
       when {
           branch 'main'
           expression { return params.DEPLOY_TO_STAGING == true }
       }
       steps {
           withCredentials([sshUserPrivateKey(credentialsId: 'target-ssh-key', keyFileVariable: 'SSH_KEY')]) {
               sh '''
                   ansible-playbook -i ansible/inventory/hosts.ini ansible/deploy.yml \
                       --private-key "${SSH_KEY}"
                   ansible-playbook -i ansible/inventory/hosts.ini ansible/verify.yml \
                       --private-key "${SSH_KEY}"
               '''
           }
       }
   }
   ```
4. **Current Status:** In compliance with the project constraints, deployment is kept manual to prevent unintentional deployments to unconfigured targets.

---

## 9. Troubleshooting & Common Issues

### A. Target Host Connection Refused (Port 22)
- Confirm SSH service is active on the target: `sudo systemctl status ssh`.
- Verify firewall allows port 22: `sudo ufw status`.
- Check inventory IP and port in `ansible/inventory/hosts.ini`.

### B. Docker Permission Denied on Target Host
If the `docker` command fails with permission denied:
- Add the deployment user to the `docker` group on the target host:
  ```bash
  sudo usermod -aG docker deploy_user
  ```
- Log out and log back in to apply group changes.

### C. Port 80 Ingress Conflict
If port 80 is already bound by another service on the target host:
- Open `ansible/group_vars/all.yml`.
- Change `nginx_port: 8080` (or another free port).
- Re-run `ansible-playbook -i ansible/inventory/hosts.ini ansible/deploy.yml`.
