import { useEffect, useState } from "react";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Gradebook() {
  const { toast, Toasts } = useToast();
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
      toast("Marks recorded!");
      setShowModal(false);
      loadGrades(selected);
    } catch (e) { toast(e?.message || "Failed", "error"); }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header"><h1>Gradebook</h1><p>Select a course to view or enter grades</p></div>

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
          {!selected && <div className="empty"><div className="empty-icon">✏️</div><p>Select a course</p></div>}
          {selected && (
            <>
              <div className="flex-between mb-3">
                <div className="section-title" style={{ marginBottom:0 }}>Student Grades</div>
                <button className="btn btn-primary btn-sm" onClick={() => setShowModal(true)}>＋ Record Marks</button>
              </div>
              <div className="table-wrap">
                <table>
                  <thead><tr><th>Student</th><th>1st</th><th>2nd</th><th>Exam</th><th>Total</th><th>Grade</th><th>Passing</th></tr></thead>
                  <tbody>
                    {grades.map((g, i) => (
                      <tr key={i}>
                        <td className="fw-600">{g.student}</td>
                        <td>{g.firstHalf}</td><td>{g.secondHalf}</td><td>{g.exam}</td><td>{g.total}</td>
                        <td><Badge label={g.letter} /></td>
                        <td><Badge label={g.passing ? "PASSING" : "FAILING"} /></td>
                      </tr>
                    ))}
                    {grades.length === 0 && <tr><td colSpan="7" style={{ textAlign:"center", color:"var(--text-2)" }}>No grades yet</td></tr>}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      </div>

      {showModal && (
        <Modal title="Record Marks" onClose={() => setShowModal(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
            <button className="btn btn-primary" form="marks-form">Save</button>
          </>}>
          <form id="marks-form" onSubmit={handleSubmit} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}>
              <label>Student Username</label>
              <input className="form-control" required value={form.studentUsername} onChange={e => setForm({...form, studentUsername:e.target.value})} />
            </div>
            <div className="form-row">
              <div className="form-group" style={{ marginBottom:0 }}><label>1st Attestation (0–30)</label>
                <input className="form-control" type="number" min="0" max="30" value={form.firstHalf} onChange={e => setForm({...form,firstHalf:+e.target.value})} /></div>
              <div className="form-group" style={{ marginBottom:0 }}><label>2nd Attestation (0–30)</label>
                <input className="form-control" type="number" min="0" max="30" value={form.secondHalf} onChange={e => setForm({...form,secondHalf:+e.target.value})} /></div>
            </div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Exam (0–40)</label>
              <input className="form-control" type="number" min="0" max="40" value={form.exam} onChange={e => setForm({...form,exam:+e.target.value})} /></div>
          </form>
        </Modal>
      )}
    </div>
  );
}
