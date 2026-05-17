import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { useI18n } from "../context/I18nContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Courses() {
  const { auth } = useAuth();
  const { t } = useI18n();
  const { toast, Toasts } = useToast();
  const role = auth?.role;
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ name:"", credits:3, type:"MAJOR", capacity:30 });

  const load = () => api.listCourses().then(setCourses).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleEnroll(c) {
    try { await api.enroll(c.id); toast(t("enroll.success", c.name)); load(); }
    catch (e) { toast(t(e?.message || "Failed"), "error"); }
  }
  async function handleDrop(c) {
    try { await api.drop(c.id); toast(t("drop.success", c.name)); load(); }
    catch (e) { toast(t(e?.message || "Failed"), "error"); }
  }
  async function handleCreate(e) {
    e.preventDefault();
    try { await api.createCourse(form); toast(t("course.created", form.name)); setShowCreate(false); load(); }
    catch (e) { toast(t(e?.message || "Failed"), "error"); }
  }

  const filtered = courses.filter(c =>
    c.name?.toLowerCase().includes(search.toLowerCase()) ||
    c.id?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>{t("ui.courses_1")}</h1><p>{t("ui.0_courses_available", courses.length)}</p></div>
        {role === "Manager" && (
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}>＋ {t("ui.new_course")}</button>
        )}
      </div>

      <input
        className="form-control mb-3"
        style={{ maxWidth:320 }}
        placeholder={t("ui.search_courses")}
        value={search}
        onChange={e => setSearch(e.target.value)}
      />

      <div className="card-grid">
        {filtered.map(c => (
          <div key={c.id} className="card card-sm" style={{ display:"flex", flexDirection:"column", gap:8 }}>
            <div className="flex-between">
              <span className="fw-600">{c.name}</span>
              <Badge label={t(c.type)} />
            </div>
            <div className="text-muted text-sm">
              {t("ui.id")}: {c.id} · {c.credits} {t("ui.credits")} · {c.remainingSeats}/{c.capacity} {t("ui.seats")}
            </div>
            <div style={{ display:"flex", gap:8, marginTop:4 }}>
              {["Student","GraduateStudent"].includes(role) && !c.isFull && (
                <button className="btn btn-primary btn-sm" onClick={() => handleEnroll(c)}>{t("ui.enroll")}</button>
              )}
              {["Student","GraduateStudent"].includes(role) && (
                <button className="btn btn-secondary btn-sm" onClick={() => handleDrop(c)}>{t("ui.drop")}</button>
              )}
              {c.isFull && <Badge label={t("ui.full")} />}
            </div>
          </div>
        ))}
        {filtered.length === 0 && <div className="empty"><div className="empty-icon">📚</div><p>{t("course.list.empty")}</p></div>}
      </div>

      {showCreate && (
        <Modal title={t("manager.menu.create_course")} onClose={() => setShowCreate(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowCreate(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="create-course-form">{t("ui.create")}</button>
          </>}>
          <form id="create-course-form" onSubmit={handleCreate} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}>
              <label>{t("ui.course_name")}</label>
              <input className="form-control" required value={form.name} onChange={e => setForm({...form, name:e.target.value})} />
            </div>
            <div className="form-row">
              <div className="form-group" style={{ marginBottom:0 }}>
                <label>{t("ui.credits_1")}</label>
                <input className="form-control" type="number" min="1" max="10" value={form.credits} onChange={e => setForm({...form, credits:+e.target.value})} />
              </div>
              <div className="form-group" style={{ marginBottom:0 }}>
                <label>{t("ui.capacity")}</label>
                <input className="form-control" type="number" min="1" value={form.capacity} onChange={e => setForm({...form, capacity:+e.target.value})} />
              </div>
            </div>
            <div className="form-group" style={{ marginBottom:0 }}>
              <label>{t("ui.type")}</label>
              <select className="form-control" value={form.type} onChange={e => setForm({...form, type:e.target.value})}>
                <option>MAJOR</option><option>MINOR</option><option>FREE</option>
              </select>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
