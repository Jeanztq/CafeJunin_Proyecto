package pe.cafejunin.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import pe.cafejunin.domain.builder.LoteBuilder;
import pe.cafejunin.domain.entities.Lote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.cafejunin.adapters.out.auth.CuentaSpringAdapter;
import pe.cafejunin.adapters.out.persistence.*;
import pe.cafejunin.adapters.out.prediction.*;
import pe.cafejunin.application.facade.PortalFacade;
import pe.cafejunin.application.ports.input.*;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.application.usecases.*;

@Configuration
public class BeansConfiguration {
  @Bean
  Clock reloj() {
    return Clock.systemUTC();
  }

  @Bean
  @Profile("memory")
  MemoryStore memoria() {
    return new MemoryStore();
  }

  @Bean
  @Profile("memory")
  @ConditionalOnProperty(name = "app.demo.lote", havingValue = "true")
  ApplicationRunner prepararLoteDemo(RegistrarCosechaUseCase cosecha, Clock reloj) {
    return args -> cosecha.ejecutar(new LoteBuilder()
        .identificado(UUID.fromString("cafe2401-0000-4000-8000-000000000001"))
        .deParcela(MemoryStore.PARCELA_DEMO)
        .cosechado(LocalDate.now(reloj).minusDays(1))
        .variedad(Lote.Variedad.BOURBON)
        .recoleccion(Lote.TipoRecoleccion.SELECTIVA)
        .peso(new BigDecimal("120.500")).build());
  }

  @Bean
  @Profile("sqlserver")
  SqlServerStore sqlserver(JdbcTemplate j, PlatformTransactionManager tm) {
    return new SqlServerStore(j, new TransactionTemplate(tm));
  }

  @Bean
  CuentaActualPort cuenta(@Value("${app.security.enabled:false}") boolean s) {
    return new CuentaSpringAdapter(s);
  }

  @Bean
  MedicionPrediccionDecorator predictor(
      @Value("${app.predictor.mode:rules}") String m,
      @Value("${app.predictor.url}") String u,
      @Value("${app.predictor.key:}") String k,
      ObjectMapper j) {
    return new MedicionPrediccionDecorator(PredictorFactory.crear(m, u, k, j));
  }

  @Bean
  RegistrarCosechaUseCase cosecha(
      LoteRepositoryPort l, EventoRepositoryPort e, UnidadTrabajoPort t, Clock c) {
    return new RegistrarCosechaService(l, e, t, c);
  }

  @Bean
  RegistrarCalidadUseCase calidad(
      LoteRepositoryPort l,
      CalidadRepositoryPort q,
      org.springframework.context.ApplicationEventPublisher publicador,
      UnidadTrabajoPort t,
      Clock c) {
    return new RegistrarCalidadService(l, q, publicador::publishEvent, t, c);
  }

  @Bean
  EvaluarRiesgoUseCase riesgo(
      LoteRepositoryPort l,
      RiesgoRepositoryPort r,
      PrediccionRiesgoPort p,
      EventoRepositoryPort e,
      UnidadTrabajoPort t,
      Clock c) {
    return new EvaluarRiesgoService(l, r, p, e, t, c);
  }

  @Bean
  RevisarAlertaUseCase revisar(
      RiesgoRepositoryPort r, EventoRepositoryPort e, UnidadTrabajoPort t, Clock c) {
    return new RevisarAlertaService(r, e, t, c);
  }

  @Bean
  PortalUseCase portal(
      CuentaActualPort a,
      LoteRepositoryPort l,
      CalidadRepositoryPort c,
      RiesgoRepositoryPort r,
      RegistrarCosechaUseCase co,
      RegistrarCalidadUseCase ca,
      EvaluarRiesgoUseCase ri,
      RevisarAlertaUseCase re) {
    return new PortalFacade(a, l, c, r, co, ca, ri, re);
  }
}
