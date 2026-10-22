$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'Instale Git primero.' }
Push-Location $raiz
try {
 if (Test-Path '.git') { Write-Host 'Este proyecto ya tiene repositorio Git.'; exit 0 }
 git init -b main
 if ($LASTEXITCODE -ne 0) { throw 'Falló git init.' }
 Write-Host 'Repositorio local iniciado. Revise git status antes de añadir archivos y crear el primer commit.'
 Write-Host 'No se ha publicado ni enviado ningún archivo a GitHub.'
} finally { Pop-Location }
