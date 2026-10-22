# Café Junín

Primera implementación académica de un sistema de trazabilidad del café de Junín.
Comience por **LEEME-PRIMERO.md**.

| Carpeta | Contenido |
|---|---|
| backend | API Java/Spring Boot, dominio puro, puertos, casos de uso, adaptadores y pruebas |
| web | Interfaz HTML de demostración funcional; futuro portal React |
| mobile | Base Flutter y organización prevista; aún sin captura ni sincronización |
| database/migrations | Esquema y procedimientos SQL versionados con Flyway |
| database/dev | Datos ficticios de desarrollo |
| infra | Docker Compose para PostgreSQL |
| docs | Arquitectura, casos, modelo, contrato y evidencia de validación |
| scripts | Inicio, pruebas, demostración y copia al Escritorio en Windows |
| .github/workflows | Validación del backend y PostgreSQL en GitHub Actions |

Los modos `memory` y `postgres` son alternativas de persistencia, no sistemas distintos.
Los clientes web y móvil utilizarán el mismo backend. No se crearon microservicios ni un backend por actor.
No se publicó ningún repositorio remoto. `.env.example` es una plantilla, no contiene credenciales reales.

Ejecutable listo en `dist/`. Con JDK 17, use `INICIAR-DEMO.cmd` en Windows. No requiere Maven para iniciar esa demo.
