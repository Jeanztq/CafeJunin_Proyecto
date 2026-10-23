# Conexiones locales pendientes

## SQL Server, Keycloak y XGBoost con Docker

Requiere Docker Desktop con contenedores Linux y memoria disponible para SQL Server. No ejecutado en el entorno de entrega.

Desde PowerShell en `infra`:

```powershell
Copy-Item .env.example .env
# Edita .env con contraseñas locales propias. No lo subas a Git.
docker compose up -d --build
# Espera que SQL Server esté healthy: docker compose ps
../scripts/Inicializar-SqlServer.ps1
```

Los puertos se vinculan a 127.0.0.1. No borres los volúmenes para resolver errores: contienen los datos. Keycloak usa start-dev y su almacenamiento local; este Compose es solo de desarrollo. No representa una instalación productiva.

SQL Server también puede instalarse directamente en Windows. Abre SSMS y ejecuta, en orden, `database/sqlserver/00_database.sql`, `01_schema.sql`, `02_procedures.sql`, `03_seed.sql`. Incluyen separadores GO; son para SSMS/sqlcmd, no para ejecutar como una única sentencia JDBC. No hay migrador automático ni eliminación de datos. El seed contiene parcelas ficticias, nunca catastros reales.

Inicia el backend desde la raíz (valores de ejemplo; usa los que configuraste):

```powershell
$env:DB_URL='jdbc:sqlserver://localhost:1433;databaseName=cafejunin;encrypt=true;trustServerCertificate=true'
$env:DB_USER='sa'
$env:DB_PASSWORD='<la contraseña de tu SQL Server local>'
$env:SECURITY_ENABLED='true'
$env:KEYCLOAK_URL='http://localhost:8081'
$env:PREDICTOR_MODE='xgboost'
$env:AI_API_KEY='<la misma clave de infra/.env>'
java -jar dist/cafejunin-backend-0.2.0.jar --spring.profiles.active=sqlserver
```

`trustServerCertificate=true` y `sa` sirven únicamente para este laboratorio local. El valor predeterminado exige validar el certificado. En producción: certificado confiable, usuario de aplicación con permisos mínimos, HTTPS y secretos fuera del repositorio.

Se puede probar Keycloak o XGBoost manteniendo memoria: usa los mismos parámetros con `--spring.profiles.active=memory`.

## Cuentas de prueba Keycloak

Realm `cafejunin`, cliente público `cafejunin-web`, autorización con PKCE S256. El backend valida firma, issuer, expiración y audiencia `cafejunin-api`; lee roles del token. La aplicación no permite elegir privilegios desde una lista antes de autenticar.

Usuarios: `productor`, `catador`, `supervisor`, `transportista`. Contraseña inicial de laboratorio: `CafeJunin-Local-2026!`. Todos son ficticios. Cambia las contraseñas antes de compartir cualquier entorno. Catador y supervisor tienen requerida la configuración de TOTP; al primer acceso Keycloak pedirá registrar un autenticador. Este recorrido con Keycloak aún debe verificarse en tu PC.

La parcela `22222222-2222-2222-2222-222222222222` pertenece al UUID del productor de prueba. La parcela `1111…` pertenece a la identidad de demo. Si creas otra cuenta, vincula `propietario_subject` de la parcela al `sub` real de esa cuenta; no al nombre de usuario. Catador y supervisor tienen acceso a los lotes del piloto de una sola cooperativa. Transporte todavía no tiene asignaciones implementadas. Consumidor no necesita una cuenta para el futuro QR público; ese flujo sigue pendiente.

La importación del realm solo crea un realm inexistente. Si ya tienes uno, revisa sus clientes/roles/usuarios; no borres el volumen sin respaldo. Las URLs autorizadas solo cubren localhost/127.0.0.1:8080 y 5173. Para otro dominio, actualiza redirects, web origins, issuer y TLS de forma consistente.

## Servicio IA sin Docker

Requiere Python 3.12, en otra terminal:

```powershell
cd ai-service
python -m venv .venv
./.venv/Scripts/Activate.ps1
pip install -r requirements.txt
$env:AI_API_KEY='<una clave local de al menos 16 caracteres>'
uvicorn app:app --host 127.0.0.1 --port 8001
```

En el backend configura la misma clave y `PREDICTOR_MODE=xgboost`. La URL por defecto es http://127.0.0.1:8001/predict. No se guardan evaluaciones cuando el servicio falla: responde 503, sin sustituirlo silenciosamente por reglas.

`python train.py --demo` reproduce el modelo sintético. `python train.py --csv ruta.csv` entrena un candidato con columnas `humedad,horasDemora,riesgo`. La etiqueta es un resultado binario observado y su definición debe acordarse con el especialista. El candidato continúa marcado como no validado: requiere revisar procedencia, separación temporal/por finca, calibración, cobertura, métricas y evaluación independiente antes de usarlo. No cambies `validated` para aparentar cumplimiento; además, la ficha fija el hash del modelo.

## Verificar SQL Server

Con los scripts inicializados y variables DB_URL, DB_USER y DB_PASSWORD definidas:

```powershell
$env:RUN_SQLSERVER_TESTS='true'
cd backend
mvn test -Dtest=SqlServerIntegrationTest
```

La prueba usa datos temporales y revierte la transacción; valida procedimientos, lecturas, alertas, revisión y outbox. Está omitida por defecto si no hay una instancia real.

## GitHub

Desde la raíz: `./scripts/Inicializar-Git.ps1`, revisa `git status`, configura tu identidad Git y crea un commit. Crea un repositorio vacío en tu cuenta y después:

```powershell
git add .
git commit -m "Implementar portal y flujo hexagonal inicial"
git remote add origin https://github.com/TU_CUENTA/TU_REPOSITORIO.git
git push -u origin main
```

No se ha ejecutado push. CI valida Java/React/Python; no prueba Keycloak, Flutter ni SQL Server automáticamente. El `.gitignore` excluye archivos `.env`, dependencias instaladas y compilaciones temporales. El modelo sintético y el JAR de entrega son artefactos explícitos; puedes distribuir el JAR en Releases en lugar de versionarlo.
