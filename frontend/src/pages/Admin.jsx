import { useEffect, useState } from "react";
import { useI18n } from "../context/I18nContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function AdminUsers() {
  const { toast, Toasts } = useToast();
  const { t } = useI18n();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ username:"", password:"", firstName:"", lastName:"", faculty:"SITE" });

  const load = () => api.listUsers().then(setUsers).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleCreate(e) {
    e.preventDefault();
    try { await api.createStudent(form); toast(t("ui.student_created")); setShowCreate(false); load(); }
    catch (e) { toast(t(e?.message || "Failed"), "error"); }
  }
  async function handleDelete(username) {
    if (!confirm(t(`Delete user "{0}"?`, username))) return;
    try { await api.deleteUser(username); toast(t("ui.user_deleted")); load(); }
    catch (e) { toast(t(e?.message || "Failed"), "error"); }
  }

  const filtered = users.filter(u =>
    u.username?.toLowerCase().includes(search.toLowerCase()) ||
    u.role?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>{t("admin.menu.users")}</h1><p>{t("ui.0_total_users", users.length)}</p></div>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>＋ {t("ui.create_student")}</button>
      </div>

      <input className="form-control mb-3" style={{ maxWidth:320 }} placeholder={t("ui.search_users")}
        value={search} onChange={e => setSearch(e.target.value)} />

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>{t("ui.username")}</th><th>{t("ui.full_name")}</th><th>{t("ui.email")}</th><th>{t("ui.faculty")}</th><th>{t("ui.role")}</th><th></th></tr></thead>
            <tbody>
              {filtered.map(u => (
                <tr key={u.username}>
                  <td className="fw-600">{u.username}</td>
                  <td>{u.fullName}</td>
                  <td className="text-muted text-sm">{u.email}</td>
                  <td className="text-muted text-sm">{u.faculty}</td>
                  <td><Badge label={t(u.role)} /></td>
                  <td>
                    <button className="btn btn-danger btn-sm" onClick={() => handleDelete(u.username)}>{t("admin.menu.delete_user")}</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showCreate && (
        <Modal title={t("ui.create_student")} onClose={() => setShowCreate(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowCreate(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="create-user-form">{t("ui.create")}</button>
          </>}>
          <form id="create-user-form" onSubmit={handleCreate} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-row">
              <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.username")}</label>
                <input className="form-control" required value={form.username} onChange={e => setForm({...form,username:e.target.value})} /></div>
              <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.password")}</label>
                <input className="form-control" type="password" required value={form.password} onChange={e => setForm({...form,password:e.target.value})} /></div>
            </div>
            <div className="form-row">
              <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.first_name")}</label>
                <input className="form-control" value={form.firstName} onChange={e => setForm({...form,firstName:e.target.value})} /></div>
              <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.last_name")}</label>
                <input className="form-control" value={form.lastName} onChange={e => setForm({...form,lastName:e.target.value})} /></div>
            </div>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.faculty")}</label>
              <select className="form-control" value={form.faculty} onChange={e => setForm({...form,faculty:e.target.value})}>
                <option>SITE</option><option>BS</option><option>ISEA</option><option>SE</option><option>SAM</option><option>LAW</option>
              </select></div>
          </form>
        </Modal>
      )}
    </div>
  );
}

export function AdminLogs() {
  const { t } = useI18n();
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => { api.getLogs().then(setLogs).finally(() => setLoading(false)); }, []);
  if (loading) return <div className="page"><div className="spinner" /></div>;
  return (
    <div className="page">
      <div className="page-header"><h1>{t("admin.menu.logs")}</h1><p>{t("ui.0_entries", logs.length)}</p></div>
      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>{t("ui.time")}</th><th>{t("ui.actor")}</th><th>{t("ui.action")}</th><th>{t("ui.details")}</th></tr></thead>
            <tbody>
              {logs.map((l, i) => (
                <tr key={i}>
                  <td className="text-muted text-sm" style={{ whiteSpace:"nowrap" }}>{l.timestamp?.slice(0,19).replace("T"," ")}</td>
                  <td className="fw-600">{l.actor}</td>
                  <td><span className="badge badge-blue">{t(l.action)}</span></td>
                  <td className="text-muted text-sm">{t(l.details)}</td>
                </tr>
              ))}
              {logs.length === 0 && <tr><td colSpan="4" style={{ textAlign:"center", color:"var(--text-2)" }}>{t("ui.no_logs")}</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export function AdminReport() {
  const { t } = useI18n();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => { api.getReport().then(setReport).finally(() => setLoading(false)); }, []);
  if (loading) return <div className="page"><div className="spinner" /></div>;
  if (!report) return <div className="page"><div className="empty"><p>{t("ui.no_report_data")}</p></div></div>;
  return (
    <div className="page">
      <div className="page-header"><h1>{t("manager.menu.report")}</h1></div>
      <div className="stat-grid">
        <div className="stat-card"><div className="stat-value">{report.totalStudents}</div><div className="stat-label">{t("ui.students")}</div></div>
        <div className="stat-card"><div className="stat-value">{report.totalCourses}</div><div className="stat-label">{t("ui.courses_1")}</div></div>
        <div className="stat-card"><div className="stat-value">{(report.averageGpa ?? 0).toFixed(2)}</div><div className="stat-label">{t("ui.avg_gpa")}</div></div>
        <div className="stat-card"><div className="stat-value">{report.failingStudents}</div><div className="stat-label">{t("ui.at_risk_students")}</div></div>
      </div>
      {report.topStudents?.length > 0 && (
        <div className="card">
          <div className="section-title">{t("ui.top_students_by_gpa")}</div>
          <div className="table-wrap">
            <table>
              <thead><tr><th>{t("ui.student")}</th><th>{t("ui.gpa")}</th></tr></thead>
              <tbody>
                {report.topStudents.map((s, i) => (
                  <tr key={i}><td className="fw-600">{s.username}</td><td>{s.gpa?.toFixed(2)}</td></tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
