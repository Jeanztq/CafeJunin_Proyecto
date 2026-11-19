import React, { useRef, useState } from "react";
import { api } from "./api";

const atributos = [
  ["fragancia", "Fragancia / aroma"],
  ["sabor", "Sabor"],
  ["acidez", "Acidez"],
  ["cuerpo", "Cuerpo"],
  ["posgusto", "Posgusto"],
  ["balance", "Balance"],
  ["uniformidad", "Uniformidad"],
  ["dulzor", "Dulzor"],
];
const puntos = (valor) =>
  Number(valor).toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 15,
    useGrouping: false,
  });
const fecha = (valor) =>
  new Date(valor).toLocaleDateString("es-PE", { timeZone: "America/Lima" });
const codigo = (id) => id.slice(0, 8).toUpperCase();

export function ResultadoCalidad({ ficha }) {
  return (
    <div className="ficha-resultado">
      <h2>{puntos(ficha.puntajeSca)} puntos SCA</h2>
      <dl>
        <dt>Defectos primarios</dt>
        <dd>{ficha.defectosPrimarios}</dd>
        <dt>Defectos secundarios</dt>
        <dd>{ficha.defectosSecundarios}</dd>
        <dt>Responsable</dt>
        <dd>
          {ficha.catadorNombre
            ? `${ficha.catadorNombre} · Catador Q Grader`
            : "Sin registrar"}
        </dd>
        <dt>Clasificación por puntaje</dt>
        <dd>{ficha.clasificacion}</dd>
        <dt>Estado</dt>
        <dd>{ficha.atributos ? "Confirmada" : "Registro básico anterior"}</dd>
      </dl>
      {ficha.atributos && (
        <details>
          <summary>Ver atributos sensoriales</summary>
          <dl>
            {atributos.map(([key, label]) => (
              <React.Fragment key={key}>
                <dt>{label}</dt>
                <dd>{puntos(ficha.atributos[key])} / 10</dd>
              </React.Fragment>
            ))}
          </dl>
        </details>
      )}
    </div>
  );
}

function Campo({ label, children }) {
  return (
    <label className="campo-cata">
      <span>{label}</span>
      {children}
    </label>
  );
}

function Numero({ name, label, max, entero = false, invalido }) {
  return (
    <Campo label={label}>
      <input
        name={name}
        aria-invalid={invalido || undefined}
        aria-describedby={invalido ? "aviso-cata" : undefined}
        type="number"
        min="0"
        max={max}
        step={entero ? "1" : "0.01"}
        required
      />
    </Campo>
  );
}

