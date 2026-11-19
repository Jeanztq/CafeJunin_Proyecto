import React from "react";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";
import Calidad from "./Calidad";
import { api } from "./api";

vi.mock("./api", () => ({ api: vi.fn() }));
afterEach(() => {
  cleanup();
  vi.resetAllMocks();
});
const lote = {
  id: "cafe2401-0000-4000-8000-000000000001",
  variedad: "BOURBON",
  peso: { kilogramos: 120.5 },
};
const abrir = () =>
  render(<Calidad lotes={[lote]} parcelas={[]} cancelar={vi.fn()} />);
const campo = (label, value) =>
  fireEvent.change(screen.getByLabelText(label), { target: { value } });
const guardar = () =>
  fireEvent.click(screen.getByRole("button", { name: "Registrar evaluación" }));

test("permite corregir la ficha, reintentar y consultar el resultado confirmado", async () => {
  abrir();
  guardar();
  expect(screen.getByRole("alert").textContent).toContain("Defectos primarios");
  campo("Defectos primarios", "0");
  campo("Defectos secundarios", "2");
  for (const label of [
    "Fragancia / aroma",
    "Sabor",
    "Acidez",
    "Cuerpo",
    "Posgusto",
    "Balance",
    "Apreciación global",
  ]) {
    campo(label, "8");
  }
  for (const label of ["Uniformidad", "Dulzor", "Taza limpia"])
    campo(label, "10");
  campo("Acidez", "12");
  guardar();
  expect(screen.getByRole("alert").textContent).toContain("de 6 a 10");
  expect(api).not.toHaveBeenCalled();
  campo("Acidez", "7.5");
  campo("Tazas con defecto leve", "3");
  expect(
    screen.getByLabelText("Puntaje final calculado").textContent,
  ).toContain("79.50");
  api
    .mockRejectedValueOnce(new Error("Conexión interrumpida"))
    .mockImplementationOnce(async (_, options) => ({
      ...JSON.parse(options.body),
      loteId: lote.id,
      catadorNombre: "Ana",
      registradaEn: "2026-10-30T12:00:00Z",
      puntajeSca: 79.5,
      umbralEspecialidad: 80,
      protocolo: "SCA-2004",
      clasificacion: "Café comercial",
    }));
  guardar();
  await screen.findByText("Conexión interrumpida");
  guardar();
  await screen.findByText("79.50 puntos SCA");
  expect(JSON.parse(api.mock.calls[1][1].body)).not.toHaveProperty(
    "puntajeSca",
  );
  expect(api.mock.calls[1]).toEqual(api.mock.calls[0]);
  expect(screen.getByText("Café comercial")).toBeTruthy();
  expect(screen.getByText(/Ana · Catador/)).toBeTruthy();
  fireEvent.click(screen.getByText("Volver a la evaluación"));
  expect(screen.getByLabelText("Fragancia / aroma").value).toBe("");
});

test("cancelar antes de confirmar no envía una ficha", () => {
  const cancelar = vi.fn();
  render(<Calidad lotes={[lote]} parcelas={[]} cancelar={cancelar} />);
  fireEvent.click(screen.getByText("Cancelar"));
  expect(cancelar).toHaveBeenCalledOnce();
  expect(api).not.toHaveBeenCalled();
});
