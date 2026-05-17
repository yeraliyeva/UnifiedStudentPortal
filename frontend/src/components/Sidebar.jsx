import { useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { useI18n } from "../context/I18nContext.jsx";
import * as api from "../api/index.js";

const NAV = [
  { label: "Dashboard",  icon: "🏠", to: "/",             roles: null, key: "app.title" },
  { section: "Academics" },
  { label: "Courses",    icon: "📚", to: "/courses",      roles: null, key: "student.menu.view_courses" },
  { label: "Transcript", icon: "📄", to: "/transcript",   roles: ["Student","GraduateStudent"], key: "student.menu.transcript" },
  { label: "Gradebook",  icon: "✏️",  to: "/gradebook",   roles: ["Teacher","Dean"], key: "teacher.menu.put_marks" },
  { section: "Library" },
  { label: "Library",    icon: "📖", to: "/library",      roles: null, key: "student.menu.borrow" },
  { section: "Research" },
  { label: "Research",   icon: "🔬", to: "/research",     roles: null, key: "researcher.menu.cabinet" },
  { section: "Communication" },
  { label: "Messages",   icon: "✉️",  to: "/messages",    roles: null, key: "student.menu.inbox" },
  { label: "News",       icon: "📰", to: "/news",         roles: null, key: "student.menu.news" },
  { label: "Requests",   icon: "🙋", to: "/requests",     roles: null, key: "manager.menu.requests" },
  { label: "IT Orders",  icon: "🖥️",  to: "/orders",      roles: null, key: "tech.menu.new_orders" },
  { section: "Admin" },
  { label: "Users",      icon: "👥", to: "/admin/users",  roles: ["Admin"], key: "admin.menu.users" },
  { label: "Audit Logs", icon: "📋", to: "/admin/logs",   roles: ["Admin"], key: "admin.menu.logs" },
  { label: "Report",     icon: "📊", to: "/admin/report", roles: ["Admin"], key: "manager.menu.report" },
];

export function Sidebar() {
  const { auth, signOut } = useAuth();
  const { language, changeLanguage, t } = useI18n();
  const navigate = useNavigate();
  const [badges, setBadges] = useState({});

  useEffect(() => {
    if (!auth) return;
    const tasks = [
      api.inbox().then(rows =>
        ({ "/messages": rows.filter(m => m.status === "UNREAD").length })
      ).catch(() => ({})),
      api.listRequests().then(rows =>
        ({ "/requests": rows.filter(r => r.status === "PENDING").length })
      ).catch(() => ({})),
    ];
    Promise.all(tasks).then(parts =>
      setBadges(parts.reduce((acc, p) => ({ ...acc, ...p }), {}))
    );
  }, [auth]);

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
          <div className="sidebar-user-avatar">
            {auth.username.charAt(0).toUpperCase()}
          </div>
          <div className="sidebar-user-info">
            <div className="user-name">{auth.username}</div>
            <div className="user-role">{role} {auth.isResearcher && '🔬'}</div>
          </div>
        </div>
      )}

      <nav className="sidebar-nav">
        {NAV.map((item, i) => {
          if (item.section) return <div key={i} className="sidebar-section">{item.section}</div>;
          if (item.roles && !item.roles.includes(role)) return null;
          
          let displayLabel = item.label;
          if (item.key) {
             let translated = t(item.key);
             // backend properties sometimes contain \n=== MENU === which we want to clean up if we use it, 
             // but let's just use it as is or clean it slightly
             displayLabel = translated.replace(/\\n/g, '').replace(/===.*?===/g, '').replace(/---.*?---/g, '').trim();
             // If key wasn't found or cleaning resulted in empty string, fallback to label
             if (!translated || translated === item.key || displayLabel === '') {
                 displayLabel = item.label;
             }
          }
          
          const badgeCount = badges[item.to];
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
            >
              <span className="nav-icon">{item.icon}</span>
              <span style={{ flex: 1 }}>{displayLabel}</span>
              {badgeCount > 0 && <span className="nav-badge">{badgeCount}</span>}
            </NavLink>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <select 
          className="language-selector" 
          value={language} 
          onChange={(e) => changeLanguage(e.target.value)}
        >
          <option value="en">English (EN)</option>
          <option value="ru">Русский (RU)</option>
          <option value="kz">Қазақша (KZ)</option>
        </select>

        <button className="nav-link" style={{color:"var(--danger)", marginTop:"8px"}} onClick={handleLogout}>
          <span className="nav-icon">🚪</span> Logout
        </button>
      </div>
    </aside>
  );
}
