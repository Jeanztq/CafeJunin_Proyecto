$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '../infra')
try {
 foreach ($archivo in @('00_database.sql','01_schema.sql','02_procedures.sql','03_seed.sql')) {
  docker compose exec -T sqlserver sh -c '/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -b -i "/scripts/$1"' -- $archivo
  if ($LASTEXITCODE -ne 0) { throw "Falló $archivo" }
 }
} finally { Pop-Location }
