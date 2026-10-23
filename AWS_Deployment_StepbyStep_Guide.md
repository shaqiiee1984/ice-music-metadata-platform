# AWS Free Tier Deployment & Teardown Step-by-Step Guide
**Project:** ICE Music Catalogue & Artist Service (`ICE_Assignment_Architecture_2`)  
**Target AWS Region:** `eu-central-1` (Frankfurt)  
**Cost Target:** **$0.00 (100% AWS Free Tier Eligible)**  
**Status:** **LIVE & VERIFIED**  
**Public IP:** `18.159.206.33`  
**Instance ID:** `i-05fb5c4ed926d6a6a`  

---

## 1. Verified Live Application Endpoints

The complete application is deployed, verified, and accessible globally:

| Service / Interface | Public URL | Verified Status | Description |
| :--- | :--- | :--- | :--- |
| **Angular 18 Frontend SPA** | [http://18.159.206.33/](http://18.159.206.33/) | `HTTP 200 OK` | Main Dashboard with Artist of the Day, Artist Table, Discography, and Editor/Viewer toggle |
| **Backend Actuator Health** | [http://18.159.206.33/actuator/health](http://18.159.206.33/actuator/health) | `HTTP 200 {"status":"UP"}` | Spring Boot Actuator liveness/readiness probe |
| **OpenAPI / Swagger UI** | [http://18.159.206.33/swagger-ui.html](http://18.159.206.33/swagger-ui.html) | `HTTP 200 OK` | Interactive Swagger API documentation |
| **Artists API Endpoint** | [http://18.159.206.33/api/v1/artists](http://18.159.206.33/api/v1/artists) | `HTTP 200 OK` | Paginated artist metadata collection |
| **Featured Artist of the Day** | [http://18.159.206.33/api/v1/featured/today](http://18.159.206.33/api/v1/featured/today) | `HTTP 200 OK` | Cyclical rotation engine (`Lady Gaga` featured today) |

---

## 2. Architecture & Security Blueprint ($0.00 Free Tier)

### 2.1 Free Tier Allocation Safeguards
| Component | AWS Resource | Free Tier Monthly Limit | Allocation in this Deployment | Monthly Cost |
| :--- | :--- | :--- | :--- | :--- |
| **Compute** | EC2 Instance (`t2.micro`) | 750 hours/month | 1 instance (`i-05fb5c4ed926d6a6a`) | **$0.00** |
| **Storage** | EBS Volume (gp3) | 30 GB/month | 20 GB (`/dev/xvda`, DeleteOnTermination) | **$0.00** |
| **Networking** | Inbound/Outbound Data Transfer | 100 GB/month outbound | < 1 GB | **$0.00** |
| **IPv4 Address** | EC2 Public IPv4 | 750 hours/month included in Free Tier | 1 auto-assigned (`18.159.206.33`) | **$0.00** |
| **Total Estimated Cost** | | | | **$0.00** |

### 2.2 Security Architecture
```
                         INTERNET
                            │
               Port 80 (HTTP) [Public Ingress]
                            ▼
     ┌──────────────────────────────────────────────┐
     │  EC2 Instance (Security Group: ice-music-sg) │
     │                                              │
     │   ┌──────────────────────────────────────┐   │
     │   │ Docker Container: Frontend (Nginx)   │   │
     │   │ - Serves Angular 18 SPA              │   │
     │   │ - Reverse proxy /api/ & /swagger-ui/ │   │
     │   │ - Enforces strict security headers   │   │
     │   └──────────────────┬───────────────────┘   │
     │                      │ (Docker Network Only) │
     │                      ▼                       │
     │   ┌──────────────────────────────────────┐   │
     │   │ Docker Container: Backend (Spring)   │   │
     │   │ - Port 8080 (NOT exposed to public)  │   │
     │   └──────────────────┬───────────────────┘   │
     │                      │ (Docker Network Only) │
     │                      ▼                       │
     │   ┌──────────────────────────────────────┐   │
     │   │ Docker Container: PostgreSQL 16      │   │
     │   │ - Port 5432 (NOT exposed to public)  │   │
     │   └──────────────────────────────────────┘   │
     └──────────────────────────────────────────────┘
```
- **Zero Exposed Databases:** PostgreSQL port `5432` and Spring Boot port `8080` are strictly internal to the Docker bridge network. External port scanners cannot connect to PostgreSQL or the Spring Boot raw port.
- **Single Public Port:** Only port `80` is open to the public internet for Nginx.
- **SSH Protection:** Port `22` in `ice-music-sg` is strictly restricted to your workstation IP address.
- **RAM Protection via Swap:** A 4 GB swap file is allocated on `/swapfile`, allowing the 1 GB `t2.micro` instance to run Java 21, PostgreSQL, and Angular with 0 out-of-memory errors.

---

## 3. Deployment Action Items Log

### Task 1: IAM User Configuration
- **Status:** **COMPLETED**
- **IAM User:** `arn:aws:iam::908438170533:user/ice-deployer`
- **Region:** `eu-central-1` (Frankfurt)
- **Policies Attached:** `AmazonEC2FullAccess`, `AmazonVPCFullAccess`

### Task 2: Automated Infrastructure Provisioning
- **Status:** **COMPLETED**
- **Security Group:** `sg-07e0b895ca0bca238` (`ice-music-sg`)
- **Key Pair:** `ice-music-key` (`ice-music-key.pem` stored locally)
- **AMI:** `ami-0878e4d659d91d45e` (Amazon Linux 2023 64-bit x86)
- **Cloud-Init Automation:**
  1. Configured 4 GB swap file on `/swapfile`.
  2. Installed Docker `25.0.16` and Docker Compose CLI plugin `v2.29.1`.
  3. Cloned project repository from GitHub.
  4. Executed `docker-compose -f docker-compose.prod.yml up --build -d`.

### Task 3: Application Health & Smoke Testing
- **Status:** **COMPLETED & VERIFIED**
- All 3 containers (`ice-music-frontend`, `ice-music-backend`, `ice-music-postgres`) are healthy.
- Full UI and API end-to-end functionality verified via automated browser agent.

---

## 4. Post-Interview Teardown & Resource De-deployment ($0.00 Safeguard)

To guarantee that **zero costs** are incurred after your interview and presentation, execute either the one-click script or manual commands below.

### 4.1 One-Click Automated Cleanup (Recommended)
Simply double-click:
```powershell
.\teardown-aws.bat
```
*(Or in PowerShell: `powershell -ExecutionPolicy Bypass -File .\teardown-aws.ps1`)*

The script will automatically:
1. Terminate EC2 instance `i-05fb5c4ed926d6a6a`.
2. Automatically delete the attached 20 GB EBS volume (via `DeleteOnTermination`).
3. Delete security group `sg-07e0b895ca0bca238`.
4. Delete key pair `ice-music-key` and clean up local `.pem` file.
5. Verify that zero active billable resources remain in `eu-central-1`.

### 4.2 Manual CLI De-deployment Steps
If you prefer running manual commands in your terminal:

```powershell
# 1. Terminate the EC2 Instance
aws ec2 terminate-instances --instance-ids i-05fb5c4ed926d6a6a --region eu-central-1

# 2. Wait for termination to complete
aws ec2 wait instance-terminated --instance-ids i-05fb5c4ed926d6a6a --region eu-central-1

# 3. Delete Security Group
aws ec2 delete-security-group --group-id sg-07e0b895ca0bca238 --region eu-central-1

# 4. Delete Key Pair
aws ec2 delete-key-pair --key-name ice-music-key --region eu-central-1
Remove-Item ice-music-key.pem -ErrorAction SilentlyContinue

# 5. Verify 0 billable resources remain
aws ec2 describe-instances --filters "Name=instance-state-name,Values=running,pending" --region eu-central-1
```
