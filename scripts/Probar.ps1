$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '../web')
try { npm test; if ($LASTEXITCODE -ne 0) { throw 'Falló React' }; npm run build; if ($LASTEXITCODE -ne 0) { throw 'Falló compilación React' } } finally { Pop-Location }
Push-Location (Join-Path $PSScriptRoot '../backend')
try { mvn verify; if ($LASTEXITCODE -ne 0) { throw 'Falló Java' } } finally { Pop-Location }
