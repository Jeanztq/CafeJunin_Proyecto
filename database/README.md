# Scripts de base de datos

El perfil sqlserver usa SQL Server. Ejecuta los archivos de **sqlserver** del 00 al 03, en orden, con SSMS o sqlcmd. El esquema y los procedimientos se pueden aplicar sobre la entrega anterior. HU08 agrega la ficha sensorial y la referencia al evento. La conexión, las credenciales locales y la prueba JDBC se describen en [Conexiones](../docs/CONEXIONES.md).

Esta carpeta contiene definiciones y datos de ejemplo versionables. SQL Server administra los archivos de datos fuera del repositorio. Las carpetas migrations y dev contienen SQL histórico de PostgreSQL de la primera versión. El perfil local usa H2 en archivo y aplica el esquema incluido en backend/src/main/resources/db/local.sql. El archivo de datos de state queda excluido de Git.
