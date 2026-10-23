# Café Junín · Entrega 0.2

Sistema de trazabilidad del café. Un backend compartido para portal web y futuro cliente móvil sincronizado. Empieza por **LEEME-PRIMERO.md**. Esta entrega amplía la versión anterior en una carpeta independiente.

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

Recorrido integrado: cosecha → evaluación básica de calidad → riesgo → alerta si probabilidad **mayor que 70%** → confirmación o descarte por supervisor. En demo, una identidad reúne permisos para recorrerlo. Con autenticación, cada cuenta recibe sus módulos y los permisos se verifican también en backend.

No equivale a las 19 historias priorizadas completas. SQL Server y Keycloak están preparados pero pendientes de prueba real. El modelo XGBoost es real como implementación, pero su entrenamiento sintético no aporta evidencia predictiva agronómica. No usar para decisiones operativas.
