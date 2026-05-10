import { useState, useCallback } from "react";

let id = 0;

export function useToast() {
  const [toasts, setToasts] = useState([]);

  const toast = useCallback((msg, type = "success") => {
    const key = ++id;
    setToasts((t) => [...t, { key, msg, type }]);
    setTimeout(() => setToasts((t) => t.filter((x) => x.key !== key)), 3500);
  }, []);

  function Toasts() {
    return (
      <div className="toast-container">
        {toasts.map((t) => (
          <div key={t.key} className={`toast ${t.type}`}>{t.msg}</div>
        ))}
      </div>
    );
  }

  return { toast, Toasts };
}
