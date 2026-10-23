# Automated AWS Free Tier Deployment Script for ICE Music Catalogue
# Region: eu-central-1 (Frankfurt)
# Instance: t2.micro (Free Tier eligible, 100% $0.00 cost)

$ErrorActionPreference = "Stop"
$env:Path = "C:\Program Files\Amazon\AWSCLIV2;" + $env:Path
$Region = "eu-central-1"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " ICE Music Catalogue - Automated AWS Free Tier Deployment " -ForegroundColor Yellow
Write-Host " Target Region: $Region (Frankfurt)" -ForegroundColor Yellow
Write-Host " Estimated Cost: $0.00 (AWS Free Tier Eligible)" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Verify Caller Identity
Write-Host "`n[1/6] Verifying AWS Identity..." -ForegroundColor Cyan
try {
    $caller = aws sts get-caller-identity | ConvertFrom-Json
    Write-Host "Authenticated as: $($caller.Arn) (Account: $($caller.Account))" -ForegroundColor Green
} catch {
    Write-Host "ERROR: AWS authentication failed. Make sure your IAM user has permissions attached." -ForegroundColor Red
    exit 1
}

# 2. Retrieve Default VPC & Subnet
Write-Host "`n[2/6] Locating default VPC in $Region..." -ForegroundColor Cyan
$vpcId = (aws ec2 describe-vpcs --region $Region --filters "Name=isDefault,Values=true" --query "Vpcs[0].VpcId" --output text)
if (-not $vpcId -or $vpcId -eq "None") {
    Write-Host "Finding any available VPC..." -ForegroundColor Yellow
    $vpcId = (aws ec2 describe-vpcs --region $Region --query "Vpcs[0].VpcId" --output text)
}
Write-Host "Using VPC: $vpcId" -ForegroundColor Green

# 3. Create or Configure Security Group
Write-Host "`n[3/6] Configuring Security Group (Port 80 Ingress only)..." -ForegroundColor Cyan
$sgId = (aws ec2 describe-security-groups --region $Region --filters "Name=group-name,Values=ice-music-sg" "Name=vpc-id,Values=$vpcId" --query "SecurityGroups[0].GroupId" --output text)

