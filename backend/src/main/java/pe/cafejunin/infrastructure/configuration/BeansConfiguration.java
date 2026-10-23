package pe.cafejunin.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
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
      EventoRepositoryPort e,
      UnidadTrabajoPort t,
      Clock c) {
    return new RegistrarCalidadService(l, q, e, t, c);
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
