# Comenzar con la entrega 0.2

## Ver el portal en cinco minutos

1. Comprueba que Java sea versión 17 o posterior con **java -version**.
2. Ejecuta **INICIAR-CAFE-JUNIN.cmd** y abre http://localhost:8080.
3. En Calidad elige **Registrar cosecha** si todavía no tienes lotes. Selecciona la parcela de origen e ingresa fecha, variedad, recolección y peso. Después abre Calidad y registra la ficha.
4. Abre **Lotes y muestras → Ver detalle** para ver los movimientos, el puntaje y el perfil sensorial. Puedes cerrar y volver a iniciar el portal.

El perfil local usa una cuenta de operador en este equipo y escucha en la interfaz de bucle local. H2 guarda el archivo **state/cafejunin.mv.db**. El lanzador fija el directorio de trabajo en la raíz del proyecto. Para otro directorio configura CAFEJUNIN_DATA con la ruta base de la base de datos, sin extensión. La configuración de SQL Server y Keycloak para cuentas individuales figura en [Conexiones](docs/CONEXIONES.md).

## Editar y recompilar

Requisitos: JDK 17, Node.js 24 LTS y npm. Maven Wrapper descarga Maven 3.9.11 en su primera ejecución; ya no hace falta instalar mvn globalmente. Abre `backend/pom.xml` en NetBeans o el repositorio completo en VS Code/Codex.

En PowerShell: `./scripts/Compilar.ps1`. Instala las dependencias web, compila React, ejecuta las pruebas Java y copia el JAR a `dist`.

Para editar la web con recarga: inicia el backend, luego `cd web`, `npm ci`, `npm run dev`; abre http://localhost:5173. Vite reenvía `/api` al backend local.

## Conectar los servicios

Sigue **docs/CONEXIONES.md** para SQL Server, Keycloak, XGBoost y GitHub. Son configuraciones independientes del perfil local. No se han creado servicios remotos ni publicado el repositorio.

## Escritorio de Windows

`./scripts/Instalar-En-Escritorio.ps1` copia el proyecto a una carpeta nueva `CafeJuninV2` en tu Escritorio y se detiene si ya existe, para no sobrescribir cambios. No se puede ejecutar desde este entorno en tu computadora.
