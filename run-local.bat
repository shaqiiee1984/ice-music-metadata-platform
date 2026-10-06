@echo off
echo ===================================================
echo Starting ICE Music Metadata Platform (Local Mode)
echo ===================================================
echo Starting Spring Boot Backend (Port 8080)...
start "ICE Backend (Spring Boot)" cmd /k "cd backend && mvnw.cmd spring-boot:run"

echo Starting Angular Frontend (Port 4200)...
start "ICE Frontend (Angular)" cmd /k "cd frontend && npm.cmd start"

echo.
echo Application will be available at:
echo - Frontend:  http://localhost:4200
echo - Swagger:   http://localhost:8080/swagger-ui.html
echo - Actuator:  http://localhost:8080/actuator/health
echo ===================================================
