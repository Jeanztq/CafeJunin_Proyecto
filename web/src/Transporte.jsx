import React, { useEffect, useState } from "react";
import { api } from "./api";

const base = "/transporte/envios";
const corto = (id) => id.slice(0, 8);
const tipos = {
  CONTROL: "Control de ruta",
  DESVIO: "Desvío",
  DERRUMBE: "Derrumbe",
  FALLA_MECANICA: "Falla mecánica",
};
const fecha = (instante) =>
  new Date(instante).toLocaleString("es-PE", { timeZone: "America/Lima" });
const guardar = (ruta, datos) =>
  api(ruta, { method: "PUT", body: JSON.stringify(datos) });

export default function Transporte({ cuenta, lotes }) {
  const [seleccion, seleccionar] = useState(null);
  const supervisor = cuenta.roles.includes("SUPERVISOR");
  if (seleccion)
    return (
      <Ficha
        key={seleccion}
        loteId={seleccion}
        cuenta={cuenta}
        volver={() => seleccionar(null)}
      />
    );
  return <Lista supervisor={supervisor} lotes={lotes} abrir={seleccionar} />;
}

function Lista({ supervisor, lotes, abrir }) {
  const [lista, setLista] = useState(null);
  const [error, setError] = useState("");
  const [intento, setIntento] = useState(0);
  const [ocupado, setOcupado] = useState(false);
  useEffect(() => {
    let vigente = true;
    setError("");
    setLista(null);
    Promise.all([
      api(base),
      supervisor ? api("/transporte/transportistas") : [],
    ])
      .then(([envios, conductores]) => {
        if (vigente) setLista({ envios, conductores });
      })
      .catch((e) => {
        if (vigente) setError(e.message);
      });
    return () => {
      vigente = false;
    };
  }, [supervisor, intento]);
  async function asignar(e) {
    e.preventDefault();
    if (ocupado) return;
    const datos = new FormData(e.currentTarget);
    const lote = datos.get("lote");
    setOcupado(true);
    setError("");
    try {
      await guardar(base + "/" + lote, {
        transportista: datos.get("transportista"),
      });
      abrir(lote);
    } catch (e) {
      setError(e.message);
    } finally {
      setOcupado(false);
    }
  }
  const asignados = new Set(lista?.envios.map((e) => e.loteId));
  const disponibles = lotes.filter((l) => !asignados.has(l.id));
  return (
    <>
      {error && (
        <p role="alert" className="error">
          {error}{" "}
          <button
            className="secondary"
            onClick={() => setIntento((i) => i + 1)}
          >
            Actualizar lista
          </button>
        </p>
      )}
      {!lista && !error && <p role="status">Cargando envíos…</p>}
      {lista && (
        <>
          {supervisor && (
            <section>
              <h2>Asignar un lote</h2>
              {!disponibles.length ? (
                <p>
                  Todos los lotes registrados están asignados o aún no hay
                  cosechas.
                </p>
              ) : !lista.conductores.length ? (
                <p>No hay transportistas habilitados.</p>
              ) : (
                <form onSubmit={asignar}>
                  <div className="form-grid">
                    <label>
                      Lote
                      <select required name="lote" defaultValue="">
                        <option value="">Selecciona un lote</option>
                        {disponibles.map((l) => (
                          <option key={l.id} value={l.id}>
                            {corto(l.id)} · {l.variedad} · {l.peso.kilogramos}{" "}
                            kg
                          </option>
                        ))}
                      </select>
                    </label>
                    <label>
                      Transportista
                      <select required name="transportista" defaultValue="">
                        <option value="">Selecciona un transportista</option>
                        {lista.conductores.map((t) => (
                          <option key={t.subject} value={t.subject}>
                            {t.nombre}
                          </option>
                        ))}
                      </select>
                    </label>
                  </div>
                  <button disabled={ocupado}>Asignar envío</button>
                </form>
              )}
            </section>
          )}
          <section>
            <h2>{supervisor ? "Seguimiento de envíos" : "Envíos asignados"}</h2>
            {!lista.envios.length && <p>Sin envíos asignados.</p>}
            {lista.envios.map((envio) => (
              <article className="alert-row" key={envio.loteId}>
                <div>
                  <span className="pill">
                    {envio.salida ? "En tránsito" : "Por despachar"}
                  </span>
                  <h3>Lote {corto(envio.loteId)}</h3>
                  {supervisor && (
                    <p>
                      {lista.conductores.find(
                        (t) => t.subject === envio.transportista,
                      )?.nombre ?? envio.transportista}
                    </p>
                  )}
                  <small>Asignado el {fecha(envio.asignadoEn)}</small>
                </div>
                <button
                  className="secondary"
                  onClick={() => abrir(envio.loteId)}
                >
                  Ver envío
                </button>
              </article>
            ))}
          </section>
        </>
      )}
    </>
  );
}

