import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { Sidebar } from "../components/Sidebar.jsx";

export function ProtectedLayout() {
  const { auth } = useAuth();
  if (!auth) return <Navigate to="/login" replace />;
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="main-content">
        <Outlet />
      </div>
    </div>
  );
}
