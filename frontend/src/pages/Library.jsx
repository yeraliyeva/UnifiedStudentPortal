import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Library() {
  const { auth } = useAuth();
  const { toast, Toasts } = useToast();
  const role = auth?.role;
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [showAdd, setShowAdd] = useState(false);
  const [form, setForm] = useState({ title:"", author:"" });

  const load = () => api.listBooks().then(setBooks).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleBorrow(title) {
    try { await api.borrowBook(title); toast("Book borrowed!"); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleReturn(title) {
    try { await api.returnBook(title); toast("Book returned!"); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleRemove(title) {
    if (!confirm(`Remove "${title}"?`)) return;
    try { await api.removeBook(title); toast("Book removed"); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleAdd(e) {
    e.preventDefault();
    try { await api.addBook(form); toast("Book added!"); setShowAdd(false); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }

  const filtered = books.filter(b =>
    b.title?.toLowerCase().includes(search.toLowerCase()) ||
    b.author?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>Library</h1><p>{books.length} books in collection</p></div>
        {role === "Librarian" && <button className="btn btn-primary" onClick={() => setShowAdd(true)}>＋ Add Book</button>}
      </div>

      <input className="form-control mb-3" style={{ maxWidth:320 }} placeholder="Search books…"
        value={search} onChange={e => setSearch(e.target.value)} />

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>Title</th><th>Author</th><th>Status</th><th>Borrowed By</th><th>Actions</th></tr></thead>
            <tbody>
              {filtered.map(b => (
                <tr key={b.id}>
                  <td className="fw-600">{b.title}</td>
                  <td className="text-muted">{b.author}</td>
                  <td>
                    <span className={`badge ${b.borrowed ? "badge-yellow" : "badge-green"}`}>
                      {b.borrowed ? "Borrowed" : "Available"}
                    </span>
                  </td>
                  <td className="text-muted text-sm">{b.borrowedBy || "—"}</td>
                  <td>
                    <div style={{ display:"flex", gap:6 }}>
                      {!b.borrowed && <button className="btn btn-primary btn-sm" onClick={() => handleBorrow(b.title)}>Borrow</button>}
                      {b.borrowed && b.borrowedBy === auth?.username && (
                        <button className="btn btn-secondary btn-sm" onClick={() => handleReturn(b.title)}>Return</button>
                      )}
                      {role === "Librarian" && (
                        <button className="btn btn-danger btn-sm" onClick={() => handleRemove(b.title)}>Remove</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && <tr><td colSpan="5" style={{ textAlign:"center", color:"var(--text-2)" }}>No books found</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      {showAdd && (
        <Modal title="Add Book" onClose={() => setShowAdd(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowAdd(false)}>Cancel</button>
            <button className="btn btn-primary" form="add-book-form">Add</button>
          </>}>
          <form id="add-book-form" onSubmit={handleAdd} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>Title</label>
              <input className="form-control" required value={form.title} onChange={e => setForm({...form,title:e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Author</label>
              <input className="form-control" value={form.author} onChange={e => setForm({...form,author:e.target.value})} /></div>
          </form>
        </Modal>
      )}
    </div>
  );
}
