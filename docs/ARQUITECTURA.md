# Arquitectura y patrones

El núcleo `domain` y `application` no importa Spring, JDBC ni adaptadores. Los casos de uso dependen de puertos; `infrastructure/configuration` conecta sus implementaciones. Una prueba verifica esa independencia. React y Flutter son clientes separados del mismo backend, con una identidad central prevista para ambos. El móvil aún no autentica ni sincroniza.

| Patrón | Implementación | Propósito |
|---|---|---|
| Adapter | TrazabilidadController, SqlServerStore, CuentaSpringAdapter, HttpXgboostAdapter | Traducir HTTP/JWT/JDBC/IA hacia los contratos del núcleo |
| Facade | PortalFacade implementa PortalUseCase | Presentar operaciones del portal y aplicar roles/pertenencia; delegar reglas a casos de uso |
| Decorator | MedicionPrediccionDecorator | Contar llamadas, fallos y tiempo del predictor sin cambiarlo ni reintentar |
| Factory Method | EventoFactory y sus creadores concretos | Construir sobres de eventos por operación |
| Simple Factory | PredictorFactory | Elegir reglas o adaptador XGBoost según configuración |
| Builder | LoteBuilder | Construir un lote validado desde el contrato de entrada |
| Singleton administrado | Beans de Spring, incluido el predictor decorado | Una instancia por bean/contexto, con inyección; no es un singleton global de JVM |
| Repository | LoteRepositoryPort, CalidadRepositoryPort, RiesgoRepositoryPort | Independizar la lógica de memoria o SQL Server |
| Strategy | PrediccionRiesgoPort | Intercambiar algoritmos/adaptadores sin modificar el caso de uso |
| Dependency Injection | BeansConfiguration | Componer servicios y adaptadores explícitamente |

Factory no crea procedimientos almacenados. Los procedimientos se definen en T-SQL; SqlServerStore los invoca mediante JDBC. Los repositorios y el pool se comparten; cada Builder se crea para una operación.

## Eventos dentro de hexagonal

Cada escritura de negocio y su sobre de evento se guardan en la misma unidad de trabajo. En SQL Server participa TransactionTemplate; en memoria se usa snapshot y rollback sincronizado. El servicio de predicción se llama antes de abrir la transacción para evitar mantener bloqueos mientras se espera HTTP.

Outbox almacena identificador, tipo, agregado, instante y versión; no el contenido completo del evento. Aún no tiene publicador, broker, confirmación de envío, consumidores, reintentos durables ni deduplicación de consumidores. Por ello esta entrega incorpora la base transaccional para eventos, no una arquitectura distribuida event driven completa. Antes de añadir consumidores se deben versionar payloads y su política de entrega. No se introduce Kafka solo por tener cliente móvil.

## Acceso y límites

Con seguridad activa, JWT verificado → CuentaActualPort → Facade → caso de uso → repositorios/eventos. El productor solo consulta y registra en sus parcelas. Supervisor/catador operan sobre los lotes del piloto de una cooperativa. No hay aislamiento entre múltiples cooperativas implementado. El transportista no ve lotes hasta implementar sus asignaciones. La UI oculta módulos y el servidor vuelve a verificar permisos.

React usa Keycloak con PKCE y token en memoria. No se guardan tokens en localStorage. Cambios de rol efectivos al renovar sesión/token. La demo desactiva autenticación explícitamente y solo escucha loopback por defecto; el modo de demostración no satisface autenticación real.

No usar herencia de clases para representar permisos por obligación del diagrama: Usuario es el actor general; las especialidades se expresan como roles de cuenta. Una cuenta puede tener varios roles autorizados y se muestra la unión de módulos.
