import { useEffect, useState } from "react";
import { useI18n } from "../context/I18nContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Gradebook() {
  const { toast, Toasts } = useToast();
  const { t } = useI18n();
  const [courses, setCourses] = useState([]);
  const [selected, setSelected] = useState(null);
  const [grades, setGrades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ studentUsername:"", firstHalf:0, secondHalf:0, exam:0 });

  useEffect(() => { api.listCourses().then(setCourses).finally(() => setLoading(false)); }, []);

  async function loadGrades(id) {
    setSelected(id);
    setGrades([]);
    try {
      const data = await api.viewGrades(id);
      setGrades(Array.isArray(data) ? data : []);
    } catch (_) {}
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      await api.recordMarks(selected, form);
      toast(t("ui.marks_recorded"));
      setShowModal(false);
      loadGrades(selected);
    } catch (e) { toast(t(e?.message || "Failed"), "error"); }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header"><h1>{t("teacher.menu.put_marks")}</h1><p>{t("ui.select_a_course_to_view_or_enter_grades")}</p></div>

      <div style={{ display:"grid", gridTemplateColumns:"220px 1fr", gap:20 }}>
        <div style={{ display:"flex", flexDirection:"column", gap:8 }}>
          {courses.map(c => (
            <button key={c.id}
              className={`nav-link${selected === c.id ? " active" : ""}`}
              onClick={() => loadGrades(c.id)}>
              <span>{c.name}</span>
            </button>
          ))}
        </div>

        <div className="card">
          {!selected && <div className="empty"><div className="empty-icon">✏️</div><p>{t("ui.select_a_course")}</p></div>}
          {selected && (
            <>
              <div className="flex-between mb-3">
                <div className="section-title" style={{ marginBottom:0 }}>{t("ui.student_grades")}</div>
                <button className="btn btn-primary btn-sm" onClick={() => setShowModal(true)}>＋ {t("ui.record_marks")}</button>
              </div>
              <div className="table-wrap">
                <table>
                  <thead><tr><th>{t("ui.student")}</th><th>{t("ui.1st")}</th><th>{t("ui.2nd")}</th><th>{t("ui.exam")}</th><th>{t("ui.total")}</th><th>{t("ui.grade")}</th><th>{t("ui.passing")}</th></tr></thead>
                  <tbody>
                    {grades.map((g, i) => (
                      <tr key={i}>
                        <td className="fw-600">{g.student}</td>
                        <td>{g.firstHalf}</td><td>{g.secondHalf}</td><td>{g.exam}</td><td>{g.total}</td>
                        <td><Badge label={g.letter} /></td>
                        <td><Badge label={t(g.passing ? "PASSING" : "FAILING")} /></td>
                      </tr>
                    ))}
                    {grades.length === 0 && <tr><td colSpan="7" style={{ textAlign:"center", color:"var(--text-2)" }}>{t("ui.no_grades_yet")}</td></tr>}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      </div>

      {showModal && (
        <Modal title={t("ui.record_marks")} onClose={() => setShowModal(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="marks-form">{t("ui.save")}</button>
          </>}>
          <form id="marks-form" onSubmit={handleSubmit} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}>
              <label>{t("ui.student_username")}</label>
              <input className="form-control" required value={form.studentUsername} onChange={e => setForm({...form, studentUsername:e.target.value})} />
            </div>
            <div className="form-row">
              <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.1st_attestation_0_30")}</label>
                <input className="form-control" type="number" min="0" max="30" value={form.firstHalf} onChange={e => setForm({...form,firstHalf:+e.target.value})} /></div>
              <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.2nd_attestation_0_30")}</label>
                <input className="form-control" type="number" min="0" max="30" value={form.secondHalf} onChange={e => setForm({...form,secondHalf:+e.target.value})} /></div>
            </div>
            <div className="form-group" style={{ marginBottom:0 }}><label>{t("ui.exam_0_40")}</label>
              <input className="form-control" type="number" min="0" max="40" value={form.exam} onChange={e => setForm({...form,exam:+e.target.value})} /></div>
          </form>
        </Modal>
      )}
    </div>
  );
}
