# Comenzar con la entrega 0.2

## Ver el portal en cinco minutos

1. Con Java 17 o posterior y Docker Desktop en modo Linux, abre **INICIAR-CAFE-JUNIN.cmd**. El lanzador inicia o reutiliza Keycloak y arranca el portal con la base H2 del proyecto.
2. Entra en http://localhost:8080 y pulsa **Iniciar sesión segura**. Para registrar una cosecha, usa la cuenta local **productor**. La clave inicial de las cuentas importadas figura en [Conexiones](docs/CONEXIONES.md#cuentas-de-prueba-keycloak).
3. En **Lotes → Registrar cosecha**, elige **La Esperanza** e ingresa fecha, variedad, recolección y peso.
4. Cierra sesión e ingresa como **catador**. En el primer acceso configura tu autenticador. Abre **Calidad**, evalúa el lote y recupera la ficha desde **Lotes y muestras → Ver detalle**.

El usuario **supervisor** accede a Riesgo y Alertas. El **transportista** todavía carece de viajes asignados. El doble factor protege a catadores y supervisores. Las cuatro cuentas importadas pertenecen al entorno local de desarrollo. En un despliegue se configuran el proveedor de identidad, las cuentas y las direcciones HTTPS propias del entorno.

**Acceso opcional sin login.** Para recorrer las operaciones en un solo equipo, ejecuta `.\INICIAR-CAFE-JUNIN.cmd -OperadorLocal`. Esta opción utiliza la cuenta combinada y vincula el servidor a 127.0.0.1. Si ya se está ejecutando el portal, guarda tus formularios y cierra su consola antes de cambiar de modo.

Ambos modos utilizan **state/cafejunin.mv.db**. Los lotes anteriores conservan su propietario. Para otro directorio configura CAFEJUNIN_DATA con la ruta base del archivo, sin extensión.

## Editar y recompilar

Requisitos: JDK 17, Node.js 24 LTS y npm. Maven Wrapper descarga Maven 3.9.11 en su primera ejecución; ya no hace falta instalar mvn globalmente. Abre `backend/pom.xml` en NetBeans o el repositorio completo en VS Code/Codex.

En PowerShell: `./scripts/Compilar.ps1`. Instala las dependencias web, compila React, ejecuta las pruebas Java y copia el JAR a `dist`.

Para editar la web con recarga: inicia el backend, luego `cd web`, `npm ci`, `npm run dev`; abre http://localhost:5173. Vite reenvía `/api` al backend local.

## Conectar los servicios

Sigue **docs/CONEXIONES.md** para SQL Server, Keycloak, XGBoost y GitHub. Son configuraciones independientes del perfil local. No se han creado servicios remotos ni publicado el repositorio.

## Escritorio de Windows

`./scripts/Instalar-En-Escritorio.ps1` copia el proyecto a una carpeta nueva `CafeJuninV2` en tu Escritorio y se detiene si ya existe, para no sobrescribir cambios. No se puede ejecutar desde este entorno en tu computadora.
