import { useEffect, useState } from "react";
import { Badge } from "../components/Badge.jsx";
import * as api from "../api/index.js";

export function Transcript() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => { api.transcript().then(setData).finally(() => setLoading(false)); }, []);

  if (loading) return <div className="page"><div className="spinner" /></div>;
  if (!data) return <div className="page"><div className="empty"><p>No transcript data</p></div></div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Academic Transcript</h1>
        <p>{data.student} · {data.degree} · Year {data.year}</p>
      </div>

      <div className="stat-grid" style={{ marginBottom:24 }}>
        <div className="stat-card"><div className="stat-value">{(data.gpa ?? 0).toFixed(2)}</div><div className="stat-label">GPA</div></div>
        <div className="stat-card"><div className="stat-value">{data.failCount}</div><div className="stat-label">Failed Courses</div></div>
        <div className="stat-card"><div className="stat-value">{data.courses?.length ?? 0}</div><div className="stat-label">Total Courses</div></div>
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead><tr><th>Course</th><th>1st Att.</th><th>2nd Att.</th><th>Exam</th><th>Total</th><th>Grade</th></tr></thead>
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
