import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { useI18n } from "../context/I18nContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { UserPicker } from "../components/UserPicker.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Gradebook() {
  const { auth } = useAuth();
  const { toast, Toasts } = useToast();
  const { t } = useI18n();
  const [courses, setCourses] = useState([]);
  const [directory, setDirectory] = useState([]);
  const [selected, setSelected] = useState(null);
  const [grades, setGrades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState({ studentUsername:"", firstHalf:0, secondHalf:0, exam:0 });

  useEffect(() => {
    api.listCourses().then(setCourses).finally(() => setLoading(false));
    api.userDirectory().then(setDirectory).catch(() => {});
  }, []);

  const myCourses = useMemo(() => {
    if (auth?.role === "Dean") return courses;
    return courses.filter(c => Array.isArray(c.teachers) && c.teachers.includes(auth?.username));
  }, [courses, auth]);

  const filteredCourses = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return myCourses;
    return myCourses.filter(c =>
      c.name?.toLowerCase().includes(q) || c.id?.toLowerCase().includes(q)
    );
  }, [myCourses, search]);

  const selectedCourse = useMemo(
    () => myCourses.find(c => c.id === selected),
    [myCourses, selected]
  );

  const enrolledStudents = useMemo(() => {
    if (!selectedCourse) return [];
    const enrolled = new Set(selectedCourse.students ?? []);
    return directory.filter(u => enrolled.has(u.username));
  }, [selectedCourse, directory]);

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

      <div style={{ display:"grid", gridTemplateColumns:"260px 1fr", gap:20 }}>
        <div style={{ display:"flex", flexDirection:"column", gap:8 }}>
          <input
            className="form-control"
            placeholder={t("ui.search_courses")}
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          {filteredCourses.map(c => (
            <button key={c.id}
              className={`nav-link${selected === c.id ? " active" : ""}`}
              onClick={() => loadGrades(c.id)}>
              <span>{c.name}</span>
              <small className="text-muted" style={{ display:"block", fontSize:11, marginTop:2 }}>
                {c.id} · {c.students?.length ?? 0} {t("ui.students").toLowerCase()}
              </small>
            </button>
          ))}
          {filteredCourses.length === 0 && (
            <div className="text-muted text-sm" style={{ padding:"12px" }}>
              {auth?.role === "Teacher" ? t("ui.no_courses_assigned") : t("course.list.empty")}
            </div>
          )}
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
                    {grades.map((g, i) => {
                      const user = directory.find(u => u.username === g.student);
                      return (
                        <tr key={i}>
                          <td className="fw-600">
                            {user?.fullName || g.student}
                            {user?.fullName && <span className="text-muted text-sm"> @{g.student}</span>}
                          </td>
                          <td>{g.firstHalf}</td>
                          <td>{g.secondHalf}</td>
                          <td>
                            {g.admittedToExam === false
                              ? <span className="badge badge-red" title={t("ui.not_admitted_hint")}>—</span>
                              : g.exam}
                          </td>
                          <td>{g.total}</td>
                          <td><Badge label={g.letter} /></td>
                          <td><Badge tone={g.passing ? "PASSING" : "FAILING"} label={t(g.passing ? "PASSING" : "FAILING")} /></td>
                        </tr>
                      );
                    })}
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
              <label>{t("ui.student")}</label>
              <UserPicker
                users={enrolledStudents}
                value={form.studentUsername}
                onChange={v => setForm({ ...form, studentUsername: v })}
                placeholder={t("ui.search_users")}
              />
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
