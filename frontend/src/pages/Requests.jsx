import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { useI18n } from "../context/I18nContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

const HELP_TYPES = [
  "TRANSCRIPT_FOR_SEMESTER",
  "TRANSCRIPT_FOR_YEAR",
  "CERTIFICATE_OF_EDUCATION",
  "ACADEMIC_MOBILITY",
  "COORDINATION_OF_DIPLOMA_TOPIC",
  "REQUEST_FOR_CREATING_ORGANIZATION",
];
const URGENCIES = ["LOW", "MEDIUM", "HIGH"];

export function Requests() {
  const { auth } = useAuth();
  const { t } = useI18n();
  const { toast, Toasts } = useToast();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [expanded, setExpanded] = useState(null);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [form, setForm] = useState({ title: "", body: "", type: HELP_TYPES[0], urgency: "MEDIUM" });

  const canProcess = ["Manager", "Dean"].includes(auth?.role);

  const load = () => api.listRequests().then(setRequests).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      await api.submitRequest(form);
      toast(t("ui.request_submitted"));
      setShowModal(false);
      setForm({ title: "", body: "", type: HELP_TYPES[0], urgency: "MEDIUM" });
      load();
    } catch (err) {
      toast(t(err?.message || "Failed"), "error");
    }
  }

  async function decide(id, status) {
    try {
      await api.processRequest(id, status);
      toast(t(status));
      load();
    } catch (err) {
      toast(t(err?.message || "Failed"), "error");
    }
  }

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return requests.filter(r => {
      if (statusFilter !== "ALL" && r.status !== statusFilter) return false;
      if (!q) return true;
      return (
        r.title?.toLowerCase().includes(q) ||
        r.body?.toLowerCase().includes(q) ||
        r.requester?.toLowerCase().includes(q) ||
        r.requesterFullName?.toLowerCase().includes(q) ||
        t(r.type)?.toLowerCase().includes(q)
      );
    });
  }, [requests, search, statusFilter, t]);

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div>
          <h1>{t("ui.help_requests")}</h1>
          <p>{t("ui.0_requests", requests.length)}</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>＋ {t("ui.new_request")}</button>
      </div>

      <div style={{ display: "flex", gap: 12, marginBottom: 16, flexWrap: "wrap" }}>
        <input
          className="form-control"
          style={{ maxWidth: 360, flex: 1, minWidth: 200 }}
          placeholder={t("ui.search_requests")}
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <select
          className="form-control"
          style={{ maxWidth: 200 }}
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}
        >
          <option value="ALL">{t("ui.status")}: {t("ui.all")}</option>
          {["PENDING", "APPROVED", "REJECTED", "ACCEPTED", "NOT_APPROVED"].map(s =>
            <option key={s} value={s}>{t(s)}</option>
          )}
        </select>
      </div>

      <div className="card-grid">
        {filtered.map(r => {
          const open = expanded === r.id;
          return (
            <div key={r.id} className="card card-sm">
              <div className="flex-between">
                <span className="fw-600">{r.title}</span>
                <Badge label={t(r.status)} />
              </div>
              <div className="text-muted text-sm mt-1">
                {t(r.type)} · <Badge label={t(r.urgency)} />
              </div>
              <div className="text-muted text-sm mt-1">
                {t("ui.by")} <strong>{r.requesterFullName || r.requester}</strong>
                {r.requesterFullName && <span> (@{r.requester})</span>}
                {" · "}
                {r.createdAt?.slice(0, 16).replace("T", " ")}
              </div>

              {open && r.body && (
                <div style={{
                  marginTop: 8,
                  padding: "10px 12px",
                  background: "var(--bg-3)",
                  borderRadius: 8,
                  fontSize: 13,
                  whiteSpace: "pre-wrap",
                  color: "var(--text-2)"
                }}>
                  {r.body}
                </div>
              )}

              <div className="flex-between mt-2" style={{ gap: 8, flexWrap: "wrap" }}>
                <button className="btn btn-secondary btn-sm" onClick={() => setExpanded(open ? null : r.id)}>
                  {open ? t("ui.collapse") : t("ui.read_more")}
                </button>
                {canProcess && r.status === "PENDING" && (
                  <div style={{ display: "flex", gap: 6 }}>
                    <button className="btn btn-primary btn-sm" onClick={() => decide(r.id, "APPROVED")}>
                      ✓ {t("ui.approve")}
                    </button>
                    <button className="btn btn-secondary btn-sm" onClick={() => decide(r.id, "REJECTED")}>
                      ✗ {t("ui.reject")}
                    </button>
                  </div>
                )}
              </div>
            </div>
          );
        })}
        {filtered.length === 0 && (
          <div className="empty"><div className="empty-icon">🙋</div><p>{t("ui.no_requests")}</p></div>
        )}
      </div>

      {showModal && (
        <Modal title={t("ui.submit_help_request")} onClose={() => setShowModal(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="req-form">{t("ui.submit")}</button>
          </>}>
          <form id="req-form" onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.title")}</label>
              <input className="form-control" required value={form.title}
                     onChange={e => setForm({ ...form, title: e.target.value })} />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.type")}</label>
              <select className="form-control" value={form.type}
                      onChange={e => setForm({ ...form, type: e.target.value })}>
                {HELP_TYPES.map(opt => <option key={opt} value={opt}>{t(opt)}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.urgency")}</label>
              <select className="form-control" value={form.urgency}
                      onChange={e => setForm({ ...form, urgency: e.target.value })}>
                {URGENCIES.map(opt => <option key={opt} value={opt}>{t(opt)}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.description")}</label>
              <textarea className="form-control" rows="4" value={form.body}
                        onChange={e => setForm({ ...form, body: e.target.value })} />
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
