package pe.cafejunin.infrastructure.configuration;

import java.time.Clock;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.cafejunin.adapters.out.persistence.*;
import pe.cafejunin.adapters.out.prediction.SimuladorRiesgoAdapter;
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
  @Profile("postgres")
  PostgresStore postgres(JdbcTemplate jdbc, PlatformTransactionManager tm) {
    return new PostgresStore(jdbc, new TransactionTemplate(tm));
  }

  @Bean
  PrediccionRiesgoPort predictor() {
    return new SimuladorRiesgoAdapter();
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
  ConsultarTrazabilidadUseCase consulta(LoteRepositoryPort l, RiesgoRepositoryPort r) {
    return new ConsultarTrazabilidadService(l, r);
  }
}
