import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext.jsx";
import { ProtectedLayout } from "./components/ProtectedLayout.jsx";
import { Login }       from "./pages/Login.jsx";
import { Dashboard }   from "./pages/Dashboard.jsx";
import { Courses }     from "./pages/Courses.jsx";
import { Transcript }  from "./pages/Transcript.jsx";
import { Gradebook }   from "./pages/Gradebook.jsx";
import { Library }     from "./pages/Library.jsx";
import { Messages }    from "./pages/Messages.jsx";
import { News }        from "./pages/News.jsx";
import { Requests }    from "./pages/Requests.jsx";
import { Orders }      from "./pages/Orders.jsx";
import { Research }    from "./pages/Research.jsx";
import { AdminUsers, AdminLogs, AdminReport } from "./pages/Admin.jsx";
import "./styles/global.css";
import "./styles/layout.css";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<ProtectedLayout />}>
            <Route index element={<Dashboard />} />
            <Route path="courses"       element={<Courses />} />
            <Route path="transcript"    element={<Transcript />} />
            <Route path="gradebook"     element={<Gradebook />} />
            <Route path="library"       element={<Library />} />
            <Route path="messages"      element={<Messages />} />
            <Route path="news"          element={<News />} />
            <Route path="requests"      element={<Requests />} />
            <Route path="orders"        element={<Orders />} />
            <Route path="research"      element={<Research />} />
            <Route path="admin/users"   element={<AdminUsers />} />
            <Route path="admin/logs"    element={<AdminLogs />} />
            <Route path="admin/report"  element={<AdminReport />} />
            <Route path="*"             element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
