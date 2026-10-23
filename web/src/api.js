let identity;
export function setIdentity(value) {
  identity = value;
}
export async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json", ...options.headers };
  if (identity?.authenticated) {
    await identity.updateToken(30);
    headers.Authorization = `Bearer ${identity.token}`;
  }
  const response = await fetch(`/api${path}`, { ...options, headers });
  const data = await response.json().catch(() => null);
  if (!response.ok)
    throw new Error(data?.mensaje || `Solicitud fallida (${response.status}).`);
  return data;
}
