import { useEffect, useMemo, useState } from "react";
import { useI18n } from "../context/I18nContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { UserPicker } from "../components/UserPicker.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

const URGENCIES = ["LOW", "MEDIUM", "HIGH"];

export function Messages() {
  const { toast, Toasts } = useToast();
  const { t } = useI18n();
  const [msgs, setMsgs] = useState([]);
  const [directory, setDirectory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [showCompose, setShowCompose] = useState(false);
  const [form, setForm] = useState({ recipient: "", subject: "", body: "", urgency: "LOW" });

  const load = () => api.inbox().then(setMsgs).finally(() => setLoading(false));
  useEffect(() => { load(); api.userDirectory().then(setDirectory).catch(() => {}); }, []);

  async function handleSend(e) {
    e.preventDefault();
    try {
      await api.sendMessage(form);
      toast(t("ui.message_sent"));
      setShowCompose(false);
      setForm({ recipient: "", subject: "", body: "", urgency: "LOW" });
    } catch (err) {
      toast(t(err?.message || "Failed"), "error");
    }
  }

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return msgs;
    return msgs.filter(m =>
      m.subject?.toLowerCase().includes(q) ||
      m.sender?.toLowerCase().includes(q) ||
      m.senderFullName?.toLowerCase().includes(q)
    );
  }, [msgs, search]);

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div>
          <h1>{t("ui.messages")}</h1>
          <p>{t("ui.0_in_inbox", msgs.length)}</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCompose(true)}>
          ✉️ {t("ui.compose")}
        </button>
      </div>

      <input
        className="form-control mb-3"
        style={{ maxWidth: 360 }}
        placeholder={t("ui.search_messages")}
        value={search}
        onChange={e => setSearch(e.target.value)}
      />

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>{t("ui.subject")}</th>
                <th>{t("ui.from")}</th>
                <th>{t("ui.urgency")}</th>
                <th>{t("ui.status")}</th>
                <th>{t("ui.sent")}</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(m => (
                <tr key={m.id}>
                  <td className="fw-600">{m.subject}</td>
                  <td className="text-muted">
                    {m.senderFullName
                      ? <><strong>{m.senderFullName}</strong> <span className="text-muted text-sm">@{m.sender}</span></>
                      : m.sender}
                  </td>
                  <td><Badge label={t(m.urgency)} /></td>
                  <td><Badge label={t(m.status)} /></td>
                  <td className="text-muted text-sm">{m.sentAt?.slice(0, 16).replace("T", " ")}</td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan="5" style={{ textAlign: "center", color: "var(--text-2)" }}>{t("inbox.empty")}</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showCompose && (
        <Modal title={t("ui.compose")} onClose={() => setShowCompose(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowCompose(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="compose-form">{t("ui.send")}</button>
          </>}>
          <form id="compose-form" onSubmit={handleSend} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.recipient")}</label>
              <UserPicker
                users={directory}
                value={form.recipient}
                onChange={v => setForm({ ...form, recipient: v })}
                placeholder={t("ui.to_username")}
              />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.subject")}</label>
              <input className="form-control" required value={form.subject}
                     onChange={e => setForm({ ...form, subject: e.target.value })} />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.body")}</label>
              <textarea className="form-control" rows="4" value={form.body}
                        onChange={e => setForm({ ...form, body: e.target.value })} />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.urgency")}</label>
              <select className="form-control" value={form.urgency}
                      onChange={e => setForm({ ...form, urgency: e.target.value })}>
                {URGENCIES.map(u => <option key={u} value={u}>{t(u)}</option>)}
              </select>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
