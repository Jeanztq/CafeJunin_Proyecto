# Scripts de base de datos

## Usar una instancia de SQL Server desde Windows

1. En SSMS, conéctate a la instancia elegida y abre los archivos de `database/sqlserver` como consultas. Ejecuta `00_database.sql`, `01_schema.sql`, `02_procedures.sql` y `03_seed.sql`, en ese orden. Crean la base `cafejunin`, sus tablas, los procedimientos y los datos iniciales.
2. Antes de iniciar el portal, configura el perfil y la conexión en la misma ventana de PowerShell. El ejemplo usa autenticación de SQL Server y un certificado local de desarrollo. Sustituye el servidor y el puerto por los de tu instancia. Para SQLEXPRESS verifica el puerto TCP habilitado.

```powershell
$credencial = Get-Credential -Message 'Cuenta de SQL Server con acceso a cafejunin'
$env:DB_USER = $credencial.UserName
$env:DB_PASSWORD = $credencial.GetNetworkCredential().Password
$env:DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=cafejunin;encrypt=true;trustServerCertificate=true'
$env:SPRING_PROFILES_ACTIVE = 'sqlserver'
.\INICIAR-CAFE-JUNIN.cmd
```

3. En otra terminal consulta `(Invoke-RestMethod http://localhost:8080/api/info).perfiles`. Debe incluir `sqlserver`. Tras registrar una cata, puedes consultar `SELECT COUNT(*) FROM cafejunin.dbo.evaluaciones_calidad;` en SSMS. Keycloak gestiona el acceso de los usuarios al portal.

Con el arranque habitual, el perfil `local` persiste en H2. Al elegir otra base se muestran los datos que contiene esa base. El cambio de perfil no copia los lotes que ya registraste en H2.

Esta carpeta contiene definiciones y datos de ejemplo versionables. SQL Server administra los archivos de datos fuera del repositorio. Las carpetas migrations y dev contienen SQL histórico de PostgreSQL de la primera versión. El perfil local usa H2 en archivo y aplica el esquema incluido en backend/src/main/resources/db/local.sql. El archivo de datos de state queda excluido de Git.
