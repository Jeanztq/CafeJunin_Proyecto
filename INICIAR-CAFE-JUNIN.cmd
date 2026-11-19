@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File "%~dp0scripts\Iniciar-Portal.ps1" %*
if errorlevel 1 (
  pause
  exit /b 1
)
