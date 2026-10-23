$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Herramientas.ps1')
Test-CafeEntorno
$raiz = Split-Path -Parent $PSScriptRoot
Push-Location (Join-Path $raiz 'web')
try {
    Invoke-CafeNpm -Arguments @('ci')
    Invoke-CafeNpm -Arguments @('run','build')
} finally { Pop-Location }
Push-Location (Join-Path $raiz 'backend')
try {
    Invoke-CafeMaven -Arguments @('clean','verify')
    New-Item -ItemType Directory -Path '../dist' -Force | Out-Null
    Copy-Item 'target/cafejunin-backend-0.2.0.jar' '../dist/' -Force
    Write-Host 'Compilacion correcta. Ejecutable: dist/cafejunin-backend-0.2.0.jar'
} finally { Pop-Location }
