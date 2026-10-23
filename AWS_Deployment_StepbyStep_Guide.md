# AWS Free Tier Deployment & Teardown Step-by-Step Guide
**Project:** ICE Music Catalogue & Artist Service (`ICE_Assignment_Architecture_2`)  
**Target AWS Region:** `eu-central-1` (Frankfurt)  
**Cost Target:** **$0.00 (100% AWS Free Tier Eligible)**  
**Target Architecture:** Docker Compose on Amazon Linux 2023 (`t2.micro` / `t3.micro`)

---

## 1. Architecture & Security Blueprint ($0.00 Free Tier)

### 1.1 Free Tier Allocation Safeguards
| Component | AWS Resource | Free Tier Monthly Limit | Allocation in this Deployment | Monthly Cost |
| :--- | :--- | :--- | :--- | :--- |
| **Compute** | EC2 Instance (`t2.micro` / `t3.micro`) | 750 hours/month | 1 instance | **$0.00** |
| **Storage** | EBS Volume (gp3) | 30 GB/month | 20 GB | **$0.00** |
| **Networking** | Inbound/Outbound Data Transfer | 100 GB/month outbound | < 1 GB | **$0.00** |
| **IPv4 Address** | EC2 Public IPv4 | 750 hours/month included in Free Tier | 1 auto-assigned | **$0.00** |
| **Total Estimated Cost** | | | | **$0.00** |

### 1.2 Security Architecture
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
- **Zero Exposed Databases:** PostgreSQL port `5432` and Spring Boot port `8080` are strictly internal to the Docker bridge network.
- **Single Public Port:** Only port `80` is open to the public internet for the Nginx web server and reverse proxy.
- **SSH Protection:** Port `22` is either restricted to your specific workstation IP or managed securely via AWS Systems Manager Session Manager (no open inbound ports required).
- **RAM Protection via Swap:** A 4 GB swap file is allocated on the 20 GB EBS disk so that Java 21, PostgreSQL, and Angular run smoothly without triggering Out-Of-Memory (OOM) on a 1 GB instance.

---

## 2. Step-by-Step Deployment Log & Action Items

### Task 1: IAM User Configuration (Action Required)
- **Status:** Pending IAM Policy Attachment
- **User:** `arn:aws:iam::908438170533:user/ice-deployer`
- **Region:** `eu-central-1`
- **Action Required:**
  1. Open [AWS IAM Users Console](https://us-east-1.console.aws.amazon.com/iam/home#/users/ice-deployer).
  2. Click **Permissions** tab > **Add permissions** > **Add permissions**.
  3. Select **Attach policies directly**.
  4. Search and select **`AdministratorAccess`** (or minimum: `AmazonEC2FullAccess` + `AmazonVPCFullAccess`).
  5. Click **Next** > **Add permissions**.

---

### Task 2: Automated Infrastructure Provisioning (Executed via Agent)
Once Task 1 is verified, the agent executes the following commands:
1. **Key Pair Creation (optional for SSH / backup access):**
   ```bash
   aws ec2 create-key-pair --key-name ice-music-key --query "KeyMaterial" --output text > ice-music-key.pem
   ```
2. **Security Group Creation:**
   ```bash
   aws ec2 create-security-group --group-name ice-music-sg --description "Security Group for ICE Music Catalogue" --vpc-id <VPC_ID>
   aws ec2 authorize-security-group-ingress --group-name ice-music-sg --protocol tcp --port 80 --cidr 0.0.0.0/0
   aws ec2 authorize-security-group-ingress --group-name ice-music-sg --protocol tcp --port 22 --cidr <YOUR_IP>/32
   ```
3. **Launch Free Tier EC2 Instance (`t2.micro` or `t3.micro`):**
   - AMI: Amazon Linux 2023 (`al2023-ami-minimal-*` or standard `al2023-ami-*`)
   - Instance Type: `t2.micro` (or `t3.micro`)
   - Storage: 20 GB gp3 (`DeleteOnTermination: true`)
   - User Data script to automatically:
     - Install Docker & Docker Compose plugin
     - Enable 4 GB Swap file to ensure smooth Java/Postgres operation
     - Clone/transfer project files and run `docker compose up -d`

---

### Task 3: Application Health & Smoke Testing
Once provisioned and booted, the agent validates:
1. **Frontend UI:** `http://<EC2_PUBLIC_IP>/` (Angular Dashboard)
2. **Backend Actuator Health:** `http://<EC2_PUBLIC_IP>/actuator/health` (HTTP 200 `UP`)
3. **Swagger UI Documentation:** `http://<EC2_PUBLIC_IP>/swagger-ui/index.html`
4. **REST API Endpoint:** `http://<EC2_PUBLIC_IP>/api/v1/artists`

---

## 3. Post-Interview Teardown & Resource De-deployment ($0 Safeguard)

To guarantee that **zero costs** are incurred after your interview and presentation, execute the teardown script or the single CLI commands below.

### 3.1 One-Command Automated Cleanup
Run the provided teardown script:
```powershell
.\teardown-aws.bat
```

### 3.2 Manual CLI De-deployment Steps
If you prefer running individual commands:

1. **Terminate the EC2 Instance:**
   ```powershell
   aws ec2 terminate-instances --instance-ids <INSTANCE_ID> --region eu-central-1
   ```
   *Note: Because the EBS volume was marked `DeleteOnTermination: true`, terminating the instance automatically deletes the attached 20 GB EBS disk.*

2. **Wait for Termination:**
   ```powershell
   aws ec2 wait instance-terminated --instance-ids <INSTANCE_ID> --region eu-central-1
   ```

3. **Delete Security Group:**
   ```powershell
   aws ec2 delete-security-group --group-id <SECURITY_GROUP_ID> --region eu-central-1
   ```

4. **Delete Key Pair (if created):**
   ```powershell
   aws ec2 delete-key-pair --key-name ice-music-key --region eu-central-1
   Remove-Item ice-music-key.pem -ErrorAction SilentlyContinue
   ```

5. **Verify No Orphaned Resources Remain:**
   ```powershell
   aws ec2 describe-instances --filters "Name=instance-state-name,Values=running,pending" --region eu-central-1
   aws ec2 describe-volumes --filters "Name=status,Values=available,in-use" --region eu-central-1
   ```
   *Output will be empty, verifying 0 active billing items.*
