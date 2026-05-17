import { useEffect, useState } from "react";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { useI18n } from "../context/I18nContext.jsx";
import * as api from "../api/index.js";

export function Research() {
  const { auth, updateResearcherStatus } = useAuth();
  const { toast, Toasts } = useToast();
  const { t } = useI18n();

  const [tab, setTab] = useState("papers");
  const [papers, setPapers] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(false);
  const [becoming, setBecoming] = useState(false);
  const [field, setField] = useState("");

  const [showPaper, setShowPaper] = useState(false);
  const [showProject, setShowProject] = useState(false);
  const [citation, setCitation] = useState(null);
  const [pForm, setPForm] = useState({ title: "", journal: "", pages: 0, doi: "" });
  const [prForm, setPrForm] = useState({ journal: "", topic: "" });

  const load = () => {
    if (!auth?.isResearcher) return;
    setLoading(true);
    Promise.all([
      api.listPapers().catch(() => []),
      api.listProjects().catch(() => []),
    ]).then(([p, pr]) => { setPapers(p); setProjects(pr); }).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [auth?.isResearcher]);

  async function handleBecomeResearcher(e) {
    e.preventDefault();
    setBecoming(true);
    try {
      await api.becomeResearcher(field);
      toast(t("ui.researcher_status_unlocked"), "success");
      updateResearcherStatus(true);
    } catch (err) {
      toast(t(err?.message || "Failed to become researcher"), "error");
    } finally {
      setBecoming(false);
    }
  }

  async function handlePublish(e) {
    e.preventDefault();
    try { await api.publishPaper(pForm); toast(t("ui.paper_published"), "success"); setShowPaper(false); load(); }
    catch (err) { toast(t(err?.message || "Failed"), "error"); }
  }
  async function handleCreateProject(e) {
    e.preventDefault();
    try {
      await api.createProject(prForm);
      toast(t("ui.project_created"), "success");
      setShowProject(false);
      setPrForm({ journal: "", topic: "" });
      load();
    } catch (err) { toast(t(err?.message || "Failed"), "error"); }
  }
  async function handleCite(id) {
    try { const data = await api.getCitation(id, "BIBTEX"); setCitation(data.citation); }
    catch (err) { toast(t(err?.message || "Failed"), "error"); }
  }
  async function handleJoin(journal) {
    try { await api.joinProject(journal); toast(t("ui.joined_project"), "success"); load(); }
    catch (err) { toast(t(err?.message || "Failed"), "error"); }
  }
  async function handleSubscribe(journal) {
    try { await api.subscribe(journal); toast(t("ui.subscribed"), "success"); }
    catch (err) { toast(t(err?.message || "Failed"), "error"); }
  }

  if (!auth?.isResearcher) {
    return (
      <div className="page" style={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
        <Toasts />
        <div className="card" style={{ maxWidth: 460, width: "100%", textAlign: "center", padding: "40px 32px" }}>
          <div style={{ fontSize: 48, marginBottom: 16 }}>🔬</div>
          <h2 style={{ fontSize: 24, fontWeight: 700, marginBottom: 8 }}>{t("ui.unlock_researcher_mode")}</h2>
          <p style={{ color: "var(--text-2)", marginBottom: 24, fontSize: 14 }}>
            {t("ui.join_the_research_community_to_publish_p")}
          </p>
          <form onSubmit={handleBecomeResearcher} style={{ display: "flex", flexDirection: "column", gap: 16, textAlign: "left" }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.research_field")}</label>
              <input className="form-control" placeholder={t("ui.e_g_artificial_intelligence")} required
                     value={field} onChange={e => setField(e.target.value)} />
            </div>
            <button className="btn btn-primary" style={{ width: "100%", justifyContent: "center", marginTop: 8 }} disabled={becoming}>
              {becoming ? t("ui.activating") : t("ui.become_a_researcher")}
            </button>
          </form>
        </div>
      </div>
    );
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  const projectJournals = new Set(projects.map(p => p.journal));

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div>
          <h1>{t("student.menu.research")}</h1>
          <p>{t("ui.manage_your_publications_projects_and_ci")}</p>
        </div>
        <div style={{ display: "flex", gap: 12 }}>
          <button className="btn btn-secondary" onClick={() => setShowProject(true)}>＋ {t("ui.new_project")}</button>
          <button className="btn btn-primary" onClick={() => setShowPaper(true)}>＋ {t("ui.publish_paper")}</button>
        </div>
      </div>

      <div style={{ display: "flex", gap: 8, marginBottom: 24, borderBottom: "1px solid var(--border)", paddingBottom: 16 }}>
        {["papers", "projects"].map(name => (
          <button key={name} className={`btn ${tab === name ? "btn-primary" : "btn-secondary"}`} onClick={() => setTab(name)}>
            {name === "papers" ? `📄 ${t("ui.publications")}` : `🔬 ${t("ui.projects_1")}`}
          </button>
        ))}
      </div>

      {tab === "papers" && (
        <div className="card-grid">
          {papers.map(p => {
            const hasProject = projectJournals.has(p.journal);
            return (
              <div key={p.id} className="card card-sm" style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                <div className="fw-600" style={{ fontSize: 15 }}>{p.title}</div>
                <div className="text-muted text-sm">
                  {t("ui.by")} <strong>{p.authorFullName || p.author}</strong>
                  {p.authorFullName && <> @{p.author}</>}
                  {" · "}{p.journal}
                </div>
                <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
                  {p.publishedDate && <span className="badge badge-gray">{p.publishedDate}</span>}
                  <span className="badge badge-blue">{p.citations ?? 0} {t("ui.citations")}</span>
                  {p.pages > 0 && <span className="badge badge-purple">{p.pages} {t("ui.pages")}</span>}
                </div>
                <div style={{ display: "flex", gap: 8, marginTop: "auto", paddingTop: 12, borderTop: "1px solid var(--border)" }}>
                  <button className="btn btn-secondary btn-sm" style={{ flex: 1 }} onClick={() => handleCite(p.id)}>
                    {t("ui.cite")}
                  </button>
                  {hasProject && (
                    <button className="btn btn-secondary btn-sm" style={{ flex: 1 }} onClick={() => handleSubscribe(p.journal)}>
                      ★ {t("ui.subscribe")}
                    </button>
                  )}
                </div>
              </div>
            );
          })}
          {papers.length === 0 && (
            <div className="empty"><div className="empty-icon">📄</div><p>{t("ui.no_publications_found")}</p></div>
          )}
        </div>
      )}

      {tab === "projects" && (
        <div className="card-grid">
          {projects.map(p => (
            <div key={p.id} className="card card-sm" style={{ display: "flex", flexDirection: "column", gap: 8 }}>
              <div className="fw-600" style={{ fontSize: 15 }}>{p.journal}</div>
              <div className="text-muted text-sm">{p.topic || t("ui.research")}</div>
              <div className="text-muted text-sm">
                {t("ui.by")} <strong>{p.supervisorFullName || p.supervisor}</strong>
              </div>
              <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
                <span className="badge badge-blue">{p.participants?.length ?? 0} {t("ui.members")}</span>
                <span className="badge badge-purple">{p.publishedPapers?.length ?? p.publications?.length ?? 0} {t("ui.papers_1")}</span>
              </div>
              <div style={{ display: "flex", gap: 8, marginTop: "auto", paddingTop: 12, borderTop: "1px solid var(--border)" }}>
                <button className="btn btn-primary btn-sm" style={{ flex: 1 }} onClick={() => handleJoin(p.journal)}>
                  {t("ui.join_project")}
                </button>
                <button className="btn btn-secondary btn-sm" style={{ flex: 1 }} onClick={() => handleSubscribe(p.journal)}>
                  ★ {t("ui.subscribe")}
                </button>
              </div>
            </div>
          ))}
          {projects.length === 0 && (
            <div className="empty"><div className="empty-icon">🔬</div><p>{t("ui.no_active_projects")}</p></div>
          )}
        </div>
      )}

      {showPaper && (
        <Modal title={t("ui.publish_research_paper")} onClose={() => setShowPaper(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowPaper(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="paper-form">{t("ui.publish")}</button>
          </>}>
          <form id="paper-form" onSubmit={handlePublish} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.title")}</label>
              <input className="form-control" required value={pForm.title}
                     onChange={e => setPForm({ ...pForm, title: e.target.value })} />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.journal_venue")}</label>
              <input className="form-control" required value={pForm.journal}
                     onChange={e => setPForm({ ...pForm, journal: e.target.value })} />
            </div>
            <div className="form-row">
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>{t("ui.pages_1")}</label>
                <input className="form-control" type="number" min="0" value={pForm.pages}
                       onChange={e => setPForm({ ...pForm, pages: +e.target.value })} />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>{t("ui.doi_optional")}</label>
                <input className="form-control" value={pForm.doi}
                       onChange={e => setPForm({ ...pForm, doi: e.target.value })} />
              </div>
            </div>
          </form>
        </Modal>
      )}

      {showProject && (
        <Modal title={t("ui.create_research_project")} onClose={() => setShowProject(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowProject(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="proj-form">{t("ui.create")}</button>
          </>}>
          <form id="proj-form" onSubmit={handleCreateProject} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.journal_project_name")}</label>
              <input className="form-control" required value={prForm.journal}
                     onChange={e => setPrForm({ ...prForm, journal: e.target.value })} />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.research_field")}</label>
              <input className="form-control" required placeholder={t("ui.e_g_artificial_intelligence")}
                     value={prForm.topic} onChange={e => setPrForm({ ...prForm, topic: e.target.value })} />
            </div>
          </form>
        </Modal>
      )}

      {citation && (
        <Modal title={t("ui.bibtex_citation")} onClose={() => setCitation(null)}>
          <pre style={{ background: "var(--bg-3)", borderRadius: "var(--radius)", padding: 20, fontSize: 13,
                        color: "var(--text)", overflowX: "auto", whiteSpace: "pre-wrap", border: "1px solid var(--border)" }}>
            {citation}
          </pre>
        </Modal>
      )}
    </div>
  );
}