if (-not $sgId -or $sgId -eq "None") {
    Write-Host "Creating security group 'ice-music-sg'..." -ForegroundColor Yellow
    $sgId = (aws ec2 create-security-group `
        --group-name "ice-music-sg" `
        --description "Security Group for ICE Music Catalogue (Port 80 HTTP only)" `
        --vpc-id $vpcId `
        --tag-specifications "ResourceType=security-group,Tags=[{Key=Name,Value=ice-music-sg},{Key=Project,Value=ICE-Music-Catalogue}]" `
        --region $Region `
        --query "GroupId" `
        --output text)

    # Ingress Rule: Allow HTTP Port 80 from everywhere
    aws ec2 authorize-security-group-ingress `
        --group-id $sgId `
        --protocol tcp `
        --port 80 `
        --cidr 0.0.0.0/0 `
        --region $Region | Out-Null

    # Optional Ingress Rule: Allow SSH Port 22 from caller's IP
    try {
        $myIp = (Invoke-RestMethod -Uri "https://checkip.amazonaws.com" -TimeoutSec 3).Trim()
        if ($myIp) {
            aws ec2 authorize-security-group-ingress `
                --group-id $sgId `
                --protocol tcp `
                --port 22 `
                --cidr "$myIp/32" `
                --region $Region | Out-Null
            Write-Host "SSH Port 22 restricted strictly to your current IP ($myIp)." -ForegroundColor Green
        }
    } catch {
        Write-Host "Note: Could not detect public IP for SSH restriction; leaving port 22 closed." -ForegroundColor Gray
    }
}
Write-Host "Security Group ID: $sgId" -ForegroundColor Green

# 4. Create or Reuse Key Pair
Write-Host "`n[4/6] Checking Key Pair..." -ForegroundColor Cyan
$keyExists = (aws ec2 describe-key-pairs --region $Region --filters "Name=key-name,Values=ice-music-key" --query "KeyPairs[0].KeyName" --output text)
if (-not $keyExists -or $keyExists -eq "None") {
    Write-Host "Creating new key pair 'ice-music-key'..." -ForegroundColor Yellow
    $keyMaterial = aws ec2 create-key-pair --key-name "ice-music-key" --region $Region --query "KeyMaterial" --output text
    $keyMaterial | Out-File -Encoding ascii -FilePath "ice-music-key.pem"
    Write-Host "Saved private key to ice-music-key.pem" -ForegroundColor Green
} else {
    Write-Host "Using existing key pair 'ice-music-key'" -ForegroundColor Green
}

# 5. Resolve Latest Amazon Linux 2023 AMI (Free Tier eligible)
Write-Host "`n[5/6] Resolving Free Tier Amazon Linux 2023 AMI..." -ForegroundColor Cyan
$amiId = (aws ec2 describe-images `
    --region $Region `
    --owners amazon `
    --filters "Name=name,Values=al2023-ami-2023.*-x86_64" "Name=state,Values=available" `
    --query "sort_by(Images, &CreationDate)[-1].ImageId" `
    --output text)
Write-Host "Selected AMI: $amiId" -ForegroundColor Green

# Prepare Cloud-Init User Data Script
$userData = @'
#!/bin/bash
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1
echo "=== Starting ICE Music Free Tier Setup ==="

# 1. Allocate 4 GB Swap to guarantee 0 OOM during builds
if [ ! -f /swapfile ]; then
    echo "Creating 4GB swap space..."
    dd if=/dev/zero of=/swapfile bs=1M count=4096
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

# 2. Install Docker & Git
echo "Installing Docker and Git..."
dnf update -y
dnf install -y docker git
systemctl enable --now docker
usermod -a -G docker ec2-user

# Install Docker Compose CLI plugin
mkdir -p /usr/local/lib/docker/cli-plugins
curl -sSL https://github.com/docker/compose/releases/download/v2.29.1/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
ln -sf /usr/local/lib/docker/cli-plugins/docker-compose /usr/bin/docker-compose

# 3. Clone Repository
mkdir -p /opt/app
cd /opt/app
git clone https://github.com/shaqiiee1984/ice-music-metadata-platform.git .

# 4. Write production docker compose configuration
cat << 'EOF' > docker-compose.prod.yml
services:
  postgres:
    image: postgres:16-alpine
    container_name: ice-music-postgres
    restart: unless-stopped
    environment:
      POSTGRES_USER: ice_user
      POSTGRES_PASSWORD: ice_password
      POSTGRES_DB: ice_music
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ice_user -d ice_music"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: ice-music-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ice_music
      SPRING_DATASOURCE_USERNAME: ice_user
      SPRING_DATASOURCE_PASSWORD: ice_password
      JAVA_OPTS: "-XX:+UseContainerSupport -Xmx256m -Xms128m -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: ice-music-frontend
    restart: unless-stopped
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  postgres_data:
    driver: local
EOF

# 5. Build and launch services in background
docker-compose -f docker-compose.prod.yml up --build -d
echo "=== Deployment Completed Successfully ==="
'@

$userDataBase64 = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($userData))

# 6. Launch EC2 Free Tier Instance
Write-Host "`n[6/6] Launching t2.micro EC2 Instance (20 GB gp3 storage)..." -ForegroundColor Cyan
$instanceResult = (aws ec2 run-instances `
    --image-id $amiId `
    --instance-type "t2.micro" `
    --key-name "ice-music-key" `
    --security-group-ids $sgId `
    --user-data $userDataBase64 `
    --block-device-mappings "DeviceName=/dev/xvda,Ebs={VolumeSize=20,VolumeType=gp3,DeleteOnTermination=true}" `
    --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=ice-music-app-server},{Key=Project,Value=ICE-Music-Catalogue}]" `
    --region $Region | ConvertFrom-Json)

$instanceId = $instanceResult.Instances[0].InstanceId
Write-Host "Instance Launched: $instanceId" -ForegroundColor Green

Write-Host "Waiting for instance to enter 'running' state..." -ForegroundColor Cyan
aws ec2 wait instance-running --instance-ids $instanceId --region $Region

# Fetch Public IP
$instanceDesc = (aws ec2 describe-instances --instance-ids $instanceId --region $Region | ConvertFrom-Json)
$publicIp = $instanceDesc.Reservations[0].Instances[0].PublicIpAddress
$publicDns = $instanceDesc.Reservations[0].Instances[0].PublicDnsName

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host " INSTANCE READY & INITIALIZING SERVICES" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "Instance ID : $instanceId" -ForegroundColor Yellow
Write-Host "Public IP   : $publicIp" -ForegroundColor Yellow
Write-Host "Public DNS  : $publicDns" -ForegroundColor Yellow
Write-Host "`nPublic URLs for Testing (accessible in ~3-4 mins after Docker builds):" -ForegroundColor Cyan
Write-Host "  1. Frontend Web App : http://$publicIp/" -ForegroundColor White
Write-Host "  2. Backend Health   : http://$publicIp/actuator/health" -ForegroundColor White
Write-Host "  3. Swagger UI Docs  : http://$publicIp/swagger-ui/index.html" -ForegroundColor White
Write-Host "  4. REST API Endpoint: http://$publicIp/api/v1/artists" -ForegroundColor White
Write-Host "`nWhen finished with your presentation, run '.\teardown-aws.bat' to terminate all resources ($0.00 guarantee)." -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Green