function Ficha({ loteId, cuenta, volver }) {
  const ruta = base + "/" + loteId;
  const [ficha, setFicha] = useState(null);
  const [error, setError] = useState("");
  const [intento, setIntento] = useState(0);
  const [ocupado, setOcupado] = useState(false);
  const [incidencia, setIncidencia] = useState(false);
  const [confirmacion, setConfirmacion] = useState("");
  const [operacion, setOperacion] = useState(() => crypto.randomUUID());
  useEffect(() => {
    let vigente = true;
    setError("");
    setFicha(null);
    api(ruta)
      .then((f) => {
        if (!vigente) return;
        setFicha(f);
        const siguiente = crypto.randomUUID();
        setOperacion((id) =>
          f.novedades.some((n) => n.id === id) ? siguiente : id,
        );
      })
      .catch((e) => {
        if (vigente) setError(e.message);
      });
    return () => {
      vigente = false;
    };
  }, [ruta, intento]);

  async function registrar(e) {
    e.preventDefault();
    if (ocupado) return;
    const form = e.currentTarget;
    const datos = Object.fromEntries(new FormData(form));
    setOcupado(true);
    setError("");
    setConfirmacion("");
    try {
      if (!ficha.envio.salida) {
        const envio = await guardar(ruta + "/despacho", {
          placa: datos.placa,
          pesoCargaKg: Number(datos.peso),
        });
        setFicha({ ...ficha, envio });
        setConfirmacion("Salida registrada. El envío está en tránsito.");
      } else {
        const novedad = await guardar(ruta + "/novedades/" + operacion, {
          tipo: incidencia ? datos.tipo : "CONTROL",
          lugar: datos.lugar,
          descripcion: incidencia ? datos.descripcion : datos.observaciones,
        });
        setFicha({ ...ficha, novedades: [...ficha.novedades, novedad] });
        setOperacion(crypto.randomUUID());
        form.reset();
        setIncidencia(false);
        setConfirmacion(
          (incidencia ? "Incidencia reportada en " : "Paso registrado en ") +
            novedad.lugar +
            ".",
        );
      }
    } catch (e) {
      setError(e.message);
    } finally {
      setOcupado(false);
    }
  }
  const conductor =
    cuenta.roles.includes("TRANSPORTISTA") &&
    cuenta.subject === ficha?.envio.transportista;
  return (
    <section className="ficha-transporte">
      <button className="secondary" onClick={volver}>
        Volver a envíos
      </button>
      {error && (
        <p role="alert" className="error">
          {error}{" "}
          <button
            className="secondary"
            onClick={() => setIntento((n) => n + 1)}
          >
            Actualizar envío
          </button>
        </p>
      )}
      {!ficha && !error && <p role="status">Cargando envío…</p>}
      {ficha && (
        <>
          <h2>
            Lote {corto(loteId)} · {ficha.lote.variedad}
          </h2>
          <p>
            {ficha.lote.peso.kilogramos} kg de cosecha ·{" "}
            {ficha.envio.salida ? "En tránsito" : "Por despachar"}
          </p>
          <div className="section-title">
            <h3>Historial del trayecto</h3>
            {cuenta.roles.includes("SUPERVISOR") && (
              <button
                className="secondary"
                onClick={() => setIntento((n) => n + 1)}
              >
                Actualizar seguimiento
              </button>
            )}
          </div>
          <ol className="movimientos">
            <li>Asignación · {fecha(ficha.envio.asignadoEn)}</li>
            {ficha.envio.salida && (
              <li>
                <strong>Salida registrada</strong> · {fecha(ficha.envio.salida)}{" "}
                · {ficha.envio.placa} · {ficha.envio.pesoCargaKg} kg
              </li>
            )}
            {ficha.novedades.map((n) => (
              <li
                key={n.id}
                className={n.tipo === "CONTROL" ? undefined : "incidencia-ruta"}
              >
                {n.tipo !== "CONTROL" && (
                  <span className="marca-incidencia">Incidencia</span>
                )}
                <strong>
                  {tipos[n.tipo]} · {n.lugar}
                </strong>{" "}
                · {fecha(n.registradaEn)}
                {n.descripcion && <p>{n.descripcion}</p>}
              </li>
            ))}
          </ol>
          {confirmacion && (
            <p className="success" role="status">
              {confirmacion}
            </p>
          )}
          {conductor && (
            <form
              key={ficha.envio.salida ? "ruta" : "salida"}
              onSubmit={registrar}
              className="form-panel"
            >
              <h3>
                {ficha.envio.salida
                  ? "Registrar en el trayecto"
                  : "Registrar salida"}
              </h3>
              {!ficha.envio.salida ? (
                <>
                  <label>
                    Placa del vehículo
                    <input
                      name="placa"
                      required
                      pattern="[A-Za-z0-9]{3}-?[A-Za-z0-9]{3}"
                      placeholder="ABC-123"
                      maxLength="7"
                    />
                  </label>
                  <label>
                    Peso de carga (kg)
                    <input
                      name="peso"
                      type="number"
                      required
                      min="0.001"
                      max={ficha.lote.peso.kilogramos}
                      step="0.001"
                    />
                  </label>
                  <p>Al confirmar se registrará la hora de salida.</p>
                </>
              ) : (
                <>
                  <fieldset className="accion-trayecto" disabled={ocupado}>
                    <legend>¿Qué ocurrió en el trayecto?</legend>
                    <div>
                      <label>
                        <input
                          type="radio"
                          name="accion"
                          value="control"
                          checked={!incidencia}
                          onChange={() => setIncidencia(false)}
                        />
                        <span>
                          <strong>Control de ruta</strong>
                          <small>Pasé por un punto del recorrido.</small>
                        </span>
                      </label>
                      <label>
                        <input
                          type="radio"
                          name="accion"
                          value="incidencia"
                          checked={incidencia}
                          onChange={() => setIncidencia(true)}
                        />
                        <span>
                          <strong>Incidencia</strong>
                          <small>Ocurrió un problema durante el viaje.</small>
                        </span>
                      </label>
                    </div>
                  </fieldset>
                  <label>
                    Punto de ruta
                    <input
                      name="lugar"
                      required
                      maxLength="160"
                      placeholder="Localidad o punto de control"
                    />
                  </label>
                  <fieldset
                    className="campos-trayecto"
                    hidden={incidencia}
                    disabled={incidencia || ocupado}
                  >
                    <label>
                      Observaciones (opcional)
                      <textarea name="observaciones" maxLength="1000" />
                    </label>
                  </fieldset>
                  <fieldset
                    className="campos-trayecto"
                    hidden={!incidencia}
                    disabled={!incidencia || ocupado}
                  >
                    <label>
                      Tipo de incidencia
                      <select name="tipo" required defaultValue="">
                        <option value="" disabled>
                          Selecciona el problema
                        </option>
                        {Object.entries(tipos)
                          .filter(([k]) => k !== "CONTROL")
                          .map(([k, v]) => (
                            <option key={k} value={k}>
                              {v}
                            </option>
                          ))}
                      </select>
                    </label>
                    <label>
                      Descripción de la incidencia
                      <textarea name="descripcion" maxLength="1000" required />
                    </label>
                  </fieldset>
                </>
              )}
              <button disabled={ocupado}>
                {ocupado
                  ? "Guardando…"
                  : ficha.envio.salida
                    ? incidencia
                      ? "Reportar incidencia"
                      : "Registrar paso"
                    : "Confirmar salida"}
              </button>
            </form>
          )}
        </>
      )}
    </section>
  );
}
