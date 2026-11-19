# Comenzar con la entrega 0.2

## Ver el portal en cinco minutos

1. Comprueba que Java sea versión 17 o posterior con `java -version`.
2. Ejecuta `INICIAR-DEMO.cmd` desde la carpeta del proyecto y abre http://localhost:8080.
3. En Calidad aparece el lote de ejemplo. Sigue [el guion de HU08](docs/GUION-DEMO.md) para registrar y consultar la ficha.
4. Para crear otra muestra, puedes abrir Cosecha y registrar otro lote. Una segunda evaluación del mismo lote también recibe un identificador propio.

Al detener la demo se pierden sus registros. El JAR ya contiene React; no hace falta Node ni Maven para verla. No exponer este modo a Internet.

## Editar y recompilar

Requisitos: JDK 17, Node.js 24 LTS y npm. Maven Wrapper descarga Maven 3.9.11 en su primera ejecución; ya no hace falta instalar mvn globalmente. Abre `backend/pom.xml` en NetBeans o el repositorio completo en VS Code/Codex.

En PowerShell: `./scripts/Compilar.ps1`. Instala las dependencias web, compila React, ejecuta las pruebas Java y copia el JAR a `dist`.

Para editar la web con recarga: inicia el backend, luego `cd web`, `npm ci`, `npm run dev`; abre http://localhost:5173. Vite reenvía `/api` al backend local.

## Conectar los servicios

Sigue **docs/CONEXIONES.md** para SQL Server, Keycloak, XGBoost y GitHub. Son pasos distintos de la demo en memoria. No se han creado servicios remotos ni publicado el repositorio.

## Escritorio de Windows

`./scripts/Instalar-En-Escritorio.ps1` copia el proyecto a una carpeta nueva `CafeJuninV2` en tu Escritorio y se detiene si ya existe, para no sobrescribir cambios. No se puede ejecutar desde este entorno en tu computadora.
