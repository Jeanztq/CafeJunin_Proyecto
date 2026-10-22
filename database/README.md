# Base de datos PostgreSQL

SQL de producción del incremento 1: `migrations/V1__nucleo_trazabilidad.sql`.
Flyway lo incluye y aplica al iniciar el backend con perfil `postgres`. No ejecutarlo manualmente además de Flyway.
La tabla `flyway_schema_history` es administrada por Flyway. Añada V2, V3, etc. para cambios posteriores; no edite una V1 ya aplicada.

## Inicio en Windows con Docker Desktop

Desde la raíz:

```powershell
Copy-Item .env.example .env
# Cambie la contraseña en .env. No lo añada a Git.
docker compose --env-file .env -f infra/docker-compose.yml up -d
```

En otra consola, configure el backend con los mismos valores que puso en `.env`:

```powershell
$env:DB_USER='cafejunin'
$env:DB_PASSWORD='su-clave-local'
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Maven **no carga automáticamente** el `.env` de Compose. Las variables anteriores deben coincidir con él.
Si cambia DB_NAME, use también `$env:DB_URL='jdbc:postgresql://localhost:5432/otro_nombre'`.
DB_URL debe ser la URL base, sin parámetros `?`; el perfil agrega el parámetro del controlador.

Después del primer arranque y con las migraciones aplicadas, cargue la parcela demo desde la raíz:

```powershell
Get-Content -Raw -Encoding UTF8 database/dev/parcelas_demo.sql | docker compose --env-file .env -f infra/docker-compose.yml exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

También puede ejecutar ese SQL en pgAdmin. La parcela de demostración no es una parcela georreferenciada ni una validación EUDR.
Los datos de PostgreSQL se conservan en el volumen. `docker compose down` conserva el volumen; no use `down -v` si quiere conservar sus datos.

## Pruebas con PostgreSQL real

Con PostgreSQL encendido y DB_PASSWORD configurada:

```powershell
$env:RUN_POSTGRES_TESTS='true'
cd backend
mvn -Dtest=PostgresIntegrationTest test
```

La prueba crea y limpia sus propios registros; no requiere la parcela demo. GitHub Actions también configura este recorrido.
No contiene usuarios reales ni contraseñas de actores. Keycloak será la fuente de identidad.
