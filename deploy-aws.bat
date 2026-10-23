@echo off
echo Starting AWS Free Tier Deployment for ICE Music Catalogue...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0deploy-aws.ps1"
pause
