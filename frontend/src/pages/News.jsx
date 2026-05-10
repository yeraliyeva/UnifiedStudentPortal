import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function News() {
  const { auth } = useAuth();
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
    try { await api.publishNews(form); toast("News published!"); setShowCompose(false); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleComment(id) {
    if (!comment.trim()) return;
    try { await api.commentOnNews(id, comment); toast("Comment posted!"); setCommenting(null); setComment(""); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  const pinned = news.filter(n => n.pinned);
  const regular = news.filter(n => !n.pinned);

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>News Feed</h1></div>
        {isEmployee && <button className="btn btn-primary" onClick={() => setShowCompose(true)}>📰 Publish</button>}
      </div>

      {pinned.length > 0 && (
        <>
          <div className="section-title">📌 Pinned</div>
          {pinned.map(n => <NewsCard key={n.id} n={n} onComment={() => { setCommenting(n.id); setComment(""); }} />)}
        </>
      )}

      <div className="section-title mt-3">Latest</div>
      {regular.map(n => <NewsCard key={n.id} n={n} onComment={() => { setCommenting(n.id); setComment(""); }} />)}
      {news.length === 0 && <div className="empty"><div className="empty-icon">📰</div><p>No news yet</p></div>}

      {showCompose && (
        <Modal title="Publish News" onClose={() => setShowCompose(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowCompose(false)}>Cancel</button>
            <button className="btn btn-primary" form="news-form">Publish</button>
          </>}>
          <form id="news-form" onSubmit={handlePublish} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>Title</label>
              <input className="form-control" required value={form.title} onChange={e => setForm({...form,title:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Body</label>
              <textarea className="form-control" rows="5" value={form.body} onChange={e => setForm({...form,body:e.target.value})} /></div>
            <label style={{ display:"flex", alignItems:"center", gap:8, fontSize:13, color:"var(--text-2)" }}>
              <input type="checkbox" checked={form.pinned} onChange={e => setForm({...form,pinned:e.target.checked})} />
              Pin this post
            </label>
          </form>
        </Modal>
      )}

      {commenting && (
        <Modal title="Add Comment" onClose={() => setCommenting(null)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setCommenting(null)}>Cancel</button>
            <button className="btn btn-primary" onClick={() => handleComment(commenting)}>Post</button>
          </>}>
          <textarea className="form-control" rows="4" placeholder="Write your comment…"
            value={comment} onChange={e => setComment(e.target.value)} />
        </Modal>
      )}
    </div>
  );
}

function NewsCard({ n, onComment }) {
  return (
    <div className="card" style={{ marginBottom:14 }}>
      <div className="flex-between">
        <span className="fw-600">{n.title}</span>
        {n.pinned && <span className="badge badge-yellow">📌 Pinned</span>}
      </div>
      <p style={{ color:"var(--text-2)", fontSize:13, marginTop:8, lineHeight:1.7 }}>{n.body}</p>
      <div className="flex-between mt-2" style={{ borderTop:"1px solid var(--border)", paddingTop:10 }}>
        <span className="text-muted text-sm">by {n.author} · {n.comments?.length ?? 0} comments</span>
        <button className="btn btn-secondary btn-sm" onClick={onComment}>💬 Comment</button>
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
