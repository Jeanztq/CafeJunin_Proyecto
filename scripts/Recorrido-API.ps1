$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api'
$id = [guid]::NewGuid().ToString()
$body = @{id=$id;parcelaId='11111111-1111-1111-1111-111111111111';fechaCosecha='2025-01-01';variedad='BOURBON';tipoRecoleccion='SELECTIVA';pesoKg=120.5} | ConvertTo-Json
Invoke-RestMethod "$base/lotes" -Method Post -ContentType 'application/json' -Body $body
$ficha = @{
  id = [guid]::NewGuid().ToString()
  defectosPrimarios = 0
  defectosSecundarios = 2
  atributos = @{ fragancia=8; sabor=8.5; acidez=8; cuerpo=8.5; posgusto=8; balance=8; uniformidad=10; dulzor=10; tazaLimpia=10; apreciacionGlobal=8 }
  defectosTaza = @{ leves=1; graves=0 }
} | ConvertTo-Json
Invoke-RestMethod "$base/lotes/$id/evaluaciones" -Method Post -ContentType 'application/json' -Body $ficha
Invoke-RestMethod "$base/lotes/$id/riesgos" -Method Post -ContentType 'application/json' -Body '{"humedad":95,"horasDemora":60}'
Invoke-RestMethod "$base/alertas"
