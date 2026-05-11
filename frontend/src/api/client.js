const BASE = "http://localhost:8080/api";

export async function request(method, path, body) {
  const token = localStorage.getItem("token");
  const lang = localStorage.getItem("language") || "en";
  const res = await fetch(BASE + path, {
    method,
    headers: {
      "Content-Type": "application/json",
      "Accept-Language": lang,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await res.json().catch(() => ({ error: res.statusText }));
  if (!res.ok) throw new Error(data.error || data.message || res.statusText);
  return data;
}

export const get  = (path)       => request("GET",    path);
export const post = (path, body) => request("POST",   path, body);
export const put  = (path, body) => request("PUT",    path, body);
export const del  = (path)       => request("DELETE", path);
