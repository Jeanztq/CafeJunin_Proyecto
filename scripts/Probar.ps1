$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Herramientas.ps1')
Test-CafeEntorno
$raiz = Split-Path -Parent $PSScriptRoot
Push-Location (Join-Path $raiz 'web')
try {
    Invoke-CafeNpm -Arguments @('ci')
    Invoke-CafeNpm -Arguments @('test')
    Invoke-CafeNpm -Arguments @('run','build')
} finally { Pop-Location }
Push-Location (Join-Path $raiz 'backend')
try { Invoke-CafeMaven -Arguments @('clean','verify') } finally { Pop-Location }
