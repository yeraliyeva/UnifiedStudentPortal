import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { useI18n } from "../context/I18nContext.jsx";
import * as api from "../api/index.js";
import { Badge } from "../components/Badge.jsx";

export function Dashboard() {
  const { auth } = useAuth();
  const { t } = useI18n();
  const role = auth?.role;
  const [news, setNews] = useState([]);
  const [msgs, setMsgs] = useState([]);
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.listNews().catch(() => []),
      api.inbox().catch(() => []),
      api.listCourses().catch(() => []),
    ]).then(([n, m, c]) => { setNews(n.slice?.(0,3) ?? []); setMsgs(m.slice?.(0,5) ?? []); setCourses(c.slice?.(0,4) ?? []); })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>{t("ui.welcome_back")}, {auth?.username} 👋</h1>
        <p>{t("ui.you_are_logged_in_as")} <strong>{t(role)}</strong></p>
      </div>

      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-value">{courses.length}</div>
          <div className="stat-label">{t("ui.courses_available")}</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{msgs.length}</div>
          <div className="stat-label">{t("ui.inbox_messages")}</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{news.length}</div>
          <div className="stat-label">{t("ui.latest_news")}</div>
        </div>
      </div>

      <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:16 }}>
        <div className="card">
          <div className="section-title">{t("ui.latest_news_1")}</div>
          {news.length === 0 && <div className="empty"><p>{t("news.empty")}</p></div>}
          {news.map((n) => (
            <div key={n.id} style={{ paddingBottom:12, marginBottom:12, borderBottom:"1px solid var(--border)" }}>
              <div className="flex-between">
                <span className="fw-600" style={{ fontSize:13 }}>{n.title}</span>
                {n.pinned && <Badge label={t("ui.pinned")} />}
              </div>
              <p className="text-muted text-sm mt-1">{n.body?.slice(0, 100)}…</p>
            </div>
          ))}
        </div>

        <div className="card">
          <div className="section-title">{t("ui.recent_messages")}</div>
          {msgs.length === 0 && <div className="empty"><p>{t("inbox.empty")}</p></div>}
          {msgs.map((m) => (
            <div key={m.id} style={{ paddingBottom:10, marginBottom:10, borderBottom:"1px solid var(--border)" }}>
              <div className="flex-between">
                <span className="fw-600" style={{ fontSize:13 }}>{m.subject}</span>
                <Badge tone={m.urgency} label={t(m.urgency)} />
              </div>
              <p className="text-muted text-sm mt-1">
                {t("ui.from_1")} {m.senderFullName || m.sender}
                {m.senderFullName && <> <span className="text-muted">@{m.sender}</span></>}
              </p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