export default function Calidad({ lotes, parcelas, cancelar }) {
  const [loteId, setLoteId] = useState(lotes[0]?.id || "");
  const [ficha, setFicha] = useState(null);
  const [error, setError] = useState("");
  const [campoInvalido, setCampoInvalido] = useState("");
  const [guardando, setGuardando] = useState(false);
  const [operacionId, setOperacionId] = useState(() => crypto.randomUUID());
  const enviando = useRef(false);
  const lote = lotes.find((item) => item.id === loteId);
  const parcela = parcelas.find((item) => item.id === lote?.parcelaId);

  async function guardar(event) {
    event.preventDefault();
    if (enviando.current) return;
    const form = event.currentTarget;
    const invalido = Array.from(form.elements).find(
      (campo) => campo.validity && !campo.validity.valid,
    );
    if (invalido) {
      const nombre = invalido.labels?.[0]?.textContent || "Campo";
      const mensaje = invalido.validity.valueMissing
        ? "Completa este campo."
        : invalido.name.startsWith("defectos")
          ? "Usa un entero no negativo."
          : `Usa un valor entre 0 y ${invalido.max} con hasta dos decimales.`;
      setError(`${nombre.trim()}. ${mensaje}`);
      setCampoInvalido(invalido.name);
      invalido.focus();
      return;
    }
    const campos = new FormData(form);
    enviando.current = true;
    setGuardando(true);
    setCampoInvalido("");
    setError("");
    try {
      setFicha(
        await api(`/lotes/${loteId}/evaluaciones`, {
          method: "POST",
          body: JSON.stringify({
            id: operacionId,
            defectosPrimarios: Number(campos.get("defectosPrimarios")),
            defectosSecundarios: Number(campos.get("defectosSecundarios")),
            atributos: Object.fromEntries(
              atributos.map(([key]) => [key, Number(campos.get(key))]),
            ),
            puntajeSca: Number(campos.get("puntajeSca")),
          }),
        }),
      );
    } catch (err) {
      setError(err.message);
    } finally {
      enviando.current = false;
      setGuardando(false);
    }
  }

  return (
    <div className="calidad">
      <p className="modulo">Calidad · HU08</p>
      <h1>Evaluar la calidad del lote</h1>
      {lote && (
        <p className="contexto-lote">
          Lote {codigo(lote.id)} · {lote.variedad}
          {parcela && ` · ${parcela.nombre}, ${parcela.ubicacion}`}
        </p>
      )}
      {!lotes.length ? (
        <section>
          <h2>Sin lotes para evaluar</h2>
          <p>La ficha se habilita cuando se registra una cosecha.</p>
          <button className="secondary" onClick={cancelar}>
            Volver a lotes
          </button>
        </section>
      ) : ficha ? (
        <>
          <div className="success" role="status">
            Evaluación registrada · {codigo(ficha.loteId)} ·{" "}
            {fecha(ficha.registradaEn)}
          </div>
          <section>
            <ResultadoCalidad ficha={ficha} />
            <button
              onClick={() => {
                setFicha(null);
                setOperacionId(crypto.randomUUID());
              }}
            >
              Volver a la evaluación
            </button>
          </section>
        </>
      ) : (
        <>
          {error && (
            <div role="alert" id="aviso-cata" className="cata-aviso">
              {error}
            </div>
          )}
          <div className="cata-columnas">
            <section>
              <form
                onSubmit={guardar}
                noValidate
                onChange={(event) => {
                  if (
                    campoInvalido === event.target.name &&
                    event.target.validity.valid
                  ) {
                    setCampoInvalido("");
                    setError("");
                  }
                }}
              >
                <fieldset disabled={guardando}>
                  <Campo label="Lote">
                    <select
                      value={loteId}
                      name="lote"
                      required
                      onChange={(e) => setLoteId(e.target.value)}
                    >
                      {lotes.map((l) => (
                        <option key={l.id} value={l.id}>
                          {codigo(l.id)} · {l.variedad} ·{" "}
                          {Number(l.peso.kilogramos).toFixed(3)} kg
                        </option>
                      ))}
                    </select>
                  </Campo>
                  <div className="cata-pares">
                    <Numero
                      label="Defectos primarios"
                      name="defectosPrimarios"
                      invalido={campoInvalido === "defectosPrimarios"}
                      entero
                      max={2147483647}
                    />
                    <Numero
                      label="Defectos secundarios"
                      name="defectosSecundarios"
                      invalido={campoInvalido === "defectosSecundarios"}
                      entero
                      max={2147483647}
                    />
                  </div>
                  <h3>Atributos sensoriales · escala de 0 a 10</h3>
                  <div className="cata-pares">
                    {atributos.map(([key, label]) => (
                      <Numero
                        key={key}
                        name={key}
                        invalido={campoInvalido === key}
                        label={label}
                        max={10}
                      />
                    ))}
                  </div>
                  <Numero
                    label="Puntaje SCA final"
                    name="puntajeSca"
                    invalido={campoInvalido === "puntajeSca"}
                    max={100}
                  />
                  <div className="cata-pares acciones">
                    <button type="submit">
                      {guardando ? "Registrando…" : "Registrar evaluación"}
                    </button>
                    <button
                      className="secondary"
                      type="button"
                      onClick={cancelar}
                    >
                      Cancelar
                    </button>
                  </div>
                </fieldset>
              </form>
            </section>
            <section className="cata-ayuda" aria-label="Ayuda de la ficha">
              <h2>Antes de confirmar</h2>
              <ol>
                <li>Verifica el lote.</li>
                <li>Revisa defectos y atributos.</li>
                <li>Confirma el puntaje final.</li>
              </ol>
              <p>
                Registra el puntaje de la muestra catada. La evaluación se
                guarda al confirmar.
              </p>
            </section>
          </div>
        </>
      )}
    </div>
  );
}
