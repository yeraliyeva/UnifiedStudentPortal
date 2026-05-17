const ROLE_BADGE = {
  Admin: "badge-red", Student: "badge-blue", GraduateStudent: "badge-blue",
  Teacher: "badge-green", Dean: "badge-green", Manager: "badge-yellow",
  Librarian: "badge-purple", TechSupport: "badge-gray", EmployeeResearcher: "badge-purple",
};
const STATUS_BADGE = {
  PENDING: "badge-yellow", NEW: "badge-blue",
  ACCEPTED: "badge-green", APPROVED: "badge-green", DONE: "badge-green", PASSING: "badge-green",
  REJECTED: "badge-red", NOT_APPROVED: "badge-red", FAILING: "badge-red",
  VIEWED: "badge-blue", READ: "badge-gray", UNREAD: "badge-blue",
  LOW: "badge-gray", MEDIUM: "badge-yellow", HIGH: "badge-red",
  MAJOR: "badge-blue", MINOR: "badge-purple", FREE: "badge-gray",
  BACHELOR: "badge-blue", MASTER: "badge-yellow", DOCTORATE: "badge-red",
  A: "badge-green", B: "badge-green", C: "badge-yellow", D: "badge-yellow", F: "badge-red",
};

export function Badge({ label, tone }) {
  const key = tone ?? label;
  const cls = ROLE_BADGE[key] || STATUS_BADGE[key] || "badge-gray";
  return <span className={`badge ${cls}`}>{label}</span>;
}
