import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

export function Orders() {
  const { auth } = useAuth();
  const { toast, Toasts } = useToast();
  const isTech = auth?.role === "TechSupport";
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ description:"", deviceType:"LAPTOP" });

  const load = () => (isTech ? api.listOrders() : Promise.resolve([])).then(setOrders).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleCreate(e) {
    e.preventDefault();
    try { await api.createOrder(form); toast("Order created!"); setShowModal(false); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleAccept(id) {
    try { await api.acceptOrder(id); toast("Order accepted!"); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }
  async function handleComplete(id) {
    try { await api.completeOrder(id); toast("Order completed!"); load(); }
    catch (e) { toast(e?.message || "Failed", "error"); }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div><h1>IT Orders</h1><p>{isTech ? `${orders.length} orders in queue` : "Submit a tech support order"}</p></div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>＋ New Order</button>
      </div>

      {isTech && (
        <div className="card">
          <div className="table-wrap">
            <table>
              <thead><tr><th>Device</th><th>Description</th><th>User</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {orders.map(o => (
                  <tr key={o.id}>
                    <td className="fw-600">{o.deviceType}</td>
                    <td className="text-muted">{o.description}</td>
                    <td>{o.requester}</td>
                    <td><Badge label={o.status} /></td>
                    <td>
                      <div style={{ display:"flex", gap:6 }}>
                        {o.status === "PENDING"   && <button className="btn btn-primary btn-sm" onClick={() => handleAccept(o.id)}>Accept</button>}
                        {o.status === "ACCEPTED"  && <button className="btn btn-success btn-sm" onClick={() => handleComplete(o.id)}>Complete</button>}
                      </div>
                    </td>
                  </tr>
                ))}
                {orders.length === 0 && <tr><td colSpan="5" style={{ textAlign:"center", color:"var(--text-2)" }}>No orders</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showModal && (
        <Modal title="Create IT Order" onClose={() => setShowModal(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
            <button className="btn btn-primary" form="order-form">Submit</button>
          </>}>
          <form id="order-form" onSubmit={handleCreate} style={{ display:"flex", flexDirection:"column", gap:14 }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>Device Type</label>
              <select className="form-control" value={form.deviceType} onChange={e => setForm({...form,deviceType:e.target.value})}>
                <option>LAPTOP</option><option>DESKTOP</option><option>PRINTER</option><option>PROJECTOR</option><option>OTHER</option>
              </select></div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Description</label>
              <textarea className="form-control" rows="3" value={form.description} onChange={e => setForm({...form,description:e.target.value})} /></div>
          </form>
        </Modal>
      )}
    </div>
  );
}
