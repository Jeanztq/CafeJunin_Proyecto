# Ejecutar desde el proyecto ya extraído. No requiere administrador.
$ErrorActionPreference = 'Stop'
$origen = Split-Path -Parent $PSScriptRoot
$escritorio = [Environment]::GetFolderPath('Desktop')
if ([string]::IsNullOrWhiteSpace($escritorio)) { throw 'Windows no devolvió una ruta de Escritorio.' }
$destino = Join-Path $escritorio 'CafeJunin'
if ([IO.Path]::GetFullPath($origen).TrimEnd('\') -eq [IO.Path]::GetFullPath($destino).TrimEnd('\')) {
 Write-Host "El proyecto ya está en: $destino"
 exit 0
}
if (Test-Path $destino) { throw "Ya existe $destino. Renómbrelo o elija otra ubicación para evitar sobrescribir trabajo." }
Copy-Item -LiteralPath $origen -Destination $destino -Recurse
Write-Host "Proyecto copiado en: $destino"
Write-Host 'Abra LEEME-PRIMERO.md para comenzar.'
