# Corrección de compilación local y GitHub

Diagnóstico realizado contra el commit 29099c4c3dbf4428bf415d7fc09ee858f24ebf48 del repositorio Jeanztq/CafeJunin_Proyecto.

1. El repositorio mezclaba versiones: PostgresStore de 0.1 ya no implementaba los métodos de LoteRepositoryPort de 0.2. Se reprodujo el error de compilación listarParcelas(). Se retiran únicamente ese adaptador, su prueba y el perfil postgres antiguos. La implementación actual es SqlServerStore; no se borran bases de datos ni historial Git.
2. PowerShell no encontraba mvn. Se añade Maven Wrapper oficial, Maven3.9.11, con checksum de distribución. Compilar.ps1 y Probar.ps1 invocan mvnw.cmd y verifican herramientas antes de instalar/compilar el portal. Requiere Internet en el primer uso, JDK17 y Node24LTS.
3. Vitest no admite Node25.1.0 según su contrato de engines. Se documenta y comprueba Node22.12+,24 o26+; se recomienda24LTS y CI utiliza .nvmrc=24. Instala Node24LTS desde https://nodejs.org/ y vuelve a abrir PowerShell. npm no tiene que actualizarse para solucionar estos errores.

Los avisos whatwg-encoding y npm fund no causaban el fallo. FallÃ³ era un problema de interpretación UTF-8 en PowerShell5.1; los scripts corregidos incluyen BOM y mensajes compatibles.

## Aplicar la corrección descargada

Extrae el ZIP fuera del repositorio. Revisa APLICAR-CORRECCION.ps1: copia los archivos nuevos/modificados sobre la raíz que elijas y elimina SOLO los tres archivos de la lista anterior si coinciden por hash con los archivos legados revisados. Se detiene antes de realizar cambios si alguno tiene modificaciones distintas. No reemplaza todo el proyecto, no toca .env ni hace commits/push.

Desde PowerShell, en la carpeta extraída:

```powershell
./APLICAR-CORRECCION.ps1 -Repositorio 'C:\Users\USER\Documents\GitHub\CafeJunin_Proyecto'
```

Después, en la raíz del repositorio:

```powershell
node -v
java -version
./scripts/Compilar.ps1
./scripts/Probar.ps1
git status
```

Comprueba que node muestre v24.x. En GitHub Desktop revisa los cambios, crea un commit y pulsa Push origin. El nuevo push volverá a ejecutar Actions; no basta Re-run sobre el commit antiguo, porque conserva el código incompatible.

Si solo quieres abrir la demo ya compilada, INICIAR-DEMO.cmd requiere Java17; Maven/Node se necesitan para reconstruir el proyecto.

## Verificación y límites

Se reproduce primero el fallo del adaptador legado y después se ejecutan las pruebas de la copia corregida. La prueba de SQL Server permanece omitida si no se dispone de instancia real. Los scripts de Windows se entregan para ejecutarlos en tu PC; este entorno no ejecuta Windows PowerShell. No se han publicado cambios remotos ni se declara aprobado el nuevo workflow antes de un push.
