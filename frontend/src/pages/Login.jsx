import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import * as api from "../api/index.js";

export function Login() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const data = await api.login(form.username, form.password);
      signIn(data.token, data.username, data.role);
      navigate("/");
    } catch (err) {
      setError(err?.message || "Invalid credentials");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ minHeight:"100vh", display:"flex", alignItems:"center", justifyContent:"center", background:"var(--bg)" }}>
      <div style={{ width: "100%", maxWidth: 400, padding: "0 20px" }}>
        <div style={{ textAlign:"center", marginBottom:32 }}>
          <div style={{ fontSize:40, marginBottom:8 }}>🎓</div>
          <h1 style={{ fontSize:24, fontWeight:700, color:"var(--text)" }}>University System</h1>
          <p style={{ color:"var(--text-2)", marginTop:4, fontSize:13 }}>Sign in to continue</p>
        </div>
        <form className="card" onSubmit={handleSubmit} style={{ display:"flex", flexDirection:"column", gap:16 }}>
          <div className="form-group" style={{ marginBottom:0 }}>
            <label>Username</label>
            <input
              className="form-control"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              autoFocus
              autoComplete="username"
            />
          </div>
          <div className="form-group" style={{ marginBottom:0 }}>
            <label>Password</label>
            <input
              className="form-control"
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              autoComplete="current-password"
            />
          </div>
          {error && <div style={{ color:"var(--danger)", fontSize:13 }}>{error}</div>}
          <button className="btn btn-primary" disabled={loading} style={{ marginTop:4 }}>
            {loading ? "Signing in…" : "Sign in"}
          </button>
        </form>
        <p style={{ textAlign:"center", color:"var(--text-2)", fontSize:12, marginTop:20 }}>
          Demo: admin / admin &nbsp;·&nbsp; eve / eve &nbsp;·&nbsp; bob / bob
        </p>
      </div>
    </div>
  );
}
