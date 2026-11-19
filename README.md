# Café Junín · Trazabilidad logística del café

La historia elegida para la entrega final es **HU08 · Registrar la evaluación física y sensorial**. El catador registra una ficha, corrige los valores fuera de escala y consulta el resultado desde el historial del lote.

**Inicio en Windows.** Con Java 17 o posterior, ejecuta **INICIAR-CAFE-JUNIN.cmd** y abre [el portal](http://localhost:8080). Registra una cosecha, evalúa el lote y consulta sus movimientos. La aplicación conserva los datos en una base H2 en archivo. Consulta el [recorrido de cata](docs/RECORRIDO-CALIDAD.md) y la [arquitectura](docs/ARQUITECTURA.md).

| Carpeta | Contenido |
|---|---|
| backend | Java 17, Spring Boot, arquitectura hexagonal, REST y pruebas |
| web | Portal React/Vite, navegación por roles, cliente Keycloak con PKCE |
| ai-service | FastAPI y XGBoost CPU; entrenamiento reproducible y modelo sintético incluido |
| database/sqlserver | Esquema y procedimientos T-SQL, instalación manual ordenada |
| infra | Docker Compose local: SQL Server, Keycloak y servicio IA; realm de prueba |
| mobile | Código Flutter de captura y SQLite local; no compilado ni conectado todavía |
| dist | JAR ejecutable que incluye el portal React |
| tests | Integración HTTP entre JAR y servicio IA |
| scripts | Inicio, compilación, pruebas, instalación y Git para Windows |
| docs | Arquitectura, alcance, conexiones, contrato y evidencia |
| .github/workflows | CI Java, React y Python, preparado para GitHub |

Los módulos de cosecha, predicción y revisión de alertas siguen disponibles por rol. El modelo XGBoost incluido se entrenó con datos sintéticos. HU08 funciona sin el servicio de IA. Para compilar el proyecto o configurar persistencia y autenticación, consulta [la guía de inicio](LEEME-PRIMERO.md).
