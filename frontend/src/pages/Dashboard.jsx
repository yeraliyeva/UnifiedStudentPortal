import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import * as api from "../api/index.js";
import { Badge } from "../components/Badge.jsx";

export function Dashboard() {
  const { auth } = useAuth();
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
        <h1>Welcome back, {auth?.username} 👋</h1>
        <p>You are logged in as <strong>{role}</strong></p>
      </div>

      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-value">{courses.length}</div>
          <div className="stat-label">Courses available</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{msgs.length}</div>
          <div className="stat-label">Inbox messages</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{news.length}</div>
          <div className="stat-label">Latest news</div>
        </div>
      </div>

      <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:16 }}>
        <div className="card">
          <div className="section-title">Latest News</div>
          {news.length === 0 && <div className="empty"><p>No news</p></div>}
          {news.map((n) => (
            <div key={n.id} style={{ paddingBottom:12, marginBottom:12, borderBottom:"1px solid var(--border)" }}>
              <div className="flex-between">
                <span className="fw-600" style={{ fontSize:13 }}>{n.title}</span>
                {n.pinned && <Badge label="PINNED" />}
              </div>
              <p className="text-muted text-sm mt-1">{n.body?.slice(0, 100)}…</p>
            </div>
          ))}
        </div>

        <div className="card">
          <div className="section-title">Recent Messages</div>
          {msgs.length === 0 && <div className="empty"><p>Inbox empty</p></div>}
          {msgs.map((m) => (
            <div key={m.id} style={{ paddingBottom:10, marginBottom:10, borderBottom:"1px solid var(--border)" }}>
              <div className="flex-between">
                <span className="fw-600" style={{ fontSize:13 }}>{m.subject}</span>
                <Badge label={m.urgency} />
              </div>
              <p className="text-muted text-sm mt-1">from {m.sender}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
