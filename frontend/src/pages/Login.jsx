import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { useI18n } from "../context/I18nContext.jsx";
import * as api from "../api/index.js";

export function Login() {
  const { signIn } = useAuth();
  const { t } = useI18n();
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
      signIn(data.token, data.username, data.role, data.isResearcher);
      navigate("/");
    } catch (err) {
      setError(t(err?.message || "Invalid credentials"));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ minHeight:"100vh", display:"flex", alignItems:"center", justifyContent:"center", background:"var(--bg)" }}>
      <div style={{ width: "100%", maxWidth: 400, padding: "0 20px" }}>
        <div style={{ textAlign:"center", marginBottom:32 }}>
          <div style={{ fontSize:40, marginBottom:8 }}>🎓</div>
          <h1 style={{ fontSize:24, fontWeight:700, color:"var(--text)" }}>{t("ui.university_system")}</h1>
          <p style={{ color:"var(--text-2)", marginTop:4, fontSize:13 }}>{t("ui.sign_in_to_continue")}</p>
        </div>
        <form className="card" onSubmit={handleSubmit} style={{ display:"flex", flexDirection:"column", gap:16 }}>
          <div className="form-group" style={{ marginBottom:0 }}>
            <label>{t("ui.username")}</label>
            <input
              className="form-control"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              autoFocus
              autoComplete="username"
            />
          </div>
          <div className="form-group" style={{ marginBottom:0 }}>
            <label>{t("ui.password")}</label>
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
            {loading ? t("ui.signing_in") : t("ui.sign_in")}
          </button>
        </form>
        <p style={{ textAlign:"center", color:"var(--text-2)", fontSize:12, marginTop:20 }}>
          {t("ui.demo_admin_admin_u00a0_u00b7_u00a0_eve_e")}
        </p>
      </div>
    </div>
  );
}
