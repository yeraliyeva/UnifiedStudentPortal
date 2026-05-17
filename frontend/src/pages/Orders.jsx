import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { useI18n } from "../context/I18nContext.jsx";
import { Badge } from "../components/Badge.jsx";
import { Modal } from "../components/Modal.jsx";
import { useToast } from "../components/Toast.jsx";
import * as api from "../api/index.js";

const DEVICE_TYPES = ["LAPTOP", "DESKTOP", "PRINTER", "PROJECTOR", "OTHER"];

export function Orders() {
  const { auth } = useAuth();
  const { toast, Toasts } = useToast();
  const { t } = useI18n();
  const isTech = auth?.role === "TechSupport";
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ description: "", deviceType: "LAPTOP" });

  const load = () => api.listOrders().then(setOrders).catch(() => setOrders([])).finally(() => setLoading(false));
  useEffect(() => { load(); }, []);

  async function handleCreate(e) {
    e.preventDefault();
    try {
      await api.createOrder(form);
      toast(t("ui.order_created"));
      setShowModal(false);
      setForm({ description: "", deviceType: "LAPTOP" });
      load();
    } catch (err) { toast(t(err?.message || "Failed"), "error"); }
  }
  async function handleAccept(id) {
    try { await api.acceptOrder(id); toast(t("ui.order_accepted")); load(); }
    catch (err) { toast(t(err?.message || "Failed"), "error"); }
  }
  async function handleComplete(id) {
    try { await api.completeOrder(id); toast(t("ui.order_completed")); load(); }
    catch (err) { toast(t(err?.message || "Failed"), "error"); }
  }

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return orders.filter(o => {
      if (statusFilter !== "ALL" && o.status !== statusFilter) return false;
      if (!q) return true;
      return (
        o.description?.toLowerCase().includes(q) ||
        o.requester?.toLowerCase().includes(q) ||
        o.requesterFullName?.toLowerCase().includes(q)
      );
    });
  }, [orders, search, statusFilter]);

  if (loading) return <div className="page"><div className="spinner" /></div>;

  return (
    <div className="page">
      <Toasts />
      <div className="page-header flex-between">
        <div>
          <h1>{t("ui.it_orders")}</h1>
          <p>{isTech ? t("ui.0_orders_in_queue", orders.length) : t("ui.0_orders_in_queue", orders.length)}</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>＋ {t("ui.new_order")}</button>
      </div>

      <div style={{ display: "flex", gap: 12, marginBottom: 16, flexWrap: "wrap" }}>
        <input
          className="form-control"
          style={{ maxWidth: 360, flex: 1, minWidth: 200 }}
          placeholder={t("ui.search_orders")}
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <select
          className="form-control"
          style={{ maxWidth: 200 }}
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}
        >
          <option value="ALL">{t("ui.status")}: {t("ui.all")}</option>
          {["NEW", "ACCEPTED", "REJECTED", "DONE"].map(s =>
            <option key={s} value={s}>{t(s)}</option>
          )}
        </select>
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>{t("ui.description")}</th>
                <th>{t("ui.user")}</th>
                <th>{t("ui.status")}</th>
                <th>{t("ui.created")}</th>
                {isTech && <th>{t("ui.actions")}</th>}
              </tr>
            </thead>
            <tbody>
              {filtered.map(o => (
                <tr key={o.id}>
                  <td className="fw-600">{o.description}</td>
                  <td>
                    {o.requesterFullName || o.requester}
                    {o.requesterFullName && <span className="text-muted text-sm"> @{o.requester}</span>}
                  </td>
                  <td><Badge label={t(o.status)} /></td>
                  <td className="text-muted text-sm">{o.createdAt?.slice(0, 10)}</td>
                  {isTech && (
                    <td>
                      <div style={{ display: "flex", gap: 6 }}>
                        {o.status === "NEW" && (
                          <button className="btn btn-primary btn-sm" onClick={() => handleAccept(o.id)}>{t("ui.accept")}</button>
                        )}
                        {o.status === "ACCEPTED" && (
                          <button className="btn btn-success btn-sm" onClick={() => handleComplete(o.id)}>{t("ui.complete")}</button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan={isTech ? 5 : 4} style={{ textAlign: "center", color: "var(--text-2)" }}>{t("ui.no_orders")}</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <Modal title={t("ui.create_it_order")} onClose={() => setShowModal(false)}
          actions={<>
            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>{t("common.back")}</button>
            <button className="btn btn-primary" form="order-form">{t("ui.submit")}</button>
          </>}>
          <form id="order-form" onSubmit={handleCreate} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.device_type")}</label>
              <select className="form-control" value={form.deviceType}
                      onChange={e => setForm({ ...form, deviceType: e.target.value })}>
                {DEVICE_TYPES.map(d => <option key={d} value={d}>{t(d)}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>{t("ui.description")}</label>
              <textarea className="form-control" rows="3" value={form.description}
                        onChange={e => setForm({ ...form, description: e.target.value })} />
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
