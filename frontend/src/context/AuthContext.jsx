import { createContext, useContext, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem("token");
    const username = localStorage.getItem("username");
    const role = localStorage.getItem("role");
    const isResearcher = localStorage.getItem("isResearcher") === "true";
    return token ? { token, username, role, isResearcher } : null;
  });


  function signIn(token, username, role, isResearcher) {
    localStorage.setItem("token", token);
    localStorage.setItem("username", username);
    localStorage.setItem("role", role);
    localStorage.setItem("isResearcher", isResearcher);
    setAuth({ token, username, role, isResearcher });
  }

  function signOut() {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    localStorage.removeItem("isResearcher");
    setAuth(null);
  }

  function updateResearcherStatus(status) {
    localStorage.setItem("isResearcher", status);
    setAuth(prev => prev ? { ...prev, isResearcher: status } : null);
  }

  return (
    <AuthContext.Provider value={{ auth, signIn, signOut, updateResearcherStatus }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
