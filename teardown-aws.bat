@echo off
echo Running AWS Teardown for ICE Music Catalogue...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0teardown-aws.ps1"
pause
