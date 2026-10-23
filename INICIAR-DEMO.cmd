@echo off
cd /d "%~dp0"
set SECURITY_ENABLED=false
set PREDICTOR_MODE=rules
set SERVER_ADDRESS=127.0.0.1
set PORT=8080
java -jar dist\cafejunin-backend-0.2.0.jar --spring.profiles.active=memory
pause
