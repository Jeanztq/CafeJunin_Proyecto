# Café Junín · Trazabilidad logística del café

La historia elegida para la entrega final es **HU08 · Registrar la evaluación física y sensorial**. El catador registra una ficha, corrige los valores fuera de escala y consulta el resultado desde el historial del lote.

**Inicio en Windows.** Con Java 17 o posterior y Docker Desktop, ejecuta **INICIAR-CAFE-JUNIN.cmd** y abre [el portal](http://localhost:8080). Ingresa como productor para registrar una cosecha y como catador para evaluarla. La aplicación conserva los datos en una base H2 en archivo. Consulta el [recorrido de cata](docs/RECORRIDO-CALIDAD.md) y la [arquitectura](docs/ARQUITECTURA.md).

| Carpeta | Contenido |
|---|---|
| backend | Java 17, Spring Boot, arquitectura hexagonal, REST y pruebas |
| web | Portal React/Vite, navegación por roles, cliente Keycloak con PKCE |
| ai-service | FastAPI y XGBoost CPU; entrenamiento reproducible y modelo sintético incluido |
| database/sqlserver | Esquema y procedimientos T-SQL, instalación manual ordenada |
| infra | Docker Compose local: SQL Server, Keycloak y servicio IA; realm de prueba |
| mobile | Cliente Flutter para Android con captura, confirmación y consulta de cosechas en SQLite |
| dist | JAR ejecutable que incluye el portal React |
| tests | Integración HTTP entre JAR y servicio IA |
| scripts | Inicio, compilación, pruebas, instalación y Git para Windows |
| docs | Arquitectura, alcance, conexiones, contrato y evidencia |
| .github/workflows | CI Java, React y Python, preparado para GitHub |

El acceso habitual utiliza las cuentas individuales de Keycloak. El productor crea el lote en **Lotes → Registrar cosecha**. La opción explícita `-OperadorLocal` reúne los tres permisos para el trabajo en un solo equipo. En **Riesgo**, el supervisor registra las mediciones y puede abrir la alerta generada. En **Alertas** consulta la evaluación de origen, registra la decisión y recupera su motivo e instrucción. Una cuenta con solo el rol de catador entra directamente en **Calidad**. El modelo XGBoost incluido se entrenó con datos sintéticos. HU08 funciona sin el servicio de IA. Para compilar el proyecto o configurar persistencia y autenticación, consulta [la guía de inicio](LEEME-PRIMERO.md).
