import { useEffect, useState } from "react";
import { useI18n } from "../context/I18nContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Messages() {
  const { toast, Toasts } = useToast();
  const { t } = useI18n();
  const [msgs, setMsgs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCompose, setShowCompose] = useState(false);
  const [form, setForm] = useState({ recipient:"", subject:"", body:"", urgency:"LOW" });

  const load = () => api.inbox().then(setMsgs).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleSend(e) {
    e.preventDefault();
    try { await api.sendMessage(form); toast(t("ui.message_sent")); setShowCompose(false); setForm({recipient:"",subject:"",body:"",urgency:"LOW"}); }
    catch (e) { toast(t(e?.message || "Failed"), "error"); }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>{t("employee.menu.messages")}</h1><p>{t("ui.0_in_inbox", msgs.length)}</p></div>
        <button className="btn btn-primary" onClick={() => setShowCompose(true)}>✉️ {t("ui.compose")}</button>
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>{t("ui.subject")}</th><th>{t("ui.from")}</th><th>{t("ui.urgency")}</th><th>{t("ui.status")}</th><th>{t("ui.sent")}</th></tr></thead>
            <tbody>
              {msgs.map(m => (
                <tr key={m.id}>
                  <td className="fw-600">{m.subject}</td>
                  <td className="text-muted">{m.sender}</td>
                  <td><Badge label={t(m.urgency)} /></td>
                  <td><Badge label={t(m.status)} /></td>
                  <td className="text-muted text-sm">{m.sentAt?.slice(0,16).replace("T"," ")}</td>
                </tr>
              ))}
              {msgs.length === 0 && <tr><td colSpan="5" style={{ textAlign:"center", color:"var(--text-2)" }}>{t("inbox.empty")}</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      {showCompose && (
        <Modal title={t("employee.menu.send_message")} onClose={() => setShowCompose(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowCompose(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="compose-form">{t("ui.send")}</button>
          </>}>
          <form id="compose-form" onSubmit={handleSend} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.to_username")}</label>
              <input className="form-control" required value={form.recipient} onChange={e => setForm({...form,recipient:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.subject")}</label>
              <input className="form-control" required value={form.subject} onChange={e => setForm({...form,subject:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.body")}</label>
              <textarea className="form-control" rows="4" value={form.body} onChange={e => setForm({...form,body:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.urgency")}</label>
              <select className="form-control" value={form.urgency} onChange={e => setForm({...form,urgency:e.target.value})}>
                <option>LOW</option><option>MEDIUM</option><option>HIGH</option>
              </select></div>
          </form>
        </Modal>
      )}
    </div>
  );
}
