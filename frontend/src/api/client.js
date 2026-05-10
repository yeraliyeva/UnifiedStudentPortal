const BASE = "http://localhost:8080/api";

export async function request(method, path, body) {
  const token = localStorage.getItem("token");
  const res = await fetch(BASE + path, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await res.json().catch(() => ({ message: res.statusText }));
  if (!res.ok) throw data;
  return data;
}

export const get  = (path)       => request("GET",    path);
export const post = (path, body) => request("POST",   path, body);
export const put  = (path, body) => request("PUT",    path, body);
export const del  = (path)       => request("DELETE", path);
