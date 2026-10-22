$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api'
$id = [guid]::NewGuid().ToString()
$body = @{id=$id;parcelaId='11111111-1111-1111-1111-111111111111';fechaCosecha='2025-01-01';variedad='BOURBON';tipoRecoleccion='SELECTIVA';pesoKg=120.5} | ConvertTo-Json
Invoke-RestMethod "$base/lotes" -Method Post -ContentType 'application/json' -Body $body
Invoke-RestMethod "$base/lotes/$id/evaluaciones" -Method Post -ContentType 'application/json' -Body '{"defectosPrimarios":0,"defectosSecundarios":2,"puntajeSca":85}'
Invoke-RestMethod "$base/lotes/$id/riesgos" -Method Post -ContentType 'application/json' -Body '{"humedad":22,"horasDemora":12}'
Invoke-RestMethod "$base/alertas"
