# Required Diagram Updates

Based on the final, perfectly-aligned codebase in `overworked_src/`, here are the detailed changes you need to make to your original UML Class Diagram and Use Case Diagram in StarUML to achieve 100% compliance with the actual system architecture.

## 1. Class Diagram Additions & Modifications

### New Classes & Interfaces
1. **`common.Messages` (Static Utility)**
   - Fields: `bundle: ResourceBundle`
   - Methods: `setLanguage(Language)`, `get(String)`, `fmt(String, Object...)`
   - Dependency: Used by almost every user and UI class.
2. **`common.LogManager` (Singleton)**
   - Fields: `instance: LogManager`, `logs: List<String>`
   - Methods: `getInstance()`, `log(username, action)`, `getLogs()`, `getLogsForUser(username)`
3. **`communication.Organization`**
   - Fields: `name: String`, `headUsername: String`, `members: List<String>`
   - Methods: `addMember()`, `removeMember()`, `isMember()`
   - Association: `Student` has a 0..* relationship with `Organization`.
4. **`users.ResearcherDecorator` (Decorator Pattern)**
   - Implements: `Researcher`
   - Fields: `user: User`, `papers: List<ResearchPaper>`, `projects: List<ResearchProject>`
   - Methods: `calculateHIndex()`, `addResearchPaper()`
5. **`interfaces.Subscriber` (Observer Pattern)**
   - Methods: `notifyNewPaper(journalName, paper)`
   - Realized by: `Teacher`, `Librarian`, `EmployeeResearcher`, `GraduateStudent`.

### Modified Classes
1. **`users.Student`**
   - **Add Fields**: `failCount: int`, `organizations: List<Organization>`
   - **Add Methods**: `recordFail()`, `getAvailableCredits()`, `rateTeacherInteractive()`, `joinOrganizationInteractive()`
2. **`users.Teacher`**
   - **Add Fields**: `ratings: List<Integer>`
   - **Add Methods**: `addRating(int)`, `getAverageRating()`, `putMarksInteractive()`.
3. **`communication.ResearchProject` (Subject in Observer Pattern)**
   - **Add Fields**: `subscribers: List<Subscriber>`
   - **Add Methods**: `subscribe(Subscriber)`, `notifySubscribers(ResearchPaper paper)`
4. **`communication.News`**
   - **Add Interface**: `Comparable<News>` (for auto-sorting pinned news to the top).
5. **Enums to Add**
   - `TeacherPosition` (PROFESSOR, LECTOR)
   - `ManagerPosition` (OR, DEPARTMENT, DEANS_OFFICE)
   - `Language` (ENGLISH, RUSSIAN, KAZAKH)

---

## 2. Use Case Diagram Additions

### New Actors
- Add **System** (actor) to represent automated background tasks (e.g., auto-generating News when a paper is published).

### Global Use Cases (Base `User`)
- **Change Language (i18n)**: Every user can change system language.

### Student Use Cases
- **Join/Create Organization**: Student interacts with Student Clubs.
- **View Teacher Info**: Student can look up their professors.
- **Rate Teacher**: Student provides a 1-10 rating.
- **Drop Course**: Student removes course from transcript.

### Manager Use Cases
- **View Academic Statistics**: Manager can see total students, courses, and average grades.
- **Assign Teacher to Course**: Manager links a Teacher to a Course.

### Admin Use Cases
- **View System Logs**: Admin accesses `LogManager` to see user actions.

### Researcher Use Cases (Teacher/GraduateStudent/EmployeeResearcher)
- **Publish Research Paper**: When executed, this use case **«includes»** the "Notify Subscribers" use case and "Auto-generate News" use case.

### Tech Support Use Cases
- **Accept Order**: Take ownership of a submitted ticket.
- **Complete Order**: Mark the ticket as done.
