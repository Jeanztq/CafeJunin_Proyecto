import React, { useCallback, useEffect, useState } from "react";
import {
  Coffee,
  LayoutDashboard,
  Package,
  ClipboardCheck,
  BrainCircuit,
  Bell,
  LogOut,
  ArrowRight,
} from "lucide-react";
import { api } from "./api";
import Alertas, { porcentaje } from "./Alertas";
import Calidad, { ResultadoCalidad } from "./Calidad";
const hitos = {
  CosechaRegistrada: "Cosecha registrada",
  EvaluacionCalidadRegistrada: "Evaluación de calidad registrada",
  RiesgoEvaluado: "Riesgo evaluado",
  RiesgoAltoDetectado: "Riesgo alto detectado",
  AlertaRevisada: "Alerta revisada",
};
const labels = {
  PRODUCTOR: "Productor",
  CATADOR: "Catador Q-Grader",
  SUPERVISOR: "Supervisor",
  TRANSPORTISTA: "Transportista",
};
const short = (id) => id?.slice(0, 8) || "—";
const today = () => new Date().toLocaleDateString("en-CA");
function Field({ label, children }) {
  return (
    <label>
      {label}
      {children}
    </label>
  );
}
export default function App({ config, identity }) {
  const [account, setAccount] = useState(null),
    [page, setPage] = useState("Inicio"),
    [lots, setLots] = useState([]),
    [parcels, setParcels] = useState([]),
    [alerts, setAlerts] = useState([]),
    [detail, setDetail] = useState(null),
    [error, setError] = useState(""),
    [notice, setNotice] = useState(""),
    [busy, setBusy] = useState(false),
    [loading, setLoading] = useState(true),
    [result, setResult] = useState(null);
  const [lotId, setLotId] = useState(() => crypto.randomUUID()),
    [selected, setSelected] = useState(null);
  const actualizarAlerta = useCallback((actualizada) => {
    setAlerts((anteriores) =>
      anteriores.map((a) => (a.id === actualizada.id ? actualizada : a)),
    );
  }, []);
  const activePage = page === "Cosecha" ? "Lotes" : page;
  const alertaGenerada =
    result && alerts.find((a) => a.evaluacionRiesgoId === result.id);
  const can = (role) => account?.roles.includes(role);
  const menus = [
    ["Inicio", LayoutDashboard],
    ["Lotes", Package],
    ...(can("CATADOR") ? [["Calidad", ClipboardCheck]] : []),
    ...(can("SUPERVISOR")
      ? [
          ["Riesgo", BrainCircuit],
          ["Alertas", Bell],
        ]
      : []),
  ];
  async function refresh(inicial = false) {
    const a = await api("/me");
    setAccount(a);
    const [l, p, al] = await Promise.all([
      api("/lotes"),
      api("/parcelas"),
      a.roles.includes("SUPERVISOR") ? api("/alertas") : [],
    ]);
    setLots(l);
    setParcels(p);
    setAlerts(al);
    if (inicial && a.roles.length === 1 && a.roles.includes("CATADOR"))
      setPage("Calidad");
  }
  useEffect(() => {
    if (config.seguridadActiva && !identity?.authenticated) {
      setLoading(false);
      return;
    }
    refresh(true)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);
  async function action(fn, escritura = true) {
    setError("");
    setNotice("");
    setBusy(true);
    try {
      await fn();
      if (escritura) {
        await refresh();
        setNotice("Operación registrada correctamente.");
      }
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  async function submit(e, type) {
    e.preventDefault();
    const f = Object.fromEntries(new FormData(e.currentTarget));
    await action(async () => {
      if (type === "cosecha") {
        await api("/lotes", {
          method: "POST",
          body: JSON.stringify({ ...f, id: lotId, pesoKg: Number(f.pesoKg) }),
        });
        setLotId(crypto.randomUUID());
        setPage("Lotes");
      }
      if (type === "riesgo") {
        setResult(null);
        setResult(
          await api(`/lotes/${f.loteId}/riesgos`, {
            method: "POST",
            body: JSON.stringify({
              humedad: Number(f.humedad),
              horasDemora: Number(f.horasDemora),
            }),
          }),
        );
      }
    });
  }
  const lotSelect = (
    <Field label="Lote">
      <select name="loteId" required defaultValue="">
        <option value="" disabled>
          Selecciona un lote
        </option>
        {lots.map((l) => (
          <option key={l.id} value={l.id}>
            {short(l.id)} · {l.variedad} · {l.peso.kilogramos} kg
          </option>
        ))}
      </select>
    </Field>
  );
  if (config.seguridadActiva && !identity?.authenticated)
    return (
      <main className="login">
        <div className="brand">
          <Coffee size={36} /> CAFÉ JUNÍN
        </div>
        <span className="eyebrow">DEL ORIGEN A LA CONFIANZA</span>
        <h1>
          Una historia detrás
          <br />
          de cada lote.
        </h1>
        <p>
          Ingresa con tu cuenta. Tus permisos y módulos se asignan según tu
          función en la cooperativa.
        </p>
        <button onClick={() => identity.login()}>
          Iniciar sesión segura <ArrowRight size={18} />
        </button>
        <small>Productor · Catador · Supervisor · Transportista</small>
      </main>
    );
  return (
    <div className={page === "Calidad" ? "shell portal-calidad" : "shell"}>
      <aside>
        <div className="brand">
          <Coffee /> CAFÉ JUNÍN
        </div>
        <p className="brand-sub">TRAZABILIDAD DEL CAFÉ</p>
        <nav aria-label="Módulos">
          {menus.map(([name, Icon]) => (
            <button
              key={name}
              className={activePage === name ? "active" : ""}
              aria-current={activePage === name ? "page" : undefined}
              onClick={() => {
                setPage(name);
                setSelected(null);
                setResult(null);
                setDetail(null);
                setError("");
                setNotice("");
              }}
            >
              <Icon size={19} />
              {name === "Lotes" && can("CATADOR") ? "Lotes y muestras" : name}
              {name === "Alertas" && (
                <b>{alerts.filter((a) => a.estado === "PENDIENTE").length}</b>
              )}
            </button>
          ))}
        </nav>
        <div className="aside-bottom">
          <span>COOPERATIVA</span>
          <p>Junín, Perú</p>
        </div>
      </aside>
      <div className="workspace">
        <header>
          <span>
            Portal web <span className="sep">/</span>{" "}
            {page === "Cosecha" ? "Lotes / Registrar cosecha" : page}
          </span>
          <div className="account">
            <span>
              {account?.nombre || "Cargando…"}
              <small>
                {account?.roles.map((r) => labels[r] || r).join(" · ")}
              </small>
            </span>
            {identity?.authenticated && (
              <button
                className="icon"
                aria-label="Cerrar sesión"
                onClick={() =>
                  identity.logout({ redirectUri: location.origin })
                }
              >
                <LogOut size={19} />
              </button>
            )}
          </div>
        </header>
        <main>
          {page !== "Calidad" && (
            <div className="page-title">
              <div>
                <span className="eyebrow">CAFÉ JUNÍN / OPERACIONES</span>
                <h1>{page === "Cosecha" ? "Registrar cosecha" : page}</h1>
                <p>
                  {page === "Inicio"
                    ? "El origen, la calidad y el riesgo de tus lotes, en un mismo lugar."
                    : "Registra y consulta la información autorizada para tu cuenta."}
                </p>
              </div>
              {page === "Riesgo" && (
                <span className="pill">
                  {config.predictor === "rules"
                    ? "Estimación por reglas"
                    : "Servicio XGBoost"}
                </span>
              )}
            </div>
          )}
          {error && (
            <div role="alert" className="error">
              {error}
            </div>
          )}
          {notice && (
            <div role="status" className="success">
              {notice}
            </div>
          )}
          {loading && <p role="status">Cargando información…</p>}
          {!loading && page === "Inicio" && (
            <>
              <div className="stats">
                <article>
                  <span>Lotes accesibles</span>
                  <strong>{lots.length}</strong>
                  <small>Según los permisos de tu cuenta</small>
                </article>
                <article>
                  <span>Parcelas disponibles</span>
                  <strong>{parcels.length}</strong>
                  <small>Origen registrado del café</small>
                </article>
                {can("SUPERVISOR") && (
                  <article>
                    <span>Alertas pendientes</span>
                    <strong>
                      {alerts.filter((a) => a.estado === "PENDIENTE").length}
                    </strong>
                    <small>Requieren revisión humana</small>
                  </article>
                )}
              </div>
              <section className="welcome">
                <div>
                  <span className="eyebrow">TRAZABILIDAD CON PROPÓSITO</span>
                  <h2>
                    Información que acompaña
                    <br />a cada cosecha.
                  </h2>
                  <p>
                    Empieza por un lote y sigue sus evaluaciones de calidad y
                    riesgo.
                  </p>
                  <button onClick={() => setPage("Lotes")}>
                    Consultar lotes <ArrowRight size={17} />
                  </button>
                </div>
                <Coffee size={110} strokeWidth={1} />
              </section>
              {can("TRANSPORTISTA") && (
                <section>
                  <h2>Transporte</h2>
                  <p>
                    La asignación de viajes, las evidencias y la sincronización
                    móvil se implementarán en la siguiente fase. Esta cuenta
                    todavía no tiene operaciones de transporte disponibles.
                  </p>
                </section>
              )}
            </>
          )}
          {page === "Lotes" && (
            <section>
              <div className="section-title">
                <h2>Lotes registrados</h2>
                {can("PRODUCTOR") && (
                  <button onClick={() => setPage("Cosecha")}>
                    Registrar cosecha
                  </button>
                )}
              </div>
              {lots.length === 0 ? (
                <p className="empty">
                  No hay lotes disponibles para tu cuenta.
                </p>
              ) : (
                <div className="table-scroll">
                  <table>
                    <thead>
                      <tr>
                        <th>Lote</th>
                        <th>Variedad</th>
                        <th>Cosecha</th>
                        <th>Peso</th>
                        <th>Acción</th>
                      </tr>
                    </thead>
                    <tbody>
                      {lots.map((l) => (
                        <tr key={l.id}>
                          <td title={l.id}>{short(l.id)}</td>
                          <td>{l.variedad}</td>
                          <td>{l.fechaCosecha}</td>
                          <td>{l.peso.kilogramos} kg</td>
                          <td>
                            <button
                              className="secondary"
                              onClick={() =>
                                action(
                                  async () =>
                                    setDetail(
                                      await api(`/lotes/${l.id}/detalle`),
                                    ),
                                  false,
                                )
                              }
                            >
                              Ver detalle
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
              {detail && (
                <article className="detail">
                  <h3>Historial del lote {short(detail.lote.id)}</h3>
                  <p>
                    Parcela:{" "}
                    {parcels.find((p) => p.id === detail.lote.parcelaId)
                      ?.nombre || short(detail.lote.parcelaId)}
                  </p>
                  <h4>Movimientos del lote</h4>
                  <ol className="movimientos">
                    {detail.historial.map((h) => (
                      <li key={h.id}>
                        <strong>{hitos[h.tipo] || h.tipo}</strong>
                        {" · "}
                        <time dateTime={h.ocurridoEn}>
                          {new Date(h.ocurridoEn).toLocaleString("es-PE", {
                            timeZone: "America/Lima",
                          })}
                        </time>
                      </li>
                    ))}
                  </ol>
                  <h4>Calidad</h4>
                  {detail.calidad.length ? (
                    detail.calidad.map((q) => (
                      <article key={q.id}>
                        {q.registradaEn && (
                          <small>
                            {new Date(q.registradaEn).toLocaleString("es-PE", {
                              timeZone: "America/Lima",
                            })}
                          </small>
                        )}
                        <ResultadoCalidad ficha={q} />
                      </article>
                    ))
                  ) : (
                    <p>Sin evaluaciones.</p>
                  )}
                  {can("SUPERVISOR") && (
                    <>
                      <h4>Riesgo</h4>
                      {detail.riesgos.map((q) => (
                        <p key={q.id}>
                          {(q.probabilidad.valor * 100).toFixed(1)}% ·{" "}
                          {q.modelo}{" "}
                          {q.simulada ? "· estimación sin validar" : ""}
                        </p>
                      ))}
                    </>
                  )}
                </article>
              )}
            </section>
          )}
          {page === "Cosecha" && can("PRODUCTOR") && (
            <section className="form-panel">
              <button className="secondary" onClick={() => setPage("Lotes")}>
                Volver a lotes
              </button>
              <p className="muted">
                El lote conserva su identificador si reintentas el envío.
              </p>
              <form onSubmit={(e) => submit(e, "cosecha")}>
                <Field label="Parcela">
                  <select name="parcelaId" required defaultValue="">
                    <option value="" disabled>
                      Selecciona tu parcela
                    </option>
                    {parcels
                      .filter((p) => p.propietarioSubject === account?.subject)
                      .map((p) => (
                        <option value={p.id} key={p.id}>
                          {p.nombre} · {p.ubicacion}
                        </option>
                      ))}
                  </select>
                </Field>
                <div className="form-grid">
                  <Field label="Fecha de cosecha">
                    <input
                      type="date"
                      name="fechaCosecha"
                      max={today()}
                      required
                    />
                  </Field>
                  <Field label="Peso neto (kg)">
                    <input
                      type="number"
                      name="pesoKg"
                      min="0.001"
                      max="99999999.999"
                      step="0.001"
                      required
                    />
                  </Field>
                  <Field label="Variedad">
                    <select name="variedad">
                      {["TYPICA", "GEISHA", "BOURBON", "CATIMOR"].map((v) => (
                        <option key={v}>{v}</option>
                      ))}
                    </select>
                  </Field>
                  <Field label="Recolección">
                    <select name="tipoRecoleccion">
                      <option>SELECTIVA</option>
                      <option>GENERAL</option>
                    </select>
                  </Field>
                </div>
                <button disabled={busy || !parcels.length}>
                  {busy ? "Guardando…" : "Guardar cosecha"}
                </button>
              </form>
            </section>
          )}
          {!loading && page === "Calidad" && can("CATADOR") && (
            <Calidad
              lotes={lots}
              parcelas={parcels}
              registrarCosecha={
                can("PRODUCTOR") ? () => setPage("Cosecha") : undefined
              }
              cancelar={() => setPage("Lotes")}
            />
          )}
          {page === "Riesgo" && can("SUPERVISOR") && (
            <section className="form-panel">
              <h2>Evaluar riesgo del lote</h2>
              <div className="banner">
                Registra la humedad relativa medida en el ambiente y la demora
                logística observada para este lote. El sistema usará esos
                valores para estimar el riesgo.
              </div>
              <form
                onChange={() => setResult(null)}
                onSubmit={(e) => submit(e, "riesgo")}
              >
                <fieldset disabled={busy} className="mediciones-riesgo">
                  {lotSelect}
                  <div className="form-grid">
                    <Field label="Humedad relativa ambiental (%)">
                      <input
                        type="number"
                        name="humedad"
                        min="0"
                        max="100"
                        step="0.1"
                        required
                      />
                    </Field>
                    <Field label="Demora (horas)">
                      <input
                        type="number"
                        name="horasDemora"
                        min="0"
                        step="0.1"
                        required
                      />
                    </Field>
                  </div>
                </fieldset>
                <button disabled={busy || !lots.length}>
                  {busy ? "Evaluando…" : "Evaluar riesgo"}
                </button>
              </form>
              {result && (
                <div className="result">
                  <strong>{porcentaje(result.probabilidad.valor)}</strong>
                  {alertaGenerada && (
                    <p>
                      <button
                        className="secondary"
                        onClick={() => {
                          setSelected(alertaGenerada.id);
                          setPage("Alertas");
                          setNotice("");
                        }}
                      >
                        {alertaGenerada.estado === "PENDIENTE"
                          ? "Revisar alerta generada"
                          : "Consultar alerta generada"}
                      </button>
                    </p>
                  )}
                  {!alertaGenerada && !busy && !error && (
                    <p>Evaluación registrada sin alerta.</p>
                  )}
                  <small>
                    {result.modelo} ·{" "}
                    {result.simulada
                      ? "Estimación sin validación de campo"
                      : "Verificar validación del modelo antes de uso operativo"}
                  </small>
                </div>
              )}
            </section>
          )}
          {page === "Alertas" && can("SUPERVISOR") && (
            <Alertas
              alertas={alerts}
              seleccion={selected}
              seleccionar={setSelected}
              alRevisar={actualizarAlerta}
            />
          )}
        </main>
        <footer>
          Café Junín · Origen verificable, decisiones responsables.
        </footer>
      </div>
    </div>
  );
}
