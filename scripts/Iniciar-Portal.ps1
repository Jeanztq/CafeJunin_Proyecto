param([switch]$OperadorLocal)
$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '..')
try {
    $jar = 'dist/cafejunin-backend-0.2.0.jar'
    if (-not (Test-Path -LiteralPath $jar)) { throw 'Falta el JAR. Ejecute scripts/Compilar.ps1.' }
    $java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
    $opciones = @()
    if ($OperadorLocal) {
        $opciones = @('--app.security.enabled=false', '--server.address=127.0.0.1')
        Write-Host 'Acceso de operador local en este equipo.'
    } else {
        $url = if ($env:KEYCLOAK_URL) { $env:KEYCLOAK_URL.TrimEnd('/') } else { 'http://localhost:8081' }
        $realm = if ($env:KEYCLOAK_REALM) { $env:KEYCLOAK_REALM } else { 'cafejunin' }
        $issuer = "$url/realms/$realm"
        function Test-Identidad {
            try {
                $config = Invoke-RestMethod "$issuer/.well-known/openid-configuration" -TimeoutSec 3
                return $config.issuer -eq $issuer
            } catch { return $false }
        }
        if (-not (Test-Identidad)) {
            if ($url -ne 'http://localhost:8081' -or $realm -ne 'cafejunin') { throw "No se pudo contactar con $issuer." }
            if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'El acceso por cuenta requiere Keycloak. Inicie Docker Desktop o configure KEYCLOAK_URL.' }
            try {
                & docker info --format '{{.ServerVersion}}' 2>$null | Out-Null
                $motorDisponible = $LASTEXITCODE -eq 0
            } catch { $motorDisponible = $false }
            if (-not $motorDisponible) {
                Write-Host 'Iniciando Docker Desktop para el acceso por cuenta...'
                & docker desktop start --timeout 90
                if ($LASTEXITCODE -ne 0) { throw 'Inicie el motor Linux de Docker Desktop y vuelva a abrir el portal.' }
            }
            & docker compose -f infra/identidad.compose.yml up -d keycloak
            if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar Keycloak. Revise el puerto 8081 y Docker Desktop.' }
            Write-Host 'Esperando el servicio de identidad...'
            $limite = [DateTime]::UtcNow.AddMinutes(2)
            while (-not (Test-Identidad)) {
                if ([DateTime]::UtcNow -ge $limite) { throw "Keycloak no responde en $issuer. Consulte docker compose -f infra/identidad.compose.yml logs keycloak." }
                Start-Sleep -Seconds 2
            }
        }
        $opciones = @('--app.security.enabled=true', "--app.keycloak.url=$url")
        Write-Host 'Acceso por cuenta habilitado.'
    }
    & $java -jar $jar @args @opciones
    if ($LASTEXITCODE -ne 0) { throw "El portal termino con codigo $LASTEXITCODE. Si el puerto esta ocupado, cierre la instancia anterior." }
} finally { Pop-Location }
