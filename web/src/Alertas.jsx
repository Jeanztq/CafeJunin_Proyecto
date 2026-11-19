import React, { useEffect, useRef, useState } from "react";
import { api } from "./api";

const formatoPorcentaje = new Intl.NumberFormat("es-PE", {
  style: "percent",
  maximumFractionDigits: 6,
});
export const porcentaje = (valor) => formatoPorcentaje.format(valor);
const formatoFecha = new Intl.DateTimeFormat("es-PE", {
  dateStyle: "medium",
  timeStyle: "short",
  timeZone: "America/Lima",
});
const fecha = (instante) => (
  <time dateTime={instante}>{formatoFecha.format(new Date(instante))}</time>
);
const corto = (id) => id.slice(0, 8);
const estados = {
  PENDIENTE: "Pendiente",
  CONFIRMADA: "Confirmada",
  DESCARTADA: "Descartada",
};

export default function Alertas({
  alertas,
  seleccion,
  seleccionar,
  alRevisar,
}) {
  if (seleccion)
    return (
      <DetalleAlerta
        key={seleccion}
        id={seleccion}
        volver={() => seleccionar(null)}
        alRevisar={alRevisar}
      />
    );
  const ordenadas = [...alertas].sort(
    (a, b) =>
      Number(b.estado === "PENDIENTE") - Number(a.estado === "PENDIENTE") ||
      Date.parse(b.registradaEn) - Date.parse(a.registradaEn),
  );
  return (
    <section>
      <h2>Revisión del supervisor</h2>
      {!ordenadas.length && (
        <p className="empty">No hay alertas registradas.</p>
      )}
      {ordenadas.map((a) => (
        <article className="alert-row" key={a.id}>
          <div>
            <span className={a.estado === "PENDIENTE" ? "pill amber" : "pill"}>
              {estados[a.estado]}
            </span>
            <h3>Lote {corto(a.loteId)}</h3>
            <small>
              Alerta {corto(a.id)} · {fecha(a.registradaEn)}
            </small>
          </div>
          <button className="secondary" onClick={() => seleccionar(a.id)}>
            {a.estado === "PENDIENTE" ? "Revisar alerta" : "Consultar decisión"}
          </button>
        </article>
      ))}
    </section>
  );
}

