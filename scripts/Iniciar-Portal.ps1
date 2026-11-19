$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '..')
try {
    & java -jar 'dist/cafejunin-backend-0.2.0.jar' @args
    if ($LASTEXITCODE -ne 0) { throw "El portal terminó con código $LASTEXITCODE." }
} finally { Pop-Location }
