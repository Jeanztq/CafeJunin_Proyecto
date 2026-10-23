# Comenzar con la entrega 0.2

## Ver el portal en cinco minutos

1. Extrae todo el ZIP; conserva `CafeJuninV2` como carpeta separada de la versión anterior.
2. Instala JDK 17 y comprueba `java -version`.
3. En Windows ejecuta `INICIAR-DEMO.cmd`, o desde esta carpeta: `java -jar dist/cafejunin-backend-0.2.0.jar --spring.profiles.active=memory`.
4. Abre http://localhost:8080. La demo no pide credenciales, muestra el aviso correspondiente y guarda datos solo en memoria.
5. Registra una cosecha con una parcela de la lista, consulta Lotes, registra Calidad y prueba Riesgo con humedad ambiental 95 y demora 60. En el simulador esto crea una alerta. Confírmala con motivo e instrucción o descártala con motivo.

Al detener la demo se pierden sus registros. El JAR ya contiene React; no hace falta Node ni Maven para verla. No exponer este modo a Internet.

## Editar y recompilar

Requisitos: JDK 17, Maven 3.9+, Node 22.12+ y npm. Abre `backend/pom.xml` en NetBeans o el repositorio completo en VS Code/Codex.

En PowerShell: `./scripts/Compilar.ps1`. Instala las dependencias web, compila React, ejecuta las pruebas Java y copia el JAR a `dist`.

Para editar la web con recarga: inicia el backend, luego `cd web`, `npm ci`, `npm run dev`; abre http://localhost:5173. Vite reenvía `/api` al backend local.

## Conectar los servicios

Sigue **docs/CONEXIONES.md** para SQL Server, Keycloak, XGBoost y GitHub. Son pasos distintos de la demo en memoria. No se han creado servicios remotos ni publicado el repositorio.

## Escritorio de Windows

`./scripts/Instalar-En-Escritorio.ps1` copia el proyecto a una carpeta nueva `CafeJuninV2` en tu Escritorio y se detiene si ya existe, para no sobrescribir cambios. No se puede ejecutar desde este entorno en tu computadora.

## Próximo paso contigo

Primero ejecutar SQL Server y Keycloak en tu PC y verificar acceso con productor, catador y supervisor. Después completar la ficha de calidad y la autenticación/sincronización móvil. No necesitas una app de escritorio específica para seguir; sí un entorno de Codex con acceso a la carpeta del proyecto si quieres que modifique y pruebe tus archivos directamente.