function DetalleAlerta({ id, volver, alRevisar }) {
  const [detalle, setDetalle] = useState(null);
  const [intento, setIntento] = useState(0);
  const [error, setError] = useState("");
  const [conflicto, setConflicto] = useState(false);
  const [decision, setDecision] = useState("");
  const [guardando, setGuardando] = useState(false);
  const [operacionId] = useState(() => crypto.randomUUID());
  const enviando = useRef(false);
  const ruta = "/alertas/" + id;
  useEffect(() => {
    let vigente = true;
    const control = new AbortController();
    api(ruta, { signal: control.signal })
      .then((datos) => {
        if (vigente) {
          setDetalle(datos);
          if (datos.alerta.estado !== "PENDIENTE") alRevisar(datos.alerta);
        }
      })
      .catch((e) => {
        if (vigente) setError(e.message);
      });
    return () => {
      vigente = false;
      control.abort();
    };
  }, [ruta, intento, alRevisar]);

  function recargar() {
    setError("");
    setConflicto(false);
    setDetalle(null);
    setDecision("");
    setIntento((n) => n + 1);
  }
  async function registrar(e) {
    e.preventDefault();
    if (enviando.current) return;
    const campos = new FormData(e.currentTarget);
    const motivo = campos.get("motivo").trim();
    const instruccion =
      decision === "CONFIRMADA" ? campos.get("instruccion").trim() : null;
    if (motivo.length < 5 || (instruccion !== null && instruccion.length < 5)) {
      setError(
        "Escribe al menos cinco caracteres de contenido en cada campo requerido.",
      );
      return;
    }
    enviando.current = true;
    setGuardando(true);
    setError("");
    try {
      const alerta = await api(ruta + "/decision", {
        method: "PUT",
        body: JSON.stringify({
          operacionId,
          estado: decision,
          motivo,
          instruccion,
        }),
      });
      setDetalle((actual) => ({ ...actual, alerta }));
      alRevisar(alerta);
    } catch (e) {
      setError(e.message);
      setConflicto(e.status === 409);
    } finally {
      enviando.current = false;
      setGuardando(false);
    }
  }
  return (
    <section className="revision-alerta">
      <button className="secondary" onClick={volver}>
        Volver a alertas
      </button>
      <h2>Alerta {corto(id)}</h2>
      {error && (
        <div role="alert" className="error">
          {error}{" "}
          {(!detalle || conflicto) && (
            <button className="secondary" onClick={recargar}>
              Actualizar alerta
            </button>
          )}
        </div>
      )}
      {!detalle && !error && (
        <p role="status">Cargando la evaluación del lote…</p>
      )}
      {detalle && (
        <>
          <span
            className={
              detalle.alerta.estado === "PENDIENTE" ? "pill amber" : "pill"
            }
          >
            {estados[detalle.alerta.estado]}
          </span>
          <h3>Evaluación que originó la alerta</h3>
          <dl className="datos-alerta">
            <div>
              <dt>Riesgo estimado</dt>
              <dd>{porcentaje(detalle.evaluacion.probabilidad.valor)}</dd>
            </div>
            <div>
              <dt>Umbral de alerta</dt>
              <dd>Mayor que {porcentaje(detalle.umbralAlerta)}</dd>
            </div>
            <div>
              <dt>Humedad relativa ambiental</dt>
              <dd>{detalle.evaluacion.humedad} %</dd>
            </div>
            <div>
              <dt>Demora logística</dt>
              <dd>{detalle.evaluacion.horasDemora} h</dd>
            </div>
            <div>
              <dt>Alerta generada (hora de Perú)</dt>
              <dd>{fecha(detalle.alerta.registradaEn)}</dd>
            </div>
            <div>
              <dt>Modelo utilizado</dt>
              <dd>{detalle.evaluacion.modelo}</dd>
            </div>
          </dl>
          {detalle.evaluacion.simulada && (
            <p className="banner">
              Esta estimación carece de validación en campo. Contrasta los datos
              con las condiciones del lote antes de decidir.
            </p>
          )}
          <h3>Lote {corto(detalle.lote.id)}</h3>
          <dl className="datos-alerta">
            <div>
              <dt>Parcela</dt>
              <dd>
                {detalle.parcela.nombre} · {detalle.parcela.ubicacion}
              </dd>
            </div>
            <div>
              <dt>Fecha de cosecha</dt>
              <dd>{detalle.lote.fechaCosecha}</dd>
            </div>
            <div>
              <dt>Variedad</dt>
              <dd>{detalle.lote.variedad}</dd>
            </div>
            <div>
              <dt>Peso neto</dt>
              <dd>{detalle.lote.peso.kilogramos} kg</dd>
            </div>
          </dl>
          {detalle.alerta.estado === "PENDIENTE" ? (
            <form onSubmit={registrar} className="form-panel">
              <h3>Decisión del supervisor</h3>
              <label>
                Decisión
                <select
                  required
                  value={decision}
                  onChange={(e) => setDecision(e.target.value)}
                >
                  <option value="" disabled>
                    Selecciona una decisión
                  </option>
                  <option value="CONFIRMADA">Confirmar riesgo</option>
                  <option value="DESCARTADA">Descartar alerta</option>
                </select>
              </label>
              <label>
                Motivo de la decisión
                <textarea
                  required
                  name="motivo"
                  minLength="5"
                  maxLength="1000"
                />
              </label>
              {decision === "CONFIRMADA" && (
                <label>
                  Instrucción para atender el riesgo
                  <textarea
                    required
                    name="instruccion"
                    minLength="5"
                    maxLength="1000"
                  />
                </label>
              )}
              <p className="muted">
                La decisión queda registrada con tu cuenta y cierra la revisión.
              </p>
              <button disabled={guardando || conflicto}>
                {guardando ? "Guardando…" : "Registrar decisión"}
              </button>
            </form>
          ) : (
            <div className="detail">
              <h3>Decisión registrada</h3>
              <dl className="datos-alerta">
                <div>
                  <dt>Responsable</dt>
                  <dd>{detalle.alerta.responsable}</dd>
                </div>
                <div>
                  <dt>Fecha de revisión (hora de Perú)</dt>
                  <dd>{fecha(detalle.alerta.revisadaEn)}</dd>
                </div>
                <div>
                  <dt>Motivo de la decisión</dt>
                  <dd>{detalle.alerta.motivo}</dd>
                </div>
                {detalle.alerta.instruccion && (
                  <div>
                    <dt>Instrucción para atender el riesgo</dt>
                    <dd>{detalle.alerta.instruccion}</dd>
                  </div>
                )}
              </dl>
            </div>
          )}
        </>
      )}
    </section>
  );
}
