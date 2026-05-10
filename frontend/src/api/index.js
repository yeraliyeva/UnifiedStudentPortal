import { get, post, del, put } from "./client.js";

export const login  = (username, password) => post("/login",  { username, password });
export const logout = ()                   => post("/logout");

export const listUsers    = ()         => get(`/users`);
export const getUser      = (username) => get(`/users/${username}`);
export const createStudent = (data)    => post("/users/students", data);
export const deleteUser   = (username) => del(`/users/${username}`);
export const getLogs      = ()         => get("/logs");
export const getReport    = ()         => get("/reports/academic");

export const listCourses  = ()            => get("/courses");
export const getCourse    = (id)          => get(`/courses/${id}`);
export const createCourse = (data)        => post("/courses", data);
export const enroll       = (id)          => post(`/courses/${id}/enroll`);
export const drop         = (id)          => post(`/courses/${id}/drop`);
export const recordMarks  = (id, data)    => post(`/courses/${id}/marks`, data);
export const viewGrades   = (id)          => get(`/courses/${id}/grades`);
export const transcript   = ()            => get("/transcript");

export const listBooks  = ()       => get("/books");
export const addBook    = (data)   => post("/books", data);
export const removeBook = (title)  => del(`/books/${encodeURIComponent(title)}`);
export const borrowBook = (title)  => post(`/books/${encodeURIComponent(title)}/borrow`);
export const returnBook = (title)  => post(`/books/${encodeURIComponent(title)}/return`);

export const inbox         = ()     => get("/messages/inbox");
export const sendMessage   = (data) => post("/messages", data);
export const listNews      = ()     => get("/news");
export const publishNews   = (data) => post("/news", data);
export const commentOnNews = (id, comment) => post(`/news/${id}/comment`, { comment });
export const listRequests  = ()     => get("/requests");
export const submitRequest = (data) => post("/requests", data);
export const listOrders    = ()     => get("/orders");
export const createOrder   = (data) => post("/orders", data);
export const acceptOrder   = (id)   => put(`/orders/${id}/accept`);
export const completeOrder = (id)   => put(`/orders/${id}/complete`);

export const listPapers    = ()        => get("/papers");
export const publishPaper  = (data)    => post("/papers", data);
export const getCitation   = (id, fmt) => get(`/papers/${id}/cite?format=${fmt}`);
export const listProjects  = ()        => get("/projects");
export const createProject = (data)    => post("/projects", data);
export const joinProject   = (journal) => post(`/projects/${encodeURIComponent(journal)}/join`);
export const subscribe     = (journal) => post("/subscriptions", { journal });
export const unsubscribe   = (journal) => del(`/subscriptions/${encodeURIComponent(journal)}`);
