$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '../backend')
try { & mvn verify; if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas.' } }
finally { Pop-Location }
