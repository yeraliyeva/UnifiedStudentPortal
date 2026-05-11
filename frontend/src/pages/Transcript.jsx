import { useEffect, useState } from "react";
import { useI18n } from "../context/I18nContext.jsx";
import { Badge } from "../components/Badge.jsx";
import * as api from "../api/index.js";

export function Transcript() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const { t } = useI18n();

  useEffect(() => { api.transcript().then(setData).finally(() => setLoading(false)); }, []);

  if (loading) return <div className="page"><div className="spinner" /></div>;
  if (!data) return <div className="page"><div className="empty"><p>{t("ui.no_transcript_data")}</p></div></div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>{t("student.menu.transcript")}</h1>
        <p>{data.student} · {t(data.degree)} · {t("ui.year")} {data.year}</p>
      </div>

      <div className="stat-grid" style={{ marginBottom:24 }}>
        <div className="stat-card"><div className="stat-value">{(data.gpa ?? 0).toFixed(2)}</div><div className="stat-label">{t("ui.gpa")}</div></div>
        <div className="stat-card"><div className="stat-value">{data.failCount}</div><div className="stat-label">{t("ui.failed_courses")}</div></div>
        <div className="stat-card"><div className="stat-value">{data.courses?.length ?? 0}</div><div className="stat-label">{t("ui.total_courses")}</div></div>
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>{t("ui.course")}</th><th>{t("ui.1st_att")}</th><th>{t("ui.2nd_att")}</th><th>{t("ui.exam")}</th><th>{t("ui.total")}</th><th>{t("ui.grade")}</th></tr></thead>
            <tbody>
              {data.courses?.map((c, i) => (
                <tr key={i}>
                  <td className="fw-600">{c.courseName}</td>
                  <td>{c.firstHalf ?? "-"}</td>
                  <td>{c.secondHalf ?? "-"}</td>
                  <td>{c.exam ?? "-"}</td>
                  <td>{c.total ?? "-"}</td>
                  <td><Badge label={c.letter ?? "-"} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
