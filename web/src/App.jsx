import React, { useEffect, useState } from "react";
import {
  Coffee,
  LayoutDashboard,
  Package,
  Leaf,
  ClipboardCheck,
  BrainCircuit,
  Bell,
  LogOut,
  ArrowRight,
} from "lucide-react";
import { api } from "./api";
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
    [reviewId, setReviewId] = useState(() => crypto.randomUUID()),
    [selected, setSelected] = useState(""),
    [decision, setDecision] = useState("CONFIRMADA");
  const can = (role) => account?.roles.includes(role);
  const menus = [
    ["Inicio", LayoutDashboard],
    ["Lotes", Package],
    ...(can("PRODUCTOR") ? [["Cosecha", Leaf]] : []),
    ...(can("CATADOR") ? [["Calidad", ClipboardCheck]] : []),
    ...(can("SUPERVISOR")
      ? [
          ["Riesgo IA", BrainCircuit],
          ["Alertas", Bell],
        ]
      : []),
  ];
  async function refresh() {
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
  }
  useEffect(() => {
    if (config.seguridadActiva && !identity?.authenticated) {
      setLoading(false);
      return;
    }
    refresh()
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);
  async function action(fn) {
    setError("");
    setNotice("");
    setBusy(true);
    try {
      await fn();
      await refresh();
      setNotice("Operación registrada correctamente.");
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
      if (type === "calidad") {
        await api(`/lotes/${f.loteId}/evaluaciones`, {
          method: "POST",
          body: JSON.stringify({
            defectosPrimarios: Number(f.defectosPrimarios),
            defectosSecundarios: Number(f.defectosSecundarios),
            puntajeSca: Number(f.puntajeSca),
          }),
        });
      }
      if (type === "riesgo") {
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
      if (type === "revision") {
        await api(`/alertas/${selected}/decision`, {
          method: "PUT",
          body: JSON.stringify({
            operacionId: reviewId,
            estado: decision,
            motivo: f.motivo,
            instruccion: decision === "CONFIRMADA" ? f.instruccion : null,
          }),
        });
        setSelected("");
        setReviewId(crypto.randomUUID());
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
    <div className="shell">
      <aside>
        <div className="brand">
          <Coffee /> CAFÉ JUNÍN
        </div>
        <p className="brand-sub">TRAZABILIDAD DEL CAFÉ</p>
        <nav aria-label="Módulos">
          {menus.map(([name, Icon]) => (
            <button
              key={name}
              className={page === name ? "active" : ""}
              onClick={() => {
                setPage(name);
                setDetail(null);
                setError("");
                setNotice("");
              }}
            >
              <Icon size={19} />
              {name}
              {name === "Alertas" && (
                <b>{alerts.filter((a) => a.estado === "PENDIENTE").length}</b>
              )}
            </button>
          ))}
        </nav>
        <div className="aside-bottom">
          <span>COOPERATIVA · PILOTO</span>
          <p>Junín, Perú</p>
          <small>Entrega 0.2 · Flujo inicial</small>
        </div>
      </aside>
      <div className="workspace">
        <header>
          <span>
            Portal de trazabilidad <span className="sep">/</span> {page}
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
          {!config.seguridadActiva && (
            <div className="banner">
              DEMOSTRACIÓN LOCAL · Sin inicio de sesión. Reúne permisos de
              prueba; no usar expuesto a Internet.
            </div>
          )}
          <div className="page-title">
            <div>
              <span className="eyebrow">CAFÉ JUNÍN / OPERACIONES</span>
              <h1>{page}</h1>
              <p>
                {page === "Inicio"
                  ? "El origen, la calidad y el riesgo de tus lotes, en un mismo lugar."
                  : "Registra y consulta la información autorizada para tu cuenta."}
              </p>
            </div>
            <span className="pill">
              {config.predictor === "rules"
                ? "Reglas de demostración"
                : "Servicio XGBoost"}
            </span>
          </div>
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
              <section>
                <h2>Recorrido disponible</h2>
                <p>
                  Productor: cosecha → Catador: calidad → Supervisor: riesgo y
                  revisión de alertas.
                </p>
                <p className="muted">
                  El puntaje de calidad se registra manualmente. Las
                  recomendaciones del modelo requieren validación humana.
                </p>
              </section>
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
                                action(async () =>
                                  setDetail(
                                    await api(`/lotes/${l.id}/detalle`),
                                  ),
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
                  <h4>Calidad</h4>
                  {detail.calidad.length ? (
                    detail.calidad.map((q) => (
                      <p key={q.id}>
                        {q.puntajeSca} puntos SCA · Defectos:{" "}
                        {q.defectosPrimarios} primarios, {q.defectosSecundarios}{" "}
                        secundarios
                      </p>
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
                          {q.modelo} {q.simulada ? "· demostración" : ""}
                        </p>
                      ))}
                    </>
                  )}
                </article>
              )}
            </section>
          )}
          {page === "Cosecha" && (
            <section className="form-panel">
              <h2>Registrar cosecha</h2>
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
          {page === "Calidad" && (
            <section className="form-panel">
              <h2>Evaluación de calidad</h2>
              <p className="muted">
                Registro básico de defectos y puntaje SCA. El formulario
                sensorial completo queda para la siguiente fase.
              </p>
              <form onSubmit={(e) => submit(e, "calidad")}>
                {lotSelect}
                <div className="form-grid">
                  <Field label="Defectos primarios">
                    <input
                      type="number"
                      name="defectosPrimarios"
                      min="0"
                      step="1"
                      required
                    />
                  </Field>
                  <Field label="Defectos secundarios">
                    <input
                      type="number"
                      name="defectosSecundarios"
                      min="0"
                      step="1"
                      required
                    />
                  </Field>
                  <Field label="Puntaje SCA">
                    <input
                      type="number"
                      name="puntajeSca"
                      min="0"
                      max="100"
                      step="0.01"
                      required
                    />
                  </Field>
                </div>
                <button disabled={busy || !lots.length}>
                  Guardar evaluación
                </button>
              </form>
            </section>
          )}
          {page === "Riesgo IA" && (
            <section className="form-panel">
              <h2>Evaluar riesgo del lote</h2>
              <div className="banner">
                La humedad corresponde al ambiente, no a la humedad del grano.
                El modelo incluido usa datos sintéticos y requiere validación
                con datos reales.
              </div>
              <form onSubmit={(e) => submit(e, "riesgo")}>
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
                <button disabled={busy || !lots.length}>Evaluar riesgo</button>
              </form>
              {result && (
                <div className="result">
                  <strong>
                    {(result.probabilidad.valor * 100).toFixed(1)}%
                  </strong>
                  <p>
                    {result.probabilidad.valor > 0.7
                      ? "Riesgo mayor al 70%: consulta Alertas."
                      : "Resultado registrado. Solo un valor mayor al 70% genera alerta."}
                  </p>
                  <small>
                    {result.modelo} ·{" "}
                    {result.simulada
                      ? "DEMOSTRACIÓN, NO VALIDADO PARA DECISIONES REALES"
                      : "Verificar validación del modelo antes de uso operativo"}
                  </small>
                </div>
              )}
            </section>
          )}
          {page === "Alertas" && (
            <section>
              <h2>Revisión del supervisor</h2>
              {!alerts.length && (
                <p className="empty">No hay alertas registradas.</p>
              )}
              {alerts.map((a) => (
                <article className="alert-row" key={a.id}>
                  <div>
                    <span
                      className={`pill ${a.estado === "PENDIENTE" ? "amber" : ""}`}
                    >
                      {a.estado}
                    </span>
                    <h3>Lote {short(a.loteId)}</h3>
                    <small>Alerta {short(a.id)}</small>
                    {a.motivo && (
                      <p>
                        {a.motivo}
                        {a.instruccion && ` · Instrucción: ${a.instruccion}`}
                      </p>
                    )}
                  </div>
                  {a.estado === "PENDIENTE" && (
                    <button
                      className="secondary"
                      onClick={() => {
                        setSelected(a.id);
                        setDecision("CONFIRMADA");
                        setReviewId(crypto.randomUUID());
                      }}
                    >
                      Revisar alerta
                    </button>
                  )}
                </article>
              ))}
              {selected && (
                <form
                  key={selected}
                  className="detail"
                  onSubmit={(e) => submit(e, "revision")}
                >
                  <h3>Revisar alerta {short(selected)}</h3>
                  <Field label="Decisión">
                    <select
                      value={decision}
                      onChange={(e) => setDecision(e.target.value)}
                    >
                      <option>CONFIRMADA</option>
                      <option>DESCARTADA</option>
                    </select>
                  </Field>
                  <Field label="Motivo de la decisión">
                    <textarea
                      name="motivo"
                      minLength="5"
                      maxLength="1000"
                      required
                    />
                  </Field>
                  {decision === "CONFIRMADA" && (
                    <Field label="Instrucción para atender el riesgo">
                      <textarea
                        name="instruccion"
                        minLength="5"
                        maxLength="1000"
                        required
                      />
                    </Field>
                  )}
                  <p className="muted">
                    La decisión queda registrada con tu cuenta y no se puede
                    reemplazar desde esta pantalla.
                  </p>
                  <button disabled={busy}>Registrar decisión</button>
                </form>
              )}
            </section>
          )}
        </main>
        <footer>
          Café Junín · Origen verificable, decisiones responsables.
        </footer>
      </div>
    </div>
  );
}
