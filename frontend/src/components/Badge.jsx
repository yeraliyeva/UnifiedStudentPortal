const ROLE_BADGE = {
  Admin: "badge-red", Student: "badge-blue", GraduateStudent: "badge-blue",
  Teacher: "badge-green", Dean: "badge-green", Manager: "badge-yellow",
  Librarian: "badge-purple", TechSupport: "badge-gray", EmployeeResearcher: "badge-purple",
};
const STATUS_BADGE = {
  PENDING: "badge-yellow", ACCEPTED: "badge-green", DONE: "badge-green",
  REJECTED: "badge-red", VIEWED: "badge-blue", PASSING: "badge-green",
  LOW: "badge-blue", MEDIUM: "badge-yellow", HIGH: "badge-red",
  A: "badge-green", B: "badge-green", C: "badge-yellow", D: "badge-yellow", F: "badge-red",
};

export function Badge({ label }) {
  const cls = ROLE_BADGE[label] || STATUS_BADGE[label] || "badge-gray";
  return <span className={`badge ${cls}`}>{label}</span>;
}
