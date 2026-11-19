# Arquitectura y patrones

La arquitectura del incremento es híbrida. React se comunica por HTTP con un backend modular en Spring Boot. El núcleo usa arquitectura hexagonal y las operaciones de trazabilidad registran eventos de dominio. El servicio de predicción se puede desplegar por separado. El detalle del lote lee la secuencia de cosecha, cata y, para el supervisor, riesgo y revisión. Los eventos se conservan tanto antes como después de una futura publicación externa.

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

Cada escritura de negocio y su sobre de evento se guardan en la misma unidad de trabajo. Tanto H2 como SQL Server utilizan TransactionTemplate; en memoria se usa snapshot y rollback sincronizado. El servicio de predicción se llama antes de abrir la transacción para evitar mantener bloqueos mientras se espera HTTP.

En HU08, RegistrarCalidadService valida el rol, comprueba el lote y reconoce reintentos por UUID de ficha. Persiste la evaluación y publica EvaluacionCalidadRegistrada a través de PublicarCalidadPort. Spring entrega el evento al consumidor local EncolarCalidadRegistrada, que escribe el sobre en el outbox dentro de la misma transacción. Un fallo del consumidor revierte la ficha. El sobre de calidad versión 2 identifica tanto el lote como la evaluación. Los sobres previos conservan su versión.

La entrega al consumidor local es síncrona. La tabla outbox conserva eventos pendientes para un futuro publicador externo. El flujo de la ficha se ejecuta en un solo backend.

## Acceso y límites

Con seguridad activa, JWT verificado → CuentaActualPort → Facade → caso de uso → repositorios/eventos. El productor solo consulta y registra en sus parcelas. Supervisor/catador operan sobre los lotes de una cooperativa. No hay aislamiento entre múltiples cooperativas implementado. El transportista no ve lotes hasta implementar sus asignaciones. La UI oculta módulos y el servidor vuelve a verificar permisos.

React usa Keycloak con PKCE y token en memoria. No se guardan tokens en localStorage. Cambios de rol efectivos al renovar sesión/token. El arranque local usa una identidad compartida y escucha en 127.0.0.1. Para operación multiusuario se configura OIDC con autorización por rol.

No usar herencia de clases para representar permisos por obligación del diagrama: Usuario es el actor general; las especialidades se expresan como roles de cuenta. Una cuenta puede tener varios roles autorizados y se muestra la unión de módulos.
