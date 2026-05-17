import { useEffect, useMemo, useRef, useState } from "react";

export function UserPicker({ users, value, onChange, placeholder, roles, disabled }) {
  const [query, setQuery] = useState(value || "");
  const [open, setOpen] = useState(false);
  const wrapRef = useRef(null);

  useEffect(() => { setQuery(value || ""); }, [value]);

  useEffect(() => {
    const close = (e) => {
      if (wrapRef.current && !wrapRef.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, []);

  const list = useMemo(() => {
    const pool = roles?.length ? users.filter(u => roles.includes(u.role)) : users;
    const q = query.trim().toLowerCase();
    if (!q) return pool;
    return pool.filter(u =>
      u.username.toLowerCase().includes(q) ||
      u.fullName?.toLowerCase().includes(q)
    );
  }, [users, query, roles]);

  const pick = (u) => {
    onChange(u.username);
    setQuery(u.username);
    setOpen(false);
  };

  return (
    <div ref={wrapRef} className="user-picker">
      <input
        className="form-control"
        placeholder={placeholder}
        value={query}
        disabled={disabled}
        onChange={e => { setQuery(e.target.value); onChange(e.target.value); setOpen(true); }}
        onFocus={() => setOpen(true)}
        autoComplete="off"
      />
      {open && list.length > 0 && (
        <div className="user-picker-menu">
          {list.slice(0, 12).map(u => (
            <button key={u.username} type="button"
                    className="user-picker-item" onClick={() => pick(u)}>
              <div className="fw-600">{u.fullName || u.username}</div>
              <div className="text-muted text-sm">
                @{u.username}{u.role ? ` · ${u.role}` : ""}
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
