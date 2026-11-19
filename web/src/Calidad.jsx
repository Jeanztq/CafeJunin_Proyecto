import React, { useRef, useState } from "react";
import { api } from "./api";

const atributos = [
  ["fragancia", "Fragancia / aroma"],
  ["sabor", "Sabor"],
  ["acidez", "Acidez"],
  ["cuerpo", "Cuerpo"],
  ["posgusto", "Posgusto"],
  ["balance", "Balance"],
  ["apreciacionGlobal", "Apreciación global"],
  ["uniformidad", "Uniformidad"],
  ["tazaLimpia", "Taza limpia"],
  ["dulzor", "Dulzor"],
];
const porTaza = new Set(["uniformidad", "dulzor", "tazaLimpia"]);

function puntajePrevio(form) {
  let subtotal = 0;
  for (const [key] of atributos) {
    const campo = form.elements.namedItem(key);
    if (!campo.validity.valid) return null;
    subtotal += Number(campo.value);
  }
  const leves = form.elements.leves,
    graves = form.elements.graves;
  if (
    !leves.validity.valid ||
    !graves.validity.valid ||
    Number(leves.value) + Number(graves.value) > 5
  )
    return null;
  return subtotal - 2 * Number(leves.value) - 4 * Number(graves.value);
}

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
        <dt>Método</dt>
        <dd>
          {ficha.protocolo === "SCA-2004"
            ? "SCA clásica (2004)"
            : "Registro histórico · puntaje manual"}
        </dd>
        {ficha.defectosTaza && (
          <>
            <dt>Tazas con defecto leve / grave</dt>
            <dd>
              {ficha.defectosTaza.leves} / {ficha.defectosTaza.graves}
            </dd>
            <dt>Umbral comercial aplicado</dt>
            <dd>{puntos(ficha.umbralEspecialidad)} puntos</dd>
          </>
        )}
        <dt>Estado</dt>
        <dd>{ficha.atributos ? "Confirmada" : "Registro básico anterior"}</dd>
      </dl>
      {ficha.atributos && (
        <details>
          <summary>Ver atributos sensoriales</summary>
          <dl>
            {atributos
              .filter(([key]) => ficha.atributos[key] != null)
              .map(([key, label]) => (
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

function Numero({ name, label, max, min = 0, step = 1, initial, invalido }) {
  return (
    <Campo label={label}>
      <input
        name={name}
        aria-invalid={invalido || undefined}
        aria-describedby={invalido ? "aviso-cata" : undefined}
        type="number"
        min={min}
        max={max}
        step={step}
        defaultValue={initial}
        required
      />
    </Campo>
  );
}

export default function Calidad({
  lotes,
  parcelas,
  cancelar,
  registrarCosecha,
}) {
  const [loteId, setLoteId] = useState(lotes[0]?.id || "");
  const [ficha, setFicha] = useState(null);
  const [error, setError] = useState("");
  const [campoInvalido, setCampoInvalido] = useState("");
  const [guardando, setGuardando] = useState(false);
  const [operacionId, setOperacionId] = useState(() => crypto.randomUUID());
  const [previo, setPrevio] = useState(null);
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
        : ["leves", "graves"].includes(invalido.name)
          ? "Ingresa un número entero de 0 a 5."
          : invalido.name.startsWith("defectos")
            ? "Usa un entero no negativo."
            : `Usa un valor de ${invalido.min} a ${invalido.max}, en pasos de ${invalido.step}.`;
      setError(`${nombre.trim()}. ${mensaje}`);
      setCampoInvalido(invalido.name);
      invalido.focus();
      return;
    }
    const campos = new FormData(form);
    const defectosTaza = {
      leves: Number(campos.get("leves")),
      graves: Number(campos.get("graves")),
    };
    if (defectosTaza.leves + defectosTaza.graves > 5) {
      setError(
        "El total de tazas afectadas supera las cinco tazas de la muestra.",
      );
      setCampoInvalido("graves");
      form.elements.graves.focus();
      return;
    }
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
            defectosTaza,
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
      <p className="modulo">Ficha de cata · SCA clásica (2004)</p>
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
          {registrarCosecha ? (
            <button onClick={registrarCosecha}>Registrar cosecha</button>
          ) : (
            <button className="secondary" onClick={cancelar}>
              Volver a lotes
            </button>
          )}
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
                setPrevio(null);
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
                  setPrevio(puntajePrevio(event.currentTarget));
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
                      max={2147483647}
                    />
                    <Numero
                      label="Defectos secundarios"
                      name="defectosSecundarios"
                      invalido={campoInvalido === "defectosSecundarios"}
                      max={2147483647}
                    />
                  </div>
                  <h3>Perfil sensorial</h3>
                  <p className="cata-nota">
                    Califica siete atributos de 6 a 10 en pasos de 0.25. Para
                    uniformidad, dulzor y taza limpia asigna 2 puntos por cada
                    taza que cumple, hasta 10.
                  </p>
                  <div className="cata-pares">
                    {atributos.map(([key, label]) => (
                      <Numero
                        key={key}
                        name={key}
                        invalido={campoInvalido === key}
                        label={label}
                        min={porTaza.has(key) ? 0 : 6}
                        step={porTaza.has(key) ? 2 : 0.25}
                        max={10}
                      />
                    ))}
                  </div>
                  <h3>Defectos en taza</h3>
                  <p className="cata-nota">
                    Cuenta cada taza una sola vez según el defecto de mayor
                    intensidad.
                  </p>
                  <div className="cata-pares">
                    <Numero
                      name="leves"
                      label="Tazas con defecto leve"
                      max={5}
                      initial={0}
                      invalido={campoInvalido === "leves"}
                    />
                    <Numero
                      name="graves"
                      label="Tazas con defecto grave"
                      max={5}
                      initial={0}
                      invalido={campoInvalido === "graves"}
                    />
                  </div>
                  <div className="cata-total">
                    <span>Puntaje final calculado</span>
                    <output
                      aria-label="Puntaje final calculado"
                      aria-live="polite"
                    >
                      {previo == null ? "Pendiente" : puntos(previo) + " / 100"}
                    </output>
                  </div>
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
                El sistema suma los diez atributos y descuenta 2 puntos por taza
                con defecto leve y 4 por defecto grave.
              </p>
            </section>
          </div>
        </>
      )}
    </div>
  );
}
