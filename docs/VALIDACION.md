# Validación de la entrega

Fecha: 22 de octubre de 2026. JDK 17.0.20, Maven 3.9.11 y Spring Boot 3.5.16.

## Comprobado

- Compilación del código Java y generación del JAR ejecutable.
- 12 pruebas aprobadas: 8 de casos de uso, 2 REST, 1 de arquitectura y 1 de política de acceso por roles con tokens simulados.
- Una prueba PostgreSQL incluida y omitida por condición de entorno; no se cuenta como aprobada.
- Arranque real del ejecutable en modo memoria.
- Peticiones HTTP reales: cosecha, calidad, riesgo simulado y consulta de alertas.
- Interfaz HTML incluida y servida desde el ejecutable.
- Sintaxis JavaScript comprobada con Node.
- Formularios de cosecha, calidad y riesgo, propagación del ID y consulta de alertas ejecutados en un DOM simulado con jsdom contra el backend real. No se afirma una revisión visual de navegador.
- SQL de migración y cuerpos de cinco procedimientos analizados con el parser PostgreSQL de pglast. Esto no sustituye una ejecución contra PostgreSQL.

Resumen de suites: `evidencias/pruebas-backend.json`.

## No comprobado en este entorno

- Ejecución contra PostgreSQL real: el entorno no permitió iniciar el servidor bajo otro usuario y no disponía de Docker. La prueba de integración y el workflow quedan preparados para ejecutarse en el equipo/GitHub.
- Integración con Keycloak real, firma y validación de tokens emitidos por su realm.
- Scripts PowerShell/CMD en Windows; se entregan para ejecución local, sin afirmar que se ejecutaron en su Escritorio.
- Flutter, Android, SQLite, funcionamiento offline, modelo IA y Kafka: no implementados salvo la base indicada en el alcance.
- Revisión visual con Chromium, rendimiento, WCAG completa y funcionamiento productivo.

El JAR de `dist` corresponde a este código. Si lo modifica, vuelva a compilar y reemplace el JAR.
