import { useEffect, useState } from "react";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Research() {
  const { toast, Toasts } = useToast();
  const [tab, setTab] = useState("papers");
  const [papers, setPapers] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showPaper, setShowPaper] = useState(false);
  const [showProject, setShowProject] = useState(false);
  const [citation, setCitation] = useState(null);
  const [pForm, setPForm] = useState({ title:"", journal:"", pages:0, doi:"" });
  const [prForm, setPrForm] = useState({ journal:"" });

  const load = () => Promise.all([
    api.listPapers().catch(() => []),
    api.listProjects().catch(() => [])
  ]).then(([p,pr]) => { setPapers(p); setProjects(pr); }).finally(() => setLoading(false));

  useEffect(() => { load(); }, []);

  async function handlePublish(e) {
    e.preventDefault();
    try { await api.publishPaper(pForm); toast("Paper published!"); setShowPaper(false); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleCreateProject(e) {
    e.preventDefault();
    try { await api.createProject(prForm); toast("Project created!"); setShowProject(false); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleCite(id) {
    try { const data = await api.getCitation(id, "BIBTEX"); setCitation(data.citation); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleJoin(journal) {
    try { await api.joinProject(journal); toast("Joined project!"); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleSubscribe(journal) {
    try { await api.subscribe(journal); toast("Subscribed!"); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>Research</h1></div>
        <div style={{ display:"flex", gap:8 }}>
          <button className="btn btn-secondary" onClick={() => setShowProject(true)}>＋ Project</button>
          <button className="btn btn-primary" onClick={() => setShowPaper(true)}>＋ Publish Paper</button>
        </div>
      </div>

      <div style={{ display:"flex", gap:4, marginBottom:20 }}>
        {["papers","projects"].map(t => (
          <button key={t} className={`btn ${tab===t?"btn-primary":"btn-secondary"}`} onClick={() => setTab(t)}>
            {t === "papers" ? "📄 Papers" : "🔬 Projects"}
          </button>
        ))}
      </div>

      {tab === "papers" && (
        <div className="card-grid">
          {papers.map(p => (
            <div key={p.id} className="card card-sm">
              <div className="fw-600" style={{ marginBottom:4 }}>{p.title}</div>
              <div className="text-muted text-sm">by {p.author} · {p.journal}</div>
              <div className="text-muted text-sm">📅 {p.publishedDate} · 📊 {p.citations} citations · {p.pages} pages</div>
              <div style={{ display:"flex", gap:6, marginTop:8 }}>
                <button className="btn btn-secondary btn-sm" onClick={() => handleCite(p.id)}>Cite (BibTeX)</button>
                <button className="btn btn-secondary btn-sm" onClick={() => handleSubscribe(p.journal)}>Subscribe</button>
              </div>
            </div>
          ))}
          {papers.length === 0 && <div className="empty"><div className="empty-icon">📄</div><p>No papers yet</p></div>}
        </div>
      )}

      {tab === "projects" && (
        <div className="card-grid">
          {projects.map(p => (
            <div key={p.id} className="card card-sm">
              <div className="fw-600" style={{ marginBottom:4 }}>{p.journal}</div>
              <div className="text-muted text-sm">{p.members?.length ?? 0} members · {p.publications?.length ?? 0} papers</div>
              <button className="btn btn-primary btn-sm mt-2" onClick={() => handleJoin(p.journal)}>Join</button>
            </div>
          ))}
          {projects.length === 0 && <div className="empty"><div className="empty-icon">🔬</div><p>No projects yet</p></div>}
        </div>
      )}

      {showPaper && (
        <Modal title="Publish Research Paper" onClose={() => setShowPaper(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowPaper(false)}>Cancel</button>
            <button className="btn btn-primary" form="paper-form">Publish</button>
          </>}>
          <form id="paper-form" onSubmit={handlePublish} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>Title</label>
              <input className="form-control" required value={pForm.title} onChange={e => setPForm({...pForm,title:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Journal</label>
              <input className="form-control" required value={pForm.journal} onChange={e => setPForm({...pForm,journal:e.target.value})} /></div>
            <div className="form-row">
              <div className="form-group" style={{ marginBottom:0 }}><label>Pages</label>
                <input className="form-control" type="number" min="0" value={pForm.pages} onChange={e => setPForm({...pForm,pages:+e.target.value})} /></div>
              <div className="form-group" style={{ marginBottom:0 }}><label>DOI (optional)</label>
                <input className="form-control" value={pForm.doi} onChange={e => setPForm({...pForm,doi:e.target.value})} /></div>
            </div>
          </form>
        </Modal>
      )}

      {showProject && (
        <Modal title="Create Research Project" onClose={() => setShowProject(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowProject(false)}>Cancel</button>
            <button className="btn btn-primary" form="proj-form">Create</button>
          </>}>
          <form id="proj-form" onSubmit={handleCreateProject} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>Journal / Project Name</label>
              <input className="form-control" required value={prForm.journal} onChange={e => setPrForm({journal:e.target.value})} /></div>
          </form>
        </Modal>
      )}

      {citation && (
        <Modal title="BibTeX Citation" onClose={() => setCitation(null)}>
          <pre style={{ background:"var(--bg-3)", borderRadius:8, padding:16, fontSize:12, color:"var(--text)", overflowX:"auto", whiteSpace:"pre-wrap" }}>{citation}</pre>
        </Modal>
      )}
    </div>
  );
}
