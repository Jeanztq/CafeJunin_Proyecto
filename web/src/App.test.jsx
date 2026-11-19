import React from "react";
import {
  render,
  screen,
  waitFor,
  cleanup,
  fireEvent,
} from "@testing-library/react";
import { afterEach, vi, test, expect } from "vitest";
import App from "./App";
import { api } from "./api";
vi.mock("./api", () => ({ api: vi.fn() }));
afterEach(() => {
  cleanup();
  vi.resetAllMocks();
});
function mock(role) {
  api.mockImplementation((path) =>
    Promise.resolve(
      path === "/me" ? { nombre: "Ana", roles: [role], local: true } : [],
    ),
  );
}
test("el productor ve cosecha y no revisiones de supervisor", async () => {
  mock("PRODUCTOR");
  render(<App config={{ seguridadActiva: false, predictor: "rules" }} />);
  await screen.findByText("Ana");
  expect(screen.queryByRole("button", { name: "Cosecha" })).toBeNull();
  expect(screen.queryByRole("button", { name: "Alertas" })).toBeNull();
  fireEvent.click(screen.getByRole("button", { name: "Lotes" }));
  fireEvent.click(screen.getByRole("button", { name: "Registrar cosecha" }));
  expect(
    screen.getByRole("button", { name: "Lotes" }).getAttribute("aria-current"),
  ).toBe("page");
  fireEvent.click(screen.getByRole("button", { name: "Volver a lotes" }));
  expect(
    screen.getByRole("heading", { name: "Lotes registrados" }),
  ).toBeTruthy();
});
test("el supervisor tiene riesgo y alertas, sin registro de cosecha", async () => {
  mock("SUPERVISOR");
  render(<App config={{ seguridadActiva: false, predictor: "xgboost" }} />);
  await screen.findByText("Ana");
  expect(screen.getByRole("button", { name: "Riesgo" })).toBeTruthy();
  expect(screen.getByRole("button", { name: /Alertas/ })).toBeTruthy();
  expect(screen.queryByRole("button", { name: "Cosecha" })).toBeNull();
});
test("el catador entra a calidad y solo dispone de sus módulos", async () => {
  mock("CATADOR");
  render(
    <App
      config={{ seguridadActiva: true }}
      identity={{ authenticated: true }}
    />,
  );
  const lotes = await screen.findByRole("button", { name: "Lotes y muestras" });
  await waitFor(() =>
    expect(
      screen
        .getByRole("button", { name: "Calidad" })
        .getAttribute("aria-current"),
    ).toBe("page"),
  );
  expect(
    Array.from(
      screen.getByRole("navigation").querySelectorAll("button"),
      (b) => b.textContent,
    ),
  ).toEqual(["Inicio", "Lotes y muestras", "Calidad"]);
  fireEvent.click(lotes);
  expect(
    screen.queryByRole("button", { name: "Registrar cosecha" }),
  ).toBeNull();
});
test("el acceso seguro usa login, sin seleccionar privilegios", () => {
  const login = vi.fn();
  render(
    <App
      config={{ seguridadActiva: true }}
      identity={{ authenticated: false, login }}
    />,
  );
  fireEvent.click(screen.getByRole("button", { name: /Iniciar sesión/ }));
  expect(login).toHaveBeenCalledOnce();
  expect(api).not.toHaveBeenCalled();
});
test("presenta errores del servidor", async () => {
  api.mockRejectedValue(new Error("Servidor no disponible"));
  render(<App config={{ seguridadActiva: false }} />);
  expect((await screen.findByRole("alert")).textContent).toContain(
    "Servidor no disponible",
  );
});

test("el transportista abre sus envíos sin consultar el catálogo de cosechas", async () => {
  mock("TRANSPORTISTA");
  render(<App config={{ seguridadActiva: false }} />);
  await screen.findByRole("heading", { name: "Envíos asignados" });
  expect(screen.getByRole("navigation").textContent).toBe("InicioMis envíos");
  expect(screen.queryByText("Asignar un lote")).toBeNull();
  expect(api.mock.calls.map(([ruta]) => ruta)).not.toContain("/lotes");
});
