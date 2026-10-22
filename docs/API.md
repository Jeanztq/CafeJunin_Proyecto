# Contrato REST del incremento 1

Base local: `http://localhost:8080/api`. JSON UTF-8. Ver ejemplos en `scripts/Demostrar-API.ps1`.

| Método | Ruta | Respuesta | Rol cuando OIDC está activo |
|---|---|---|---|
| GET | /info | 200, estado del prototipo | Público, sin datos del negocio |
| POST | /lotes | 201, lote | PRODUCTOR |
| GET | /lotes | 200, lista | PRODUCTOR, CATADOR, SUPERVISOR, TRANSPORTISTA |
| POST | /lotes/{uuid}/evaluaciones | 201, evaluación | CATADOR |
| POST | /lotes/{uuid}/riesgos | 201, predicción simulada | SUPERVISOR |
| GET | /alertas | 200, lista | SUPERVISOR |

## Registrar lote

```json
{"id":"22222222-2222-2222-2222-222222222222","parcelaId":"11111111-1111-1111-1111-111111111111","fechaCosecha":"2025-01-01","variedad":"BOURBON","tipoRecoleccion":"SELECTIVA","pesoKg":120.5}
```

## Evaluar calidad

```json
{"defectosPrimarios":0,"defectosSecundarios":2,"puntajeSca":85}
```

## Evaluar riesgo

```json
{"humedad":22,"horasDemora":12}
```

Probabilidad se expresa entre 0 y 1. `simulada:true` y `modelo:SIMULADOR-REGLAS-v1` identifican el mock.
En el lote de respuesta, peso aparece como `peso.kilogramos`. UUID, fechas y enumeraciones son obligatorios.
Errores: 400 entrada inválida, 404 referencia inexistente, 409 conflicto. OIDC añade 401/403.
No hay paginación, API pública QR ni especificación OpenAPI generada todavía.

## Activación OIDC — integración pendiente con su realm

Configure un realm Keycloak con roles PRODUCTOR, CATADOR, SUPERVISOR y TRANSPORTISTA.
Antes de arrancar desde la consola:

```powershell
$env:SECURITY_ENABLED='true'
$env:SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUERURI='https://su-servidor/realms/cafejunin'
```

Consuma la API con `Authorization: Bearer <access_token>` obtenido mediante el flujo de su cliente OIDC.
El servidor debe ser accesible y emitir tokens válidos. No se suministra un realm ni usuarios reales.
La demo HTML no implementa ese flujo; con seguridad activa su acceso se deniega.
Queda pendiente validar audiencia del token, propiedad de recursos, MFA y demás controles del informe.
