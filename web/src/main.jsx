import React from "react";
import { createRoot } from "react-dom/client";
import Keycloak from "keycloak-js";
import { api, setIdentity } from "./api";
import App from "./App";
import "./style.css";
async function start() {
  const config = await api("/config");
  let identity;
  if (config.seguridadActiva) {
    identity = new Keycloak({
      url: config.url,
      realm: config.realm,
      clientId: config.clientId,
    });
    await identity.init({
      onLoad: "check-sso",
      pkceMethod: "S256",
      checkLoginIframe: false,
    });
    setIdentity(identity);
  }
  createRoot(document.getElementById("root")).render(
    <App config={config} identity={identity} />,
  );
}
start().catch((e) => {
  const box = document.createElement("p");
  box.textContent = `No se pudo iniciar el portal: ${e.message}`;
  document.getElementById("root").replaceChildren(box);
});
