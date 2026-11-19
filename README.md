# Café Junín · Trazabilidad logística del café

La historia elegida para la entrega final es **HU08 · Registrar la evaluación física y sensorial**. El catador registra una ficha, corrige los valores fuera de escala y consulta el resultado desde el historial del lote.

**Demo en Windows.** Con Java 17 o posterior, ejecuta `INICIAR-DEMO.cmd` y abre [el portal local](http://localhost:8080). Se abre Calidad con un lote Bourbon de 120.500 kg de El Mirador. La demo usa memoria y una cuenta local de prueba. Los datos se conservan durante esa ejecución. Consulta el [guion con los tres criterios de aceptación](docs/GUION-DEMO.md) y la [arquitectura y patrones](docs/ARQUITECTURA.md).

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

Los módulos de cosecha, predicción y revisión de alertas siguen disponibles por rol. La IA de demostración se entrenó con datos sintéticos. HU08 funciona sin el servicio de IA. Para compilar el proyecto o configurar persistencia y autenticación, consulta [la guía de inicio](LEEME-PRIMERO.md).
