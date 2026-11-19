import React from "react";
import { render, screen, fireEvent, cleanup } from "@testing-library/react";
import { test, expect, vi, afterEach } from "vitest";
import Alertas from "./Alertas";
import { api } from "./api";
vi.mock("./api", () => ({ api: vi.fn() }));
afterEach(() => {
  cleanup();
  vi.resetAllMocks();
});

const alerta = {
  id: "alerta-1",
  loteId: "lote-1",
  estado: "PENDIENTE",
  registradaEn: "2026-11-10T10:00:00Z",
};
const detalle = {
  alerta,
  lote: {
    id: alerta.loteId,
    peso: { kilogramos: 120 },
    fechaCosecha: "2026-11-09",
    variedad: "BOURBON",
  },
  parcela: { nombre: "El Mirador", ubicacion: "Junín" },
  evaluacion: {
    humedad: 95,
    horasDemora: 36,
    probabilidad: { valor: 0.985 },
    modelo: "reglas-ambientales-v1",
    simulada: true,
  },
  umbralAlerta: 0.7,
};

test("exige la evidencia, permite recuperarla y consulta la decisión guardada", async () => {
  api
    .mockRejectedValueOnce(new Error("Sin conexión"))
    .mockResolvedValueOnce(detalle)
    .mockResolvedValueOnce({
      ...alerta,
      estado: "CONFIRMADA",
      responsable: "supervisor-ana",
      motivo: "Medición verificada",
      instruccion: "Priorizar traslado",
      revisadaEn: "2026-11-10T11:00:00Z",
    });
  const alRevisar = vi.fn();
  render(
    <Alertas
      alertas={[alerta]}
      seleccion={alerta.id}
      seleccionar={vi.fn()}
      alRevisar={alRevisar}
    />,
  );
  await screen.findByRole("alert");
  expect(screen.queryByLabelText("Decisión")).toBeNull();
  fireEvent.click(screen.getByRole("button", { name: "Actualizar alerta" }));
  await screen.findByText("95 %");
  expect(screen.getByText("36 h")).toBeTruthy();
  expect(screen.getByText("El Mirador · Junín")).toBeTruthy();
  const decision = screen.getByLabelText("Decisión");
  expect(decision.value).toBe("");
  fireEvent.change(decision, { target: { value: "CONFIRMADA" } });
  fireEvent.change(screen.getByLabelText("Motivo de la decisión"), {
    target: { value: "Medición verificada" },
  });
  fireEvent.change(
    screen.getByLabelText("Instrucción para atender el riesgo"),
    { target: { value: "Priorizar traslado" } },
  );
  fireEvent.click(screen.getByRole("button", { name: "Registrar decisión" }));
  await screen.findByText("supervisor-ana");
  expect(
    screen.queryByRole("button", { name: "Registrar decisión" }),
  ).toBeNull();
  expect(alRevisar).toHaveBeenCalledOnce();
  const [ruta, opciones] = api.mock.calls.at(-1);
  expect(ruta).toBe("/alertas/alerta-1/decision");
  expect(JSON.parse(opciones.body)).toEqual({
    estado: "CONFIRMADA",
    motivo: "Medición verificada",
    instruccion: "Priorizar traslado",
    operacionId: expect.any(String),
  });
});

test("una revisión concurrente permite consultar la decisión existente", async () => {
  api
    .mockResolvedValueOnce(detalle)
    .mockRejectedValueOnce(
      Object.assign(new Error("La alerta ya fue revisada"), { status: 409 }),
    )
    .mockResolvedValueOnce({
      ...detalle,
      alerta: {
        ...alerta,
        estado: "DESCARTADA",
        motivo: "Lectura corregida",
        responsable: "otro-supervisor",
        revisadaEn: "2026-11-10T11:00:00Z",
      },
    });
  render(
    <Alertas
      alertas={[alerta]}
      seleccion={alerta.id}
      seleccionar={vi.fn()}
      alRevisar={vi.fn()}
    />,
  );
  fireEvent.change(await screen.findByLabelText("Decisión"), {
    target: { value: "DESCARTADA" },
  });
  fireEvent.change(screen.getByLabelText("Motivo de la decisión"), {
    target: { value: "Lectura corregida" },
  });
  expect(
    screen.queryByLabelText("Instrucción para atender el riesgo"),
  ).toBeNull();
  fireEvent.click(screen.getByRole("button", { name: "Registrar decisión" }));
  fireEvent.click(
    await screen.findByRole("button", { name: "Actualizar alerta" }),
  );
  await screen.findByText("otro-supervisor");
  expect(screen.queryByLabelText("Decisión")).toBeNull();
});
