import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import * as api from "../api/index.js";

const NAV = [
  { label: "Dashboard",  icon: "🏠", to: "/",             roles: null },
  { section: "Academics" },
  { label: "Courses",    icon: "📚", to: "/courses",      roles: null },
  { label: "Transcript", icon: "📄", to: "/transcript",   roles: ["Student","GraduateStudent"] },
  { label: "Gradebook",  icon: "✏️",  to: "/gradebook",   roles: ["Teacher","Dean"] },
  { section: "Library" },
  { label: "Library",    icon: "📖", to: "/library",      roles: null },
  { section: "Research" },
  { label: "Research",   icon: "🔬", to: "/research",     roles: null },
  { section: "Communication" },
  { label: "Messages",   icon: "✉️",  to: "/messages",    roles: null },
  { label: "News",       icon: "📰", to: "/news",         roles: null },
  { label: "Requests",   icon: "🙋", to: "/requests",     roles: null },
  { label: "IT Orders",  icon: "🖥️",  to: "/orders",      roles: null },
  { section: "Admin" },
  { label: "Users",      icon: "👥", to: "/admin/users",  roles: ["Admin"] },
  { label: "Audit Logs", icon: "📋", to: "/admin/logs",   roles: ["Admin"] },
  { label: "Report",     icon: "📊", to: "/admin/report", roles: ["Admin"] },
];

export function Sidebar() {
  const { auth, signOut } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    try { await api.logout(); } catch (_) {}
    signOut();
    navigate("/login");
  }

  const role = auth?.role;

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="brand-name">University System</div>
        <div className="brand-sub">Management Portal</div>
      </div>

      {auth && (
        <div className="sidebar-user">
          <div className="user-name">{auth.username}</div>
          <div className="user-role">{role}</div>
        </div>
      )}

      <nav className="sidebar-nav">
        {NAV.map((item, i) => {
          if (item.section) return <div key={i} className="sidebar-section">{item.section}</div>;
          if (item.roles && !item.roles.includes(role)) return null;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
            >
              <span className="nav-icon">{item.icon}</span>
              {item.label}
            </NavLink>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <button className="nav-link" style={{color:"var(--danger)"}} onClick={handleLogout}>
          <span className="nav-icon">🚪</span> Logout
        </button>
      </div>
    </aside>
  );
}
