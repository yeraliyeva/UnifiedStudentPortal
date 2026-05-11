import { useEffect, useState } from "react";
import { useI18n } from "../context/I18nContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Requests() {
  const { toast, Toasts } = useToast();
  const { t } = useI18n();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ title:"", body:"", helpType:"TECHNICAL" });

  const load = () => api.listRequests().then(setRequests).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    try { await api.submitRequest(form); toast(t("ui.request_submitted")); setShowModal(false); load(); }
    catch (e) { toast(t(e?.message || "Failed"), "error"); }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>{t("ui.help_requests")}</h1><p>{t("ui.0_requests", requests.length)}</p></div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>＋ {t("ui.new_request")}</button>
      </div>
      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>{t("ui.title")}</th><th>{t("ui.type")}</th><th>{t("ui.status")}</th><th>{t("ui.created")}</th></tr></thead>
            <tbody>
              {requests.map(r => (
                <tr key={r.id}>
                  <td className="fw-600">{r.title}</td>
                  <td className="text-muted">{t(r.helpType)}</td>
                  <td><Badge label={t(r.status)} /></td>
                  <td className="text-muted text-sm">{r.createdAt?.slice(0,10)}</td>
                </tr>
              ))}
              {requests.length === 0 && <tr><td colSpan="4" style={{ textAlign:"center", color:"var(--text-2)" }}>{t("ui.no_requests")}</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <Modal title={t("ui.submit_help_request")} onClose={() => setShowModal(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="req-form">{t("ui.submit")}</button>
          </>}>
          <form id="req-form" onSubmit={handleSubmit} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.title")}</label>
              <input className="form-control" required value={form.title} onChange={e => setForm({...form,title:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.description")}</label>
              <textarea className="form-control" rows="3" value={form.body} onChange={e => setForm({...form,body:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.type")}</label>
              <select className="form-control" value={form.helpType} onChange={e => setForm({...form,helpType:e.target.value})}>
                <option>TECHNICAL</option><option>ACADEMIC</option><option>FINANCIAL</option><option>OTHER</option>
              </select></div>
          </form>
        </Modal>
      )}
    </div>
  );
}
