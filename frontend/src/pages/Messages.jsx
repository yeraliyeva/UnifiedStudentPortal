import { useEffect, useState } from "react";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Messages() {
  const { toast, Toasts } = useToast();
  const [msgs, setMsgs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCompose, setShowCompose] = useState(false);
  const [form, setForm] = useState({ recipient:"", subject:"", body:"", urgency:"LOW" });

  const load = () => api.inbox().then(setMsgs).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleSend(e) {
    e.preventDefault();
    try { await api.sendMessage(form); toast("Message sent!"); setShowCompose(false); setForm({recipient:"",subject:"",body:"",urgency:"LOW"}); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>Messages</h1><p>{msgs.length} in inbox</p></div>
        <button className="btn btn-primary" onClick={() => setShowCompose(true)}>✉️ Compose</button>
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>Subject</th><th>From</th><th>Urgency</th><th>Status</th><th>Sent</th></tr></thead>
            <tbody>
              {msgs.map(m => (
                <tr key={m.id}>
                  <td className="fw-600">{m.subject}</td>
                  <td className="text-muted">{m.sender}</td>
                  <td><Badge label={m.urgency} /></td>
                  <td><Badge label={m.status} /></td>
                  <td className="text-muted text-sm">{m.sentAt?.slice(0,16).replace("T"," ")}</td>
                </tr>
              ))}
              {msgs.length === 0 && <tr><td colSpan="5" style={{ textAlign:"center", color:"var(--text-2)" }}>Inbox empty</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      {showCompose && (
        <Modal title="Compose Message" onClose={() => setShowCompose(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowCompose(false)}>Cancel</button>
            <button className="btn btn-primary" form="compose-form">Send</button>
          </>}>
          <form id="compose-form" onSubmit={handleSend} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>To (username)</label>
              <input className="form-control" required value={form.recipient} onChange={e => setForm({...form,recipient:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Subject</label>
              <input className="form-control" required value={form.subject} onChange={e => setForm({...form,subject:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Body</label>
              <textarea className="form-control" rows="4" value={form.body} onChange={e => setForm({...form,body:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Urgency</label>
              <select className="form-control" value={form.urgency} onChange={e => setForm({...form,urgency:e.target.value})}>
                <option>LOW</option><option>MEDIUM</option><option>HIGH</option>
              </select></div>
          </form>
        </Modal>
      )}
    </div>
  );
}
