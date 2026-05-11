import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { useI18n } from "../context/I18nContext.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function News() {
  const { auth } = useAuth();
  const { t } = useI18n();
  const { toast, Toasts } = useToast();
  const role = auth?.role;
  const isEmployee = !["Student","GraduateStudent","Admin"].includes(role);
  const [news, setNews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCompose, setShowCompose] = useState(false);
  const [commenting, setCommenting] = useState(null);
  const [comment, setComment] = useState("");
  const [form, setForm] = useState({ title:"", body:"", pinned: false });

  const load = () => api.listNews().then(setNews).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handlePublish(e) {
    e.preventDefault();
    try { await api.publishNews(form); toast(t("ui.news_published")); setShowCompose(false); load(); }
    catch (e) { toast(t(e?.message || "Failed"), "error"); }
  }
  async function handleComment(id) {
    if (!comment.trim()) return;
    try { await api.commentOnNews(id, comment); toast(t("ui.comment_posted")); setCommenting(null); setComment(""); load(); }
    catch (e) { toast(t(e?.message || "Failed"), "error"); }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  const pinned = news.filter(n => n.pinned);
  const regular = news.filter(n => !n.pinned);

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>{t("student.menu.news")}</h1></div>
        {isEmployee && <button className="btn btn-primary" onClick={() => setShowCompose(true)}>📰 {t("ui.publish")}</button>}
      </div>

      {pinned.length > 0 && (
        <>
          <div className="section-title">📌 {t("ui.pinned_1")}</div>
          {pinned.map(n => <NewsCard key={n.id} n={n} onComment={() => { setCommenting(n.id); setComment(""); }} />)}
        </>
      )}

      <div className="section-title mt-3">{t("ui.latest")}</div>
      {regular.map(n => <NewsCard key={n.id} n={n} onComment={() => { setCommenting(n.id); setComment(""); }} />)}
      {news.length === 0 && <div className="empty"><div className="empty-icon">📰</div><p>{t("news.empty")}</p></div>}

      {showCompose && (
        <Modal title={t("ui.publish_news")} onClose={() => setShowCompose(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowCompose(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="news-form">{t("ui.publish")}</button>
          </>}>
          <form id="news-form" onSubmit={handlePublish} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.title")}</label>
              <input className="form-control" required value={form.title} onChange={e => setForm({...form,title:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.body")}</label>
              <textarea className="form-control" rows="5" value={form.body} onChange={e => setForm({...form,body:e.target.value})} /></div>
            <label style={{ display:"flex", alignItems:"center", gap:8, fontSize:13, color:"var(--text-2)" }}>
              <input type="checkbox" checked={form.pinned} onChange={e => setForm({...form,pinned:e.target.checked})} />
              {t("ui.pin_this_post")}
            </label>
          </form>
        </Modal>
      )}

      {commenting && (
        <Modal title={t("ui.add_comment")} onClose={() => setCommenting(null)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setCommenting(null)}>{t("common.back")}</button>
            <button className="btn btn-primary" onClick={() => handleComment(commenting)}>{t("ui.post_1")}</button>
          </>}>
          <textarea className="form-control" rows="4" placeholder={t("ui.write_your_comment")}
            value={comment} onChange={e => setComment(e.target.value)} />
        </Modal>
      )}
    </div>
  );
}

function NewsCard({ n, onComment }) {
  const { t } = useI18n();
  return (
    <div className="card" style={{ marginBottom:14 }}>
      <div className="flex-between">
        <span className="fw-600">{n.title}</span>
        {n.pinned && <span className="badge badge-yellow">📌 {t("ui.pinned_1")}</span>}
      </div>
      <p style={{ color:"var(--text-2)", fontSize:13, marginTop:8, lineHeight:1.7 }}>{n.body}</p>
      <div className="flex-between mt-2" style={{ borderTop:"1px solid var(--border)", paddingTop:10 }}>
        <span className="text-muted text-sm">{t("ui.by")} {n.author} · {n.comments?.length ?? 0} {t("ui.comments")}</span>
        <button className="btn btn-secondary btn-sm" onClick={onComment}>💬 {t("ui.comment")}</button>
      </div>
      {n.comments?.length > 0 && (
        <div style={{ marginTop:10, display:"flex", flexDirection:"column", gap:6 }}>
          {n.comments.map((c, i) => (
            <div key={i} style={{ background:"var(--bg-3)", borderRadius:8, padding:"8px 12px", fontSize:13 }}>
              <span className="fw-600">{c.author}: </span><span className="text-muted">{c.text}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
