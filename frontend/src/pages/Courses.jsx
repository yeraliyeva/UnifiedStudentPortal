import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Courses() {
  const { auth } = useAuth();
  const { toast, Toasts } = useToast();
  const role = auth?.role;
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ name:"", credits:3, type:"MAJOR", capacity:30 });

  const load = () => api.listCourses().then(setCourses).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleEnroll(id) {
    try { await api.enroll(id); toast("Enrolled successfully!"); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleDrop(id) {
    try { await api.drop(id); toast("Dropped course"); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleCreate(e) {
    e.preventDefault();
    try { await api.createCourse(form); toast("Course created!"); setShowCreate(false); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
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
        <div><h1>Courses</h1><p>{courses.length} courses available</p></div>
        {role === "Manager" && (
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}>＋ New Course</button>
        )}
      </div>

      <input
        className="form-control mb-3"
        style={{ maxWidth:320 }}
        placeholder="Search courses…"
        value={search}
        onChange={e => setSearch(e.target.value)}
      />

      <div className="card-grid">
        {filtered.map(c => (
          <div key={c.id} className="card card-sm" style={{ display:"flex", flexDirection:"column", gap:8 }}>
            <div className="flex-between">
              <span className="fw-600">{c.name}</span>
              <Badge label={c.type} />
            </div>
            <div className="text-muted text-sm">
              ID: {c.id} · {c.credits} credits · {c.remainingSeats}/{c.capacity} seats
            </div>
            <div style={{ display:"flex", gap:8, marginTop:4 }}>
              {["Student","GraduateStudent"].includes(role) && !c.isFull && (
                <button className="btn btn-primary btn-sm" onClick={() => handleEnroll(c.id)}>Enroll</button>
              )}
              {["Student","GraduateStudent"].includes(role) && (
                <button className="btn btn-secondary btn-sm" onClick={() => handleDrop(c.id)}>Drop</button>
              )}
              {c.isFull && <Badge label="FULL" />}
            </div>
          </div>
        ))}
        {filtered.length === 0 && <div className="empty"><div className="empty-icon">📚</div><p>No courses found</p></div>}
      </div>

      {showCreate && (
        <Modal title="Create Course" onClose={() => setShowCreate(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowCreate(false)}>Cancel</button>
            <button className="btn btn-primary" form="create-course-form">Create</button>
          </>}>
          <form id="create-course-form" onSubmit={handleCreate} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}>
              <label>Course Name</label>
              <input className="form-control" required value={form.name} onChange={e => setForm({...form, name:e.target.value})} />
            </div>
            <div className="form-row">
              <div className="form-group" style={{ marginBottom:0 }}>
                <label>Credits</label>
                <input className="form-control" type="number" min="1" max="10" value={form.credits} onChange={e => setForm({...form, credits:+e.target.value})} />
              </div>
              <div className="form-group" style={{ marginBottom:0 }}>
                <label>Capacity</label>
                <input className="form-control" type="number" min="1" value={form.capacity} onChange={e => setForm({...form, capacity:+e.target.value})} />
              </div>
            </div>
            <div className="form-group" style={{ marginBottom:0 }}>
              <label>Type</label>
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
