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
      path === "/me"
        ? { nombre: "Ana", roles: [role], demostracion: true }
        : [],
    ),
  );
}
test("el productor ve cosecha y no revisiones de supervisor", async () => {
  mock("PRODUCTOR");
  render(<App config={{ seguridadActiva: false, predictor: "rules" }} />);
  await screen.findByText("Ana");
  expect(screen.getByRole("button", { name: "Cosecha" })).toBeTruthy();
  expect(screen.queryByRole("button", { name: "Alertas" })).toBeNull();
  fireEvent.click(screen.getByRole("button", { name: "Cosecha" }));
  expect(screen.getByText("Registrar cosecha")).toBeTruthy();
});
test("el supervisor tiene riesgo y alertas, sin registro de cosecha", async () => {
  mock("SUPERVISOR");
  render(<App config={{ seguridadActiva: false, predictor: "xgboost" }} />);
  await screen.findByText("Ana");
  expect(screen.getByRole("button", { name: /Riesgo IA/ })).toBeTruthy();
  expect(screen.getByRole("button", { name: /Alertas/ })).toBeTruthy();
  expect(screen.queryByRole("button", { name: "Cosecha" })).toBeNull();
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
