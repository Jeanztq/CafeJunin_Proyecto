$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
Push-Location (Join-Path $raiz 'web')
try { npm ci; if ($LASTEXITCODE -ne 0) { throw 'Falló npm ci' }; npm run build; if ($LASTEXITCODE -ne 0) { throw 'Falló React' } } finally { Pop-Location }
Push-Location (Join-Path $raiz 'backend')
try { mvn verify; if ($LASTEXITCODE -ne 0) { throw 'Falló Maven' }; Copy-Item 'target/cafejunin-backend-0.2.0.jar' '../dist/' -Force } finally { Pop-Location }
