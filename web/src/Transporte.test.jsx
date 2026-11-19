import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { test, expect, vi } from "vitest";
import Transporte from "./Transporte";
import { api } from "./api";

vi.mock("./api", () => ({ api: vi.fn() }));

test("el supervisor actualiza el trayecto abierto y lee la incidencia del transportista", async () => {
  const envio = {
    loteId: "lote-123",
    transportista: "chofer",
    placa: "ABC-123",
    asignadoEn: "2026-11-10T10:00:00Z",
    salida: "2026-11-10T12:00:00Z",
    pesoCargaKg: 100,
  };
  const ficha = {
    envio,
    lote: { variedad: "BOURBON", peso: { kilogramos: 120 } },
    novedades: [],
  };
  const incidencia = {
    id: "parte-1",
    tipo: "DERRUMBE",
    lugar: "La Oroya",
    descripcion: "Paso interrumpido",
    registradaEn: "2026-11-10T13:00:00Z",
  };
  api
    .mockResolvedValueOnce([envio])
    .mockResolvedValueOnce([{ subject: "chofer", nombre: "Ana" }])
    .mockResolvedValueOnce(ficha)
    .mockResolvedValueOnce({ ...ficha, novedades: [incidencia] });
  render(
    <Transporte
      cuenta={{ subject: "supervisor", roles: ["SUPERVISOR"] }}
      lotes={[]}
    />,
  );
  fireEvent.click(await screen.findByRole("button", { name: "Ver envío" }));
  fireEvent.click(
    await screen.findByRole("button", { name: "Actualizar seguimiento" }),
  );
  await screen.findByText("Paso interrumpido");
  expect(screen.queryByRole("button", { name: "Registrar paso" })).toBeNull();
  expect(screen.queryByRole("radio", { name: /Incidencia/ })).toBeNull();
  expect(api.mock.calls.at(-1)).toEqual(["/transporte/envios/lote-123"]);
});

test("el paso normal admite solo el lugar y la incidencia exige problema y descripción", async () => {
  const envio = {
    loteId: "lote-1",
    transportista: "chofer",
    asignadoEn: "2026-11-10T10:00:00Z",
    salida: "2026-11-10T12:00:00Z",
    placa: "ABC-123",
    pesoCargaKg: 100,
  };
  const ficha = {
    envio,
    lote: { variedad: "BOURBON", peso: { kilogramos: 120 } },
    novedades: [],
  };
  const put = vi.fn((_ruta, options) =>
    Promise.resolve({
      ...JSON.parse(options.body),
      id: crypto.randomUUID(),
      registradaEn: envio.salida,
    }),
  );
  api.mockImplementation((ruta, options) =>
    options
      ? put(ruta, options)
      : Promise.resolve(ruta.endsWith("/envios") ? [envio] : ficha),
  );
  render(
    <Transporte
      cuenta={{ subject: "chofer", roles: ["TRANSPORTISTA"] }}
      lotes={[]}
    />,
  );
  fireEvent.click(await screen.findByRole("button", { name: "Ver envío" }));
  const lugar = await screen.findByRole("textbox", { name: "Punto de ruta" });
  fireEvent.change(lugar, { target: { value: "Huancayo" } });
  fireEvent.click(screen.getByRole("button", { name: "Registrar paso" }));
  await screen.findByText("Paso registrado en Huancayo.");
  expect(JSON.parse(put.mock.calls[0][1].body)).toEqual({
    tipo: "CONTROL",
    lugar: "Huancayo",
    descripcion: "",
  });
  fireEvent.click(screen.getByRole("radio", { name: /Incidencia/ }));
  fireEvent.change(lugar, { target: { value: "La Oroya" } });
  const enviar = screen.getByRole("button", { name: "Reportar incidencia" });
  fireEvent.click(enviar);
  expect(put).toHaveBeenCalledTimes(1);
  fireEvent.change(
    screen.getByRole("combobox", { name: "Tipo de incidencia" }),
    { target: { value: "DERRUMBE" } },
  );
  fireEvent.click(enviar);
  expect(put).toHaveBeenCalledTimes(1);
  fireEvent.change(
    screen.getByRole("textbox", { name: "Descripción de la incidencia" }),
    { target: { value: "Vía bloqueada" } },
  );
  fireEvent.click(enviar);
  await screen.findByText("Incidencia reportada en La Oroya.");
  expect(JSON.parse(put.mock.calls[1][1].body)).toEqual({
    tipo: "DERRUMBE",
    lugar: "La Oroya",
    descripcion: "Vía bloqueada",
  });
});
