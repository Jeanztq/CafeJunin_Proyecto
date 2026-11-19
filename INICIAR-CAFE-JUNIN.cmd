@echo off
setlocal
cd /d "%~dp0"
if not exist "dist\cafejunin-backend-0.2.0.jar" (
  echo Primero ejecute scripts\Compilar.ps1.
  exit /b 1
)
java -jar "dist\cafejunin-backend-0.2.0.jar" %*
if errorlevel 1 pause
