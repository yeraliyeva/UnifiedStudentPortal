import { useEffect, useState } from "react";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Requests() {
  const { toast, Toasts } = useToast();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ title:"", body:"", helpType:"TECHNICAL" });

  const load = () => api.listRequests().then(setRequests).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    try { await api.submitRequest(form); toast("Request submitted!"); setShowModal(false); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>Help Requests</h1><p>{requests.length} requests</p></div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>＋ New Request</button>
      </div>
      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>Title</th><th>Type</th><th>Status</th><th>Created</th></tr></thead>
            <tbody>
              {requests.map(r => (
                <tr key={r.id}>
                  <td className="fw-600">{r.title}</td>
                  <td className="text-muted">{r.helpType}</td>
                  <td><Badge label={r.status} /></td>
                  <td className="text-muted text-sm">{r.createdAt?.slice(0,10)}</td>
                </tr>
              ))}
              {requests.length === 0 && <tr><td colSpan="4" style={{ textAlign:"center", color:"var(--text-2)" }}>No requests</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <Modal title="Submit Help Request" onClose={() => setShowModal(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
            <button className="btn btn-primary" form="req-form">Submit</button>
          </>}>
          <form id="req-form" onSubmit={handleSubmit} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>Title</label>
              <input className="form-control" required value={form.title} onChange={e => setForm({...form,title:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Description</label>
              <textarea className="form-control" rows="3" value={form.body} onChange={e => setForm({...form,body:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Type</label>
              <select className="form-control" value={form.helpType} onChange={e => setForm({...form,helpType:e.target.value})}>
                <option>TECHNICAL</option><option>ACADEMIC</option><option>FINANCIAL</option><option>OTHER</option>
              </select></div>
          </form>
        </Modal>
      )}
    </div>
  );
}
