# Funciones compartidas. Compatible con Windows PowerShell 5.1 y PowerShell 7.
function Test-CafeEntorno {
    if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
        throw 'Falta Node.js. Instala Node.js 24 LTS y vuelve a abrir PowerShell: https://nodejs.org/'
    }
    & node (Join-Path $PSScriptRoot 'comprobar-node.cjs')
    if ($LASTEXITCODE -ne 0) { throw 'Version de Node no compatible. Cambia a Node.js 24 LTS antes de compilar.' }
    if (-not (Get-Command npm.cmd -ErrorAction SilentlyContinue)) {
        throw 'No se encontro npm.cmd. Reinstala Node.js 24 LTS y vuelve a abrir PowerShell.'
    }
    if ($env:JAVA_HOME) {
        if (-not (Test-Path (Join-Path $env:JAVA_HOME 'bin/java.exe'))) {
            throw 'JAVA_HOME apunta a una carpeta incorrecta. Debe apuntar al JDK (por ejemplo JDK 17), no a su carpeta bin.'
        }
    } elseif (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Falta Java. Instala JDK 17 y configura JAVA_HOME o el PATH antes de compilar.'
    }
    if (-not (Test-Path (Join-Path $PSScriptRoot '../backend/mvnw.cmd'))) {
        throw 'Falta backend/mvnw.cmd. Aplica todos los archivos de la correccion, incluida la carpeta backend/.mvn.'
    }
}
function Invoke-CafeNpm {
    param([Parameter(Mandatory=$true)][string[]] $Arguments)
    & npm.cmd @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Fallo npm $($Arguments -join ' ') (codigo $LASTEXITCODE)." }
}
function Invoke-CafeMaven {
    param([Parameter(Mandatory=$true)][string[]] $Arguments)
    $wrapper = Join-Path $PSScriptRoot '../backend/mvnw.cmd'
    & $wrapper @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Fallo Maven Wrapper (codigo $LASTEXITCODE). Revisa el mensaje anterior." }
}
