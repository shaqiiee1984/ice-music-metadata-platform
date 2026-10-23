# PowerShell script to cleanly destroy all AWS resources deployed for ICE Music Catalogue
# Region: eu-central-1

$env:Path = "C:\Program Files\Amazon\AWSCLIV2;" + $env:Path
$Region = "eu-central-1"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " ICE Music Catalogue - AWS Teardown & 0$ Safeguard Script" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Terminate Instances
Write-Host "[1/4] Checking for running ICE Music instances..." -ForegroundColor Cyan
$instanceIds = (aws ec2 describe-instances `
    --region $Region `
    --filters "Name=tag:Project,Values=ICE-Music-Catalogue" "Name=instance-state-name,Values=running,pending,stopped" `
    --query "Reservations[*].Instances[*].InstanceId" `
    --output text)

if ($instanceIds -and $instanceIds.Trim().Length -gt 0) {
    Write-Host "Found instances: $instanceIds. Terminating..." -ForegroundColor Yellow
    aws ec2 terminate-instances --instance-ids $instanceIds.Split() --region $Region | Out-Null
    Write-Host "Waiting for instance termination to complete..." -ForegroundColor Cyan
    aws ec2 wait instance-terminated --instance-ids $instanceIds.Split() --region $Region
    Write-Host "Instances successfully terminated." -ForegroundColor Green
} else {
    Write-Host "No active instances found." -ForegroundColor Gray
}

# 2. Delete Security Group
Write-Host "[2/4] Checking for ICE Music Security Group..." -ForegroundColor Cyan
$sgId = (aws ec2 describe-security-groups `
    --region $Region `
    --filters "Name=group-name,Values=ice-music-sg" `
    --query "SecurityGroups[0].GroupId" `
    --output text)

if ($sgId -and $sgId -ne "None") {
    Write-Host "Deleting security group $sgId..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
    aws ec2 delete-security-group --group-id $sgId --region $Region 2>$null
    Write-Host "Security group deleted." -ForegroundColor Green
} else {
    Write-Host "No security group found." -ForegroundColor Gray
}

# 3. Delete Key Pair
Write-Host "[3/4] Checking for key pair..." -ForegroundColor Cyan
aws ec2 delete-key-pair --key-name ice-music-key --region $Region 2>$null
if (Test-Path "ice-music-key.pem") {
    Remove-Item -Force "ice-music-key.pem"
    Write-Host "Local key file ice-music-key.pem removed." -ForegroundColor Green
}
Write-Host "Key pair cleaned up." -ForegroundColor Green

# 4. Final verification
Write-Host "[4/4] Verifying all resources are cleared..." -ForegroundColor Cyan
$remaining = (aws ec2 describe-instances `
    --region $Region `
    --filters "Name=tag:Project,Values=ICE-Music-Catalogue" "Name=instance-state-name,Values=running,pending" `
    --query "Reservations[*].Instances[*].InstanceId" `
    --output text)

if (-not $remaining) {
    Write-Host "SUCCESS: Zero active instances or resources remain. No AWS charges will be incurred!" -ForegroundColor Green
} else {
    Write-Host "WARNING: Instances still shutting down: $remaining" -ForegroundColor Red
}
Write-Host "==========================================================" -ForegroundColor Cyan
