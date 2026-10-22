$ErrorActionPreference = 'Stop'
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw 'Instale Maven y agréguelo al PATH. Consulte LEEME-PRIMERO.md.' }
Push-Location (Join-Path $PSScriptRoot '../backend')
try { & mvn spring-boot:run '-Dspring-boot.run.profiles=memory'; if ($LASTEXITCODE -ne 0) { throw 'Falló el backend.' } }
finally { Pop-Location }
