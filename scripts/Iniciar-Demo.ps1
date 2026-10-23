$ErrorActionPreference = 'Stop'
$jar = Join-Path $PSScriptRoot '../dist/cafejunin-backend-0.2.0.jar'
if (-not (Test-Path $jar)) { throw 'Primero ejecute Compilar.ps1.' }
$env:SECURITY_ENABLED='false'
$env:PREDICTOR_MODE='rules'
Write-Host 'Demo sin autenticación y en memoria: http://localhost:8080. Los datos se pierden al cerrar.'
java -jar $jar '--spring.profiles.active=memory'
