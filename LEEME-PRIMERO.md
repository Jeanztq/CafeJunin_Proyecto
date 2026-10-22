# Café Junín — empezar aquí

Prototipo inicial para el proyecto de trazabilidad del café. Backend hexagonal Java/Spring Boot.
**Implementado:** tres casos de uso, interfaz web demo, pruebas, persistencia en memoria o PostgreSQL,
procedimientos almacenados, migraciones y registro transaccional de eventos pendientes.
**Pendiente:** portal React por actor, login de clientes, app offline, modelo IA real, Kafka y el resto de RF/RNF.

## Colocarlo en el Escritorio de Windows

1. Extraiga el ZIP. Abra la carpeta `CafeJunin`.
2. Puede mover esa carpeta directamente al Escritorio.
3. Alternativamente, abra PowerShell en esa carpeta y ejecute:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Instalar-En-Escritorio.ps1
```

El script copia el proyecto a la ruta de Escritorio que informa Windows, incluido OneDrive si corresponde.
No sobrescribe una carpeta `CafeJunin` existente. La opción de ejecución sólo afecta ese proceso.

## Ejecutar la demo sin Maven

Instale JDK 17. Haga doble clic en `INICIAR-DEMO.cmd` y mantenga abierta la consola.
Cuando aparezca `Started CafeJuninApplication`, abra http://localhost:8080.
Se incluye `dist/cafejunin-backend-0.1.0.jar`, generado desde el código de esta entrega.
No necesita Maven para esta opción. Al modificar el código, debe reconstruir el ejecutable.

## Ejecutar desde el código sin base de datos

Instale JDK 17 y Maven 3.9 o posterior. JDK 17 es la referencia para esta entrega; no depende del JDK 24 usado en otros ejercicios.
Compruebe `java -version` y `mvn -version`. Abra la carpeta `backend` como proyecto Maven en NetBeans, IntelliJ o VS Code.
Desde PowerShell en la raíz:

```powershell
.\scripts\Iniciar-Demo.ps1
```

Si su política de PowerShell bloquea scripts, puede usar directamente:

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=memory
```

Abra http://localhost:8080. Registre una cosecha; su ID se trasladará a calidad y riesgo.
El modo memoria pierde sus datos al cerrar el backend. No necesita PostgreSQL, Docker ni Keycloak.
La primera descarga de Maven necesita Internet.

## Verificación

```powershell
cd backend
mvn verify
```

Consulte `docs/VALIDACION.md` para saber qué se comprobó realmente al entregar.
Para probar desde otra consola mientras el backend está encendido: `scripts/Demostrar-API.ps1`.

## Siguiente lectura

- `README.md`: mapa de carpetas.
- `docs/ARQUITECTURA.md`: puertos, adaptadores y patrones.
- `docs/CASOS-DE-USO.md`: tres casos y criterios Given–When–Then.
- `database/README.md`: PostgreSQL y migraciones.
- `docs/GITHUB.md`: control de versiones y publicación.
- `docs/ALCANCE-Y-PENDIENTES.md`: avance real y límites.

La interfaz demo no tiene autenticación y sólo se publica en loopback por defecto. No desplegar ese modo en Internet.
