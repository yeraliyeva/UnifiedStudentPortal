# University System — OOP Final Project

A console-based university management system built in **Java 17+**, designed as an OOP final project. The system models the full lifecycle of a university: user management, course registration, grading, research, communication, and administrative workflows.

## Quick Start

```bash
# Compile original
javac -d out/compiled -sourcepath src $(find src -name "*.java")
java -cp out/compiled Main

# Compile overworked version (all fixes applied)
javac -d out/overworked -sourcepath overworked_src $(find overworked_src -name "*.java")
java -cp out/overworked Main

# Run the Automated Test Suite (18 E2E and Unit Tests)
chmod +x test.sh
./test.sh
```

### Demo Accounts

| Username | Password  | Role               |
|----------|-----------|---------------------|
| admin    | admin123  | Admin               |
| alice    | pass123   | Teacher (Professor)  |
| bob      | pass123   | Teacher (Lector)     |
| carol    | pass123   | Dean                 |
| david    | pass123   | Manager (OR)         |
| eve      | pass123   | Student (Bachelor)   |
| frank    | pass123   | Student (Bachelor)   |
| grace    | pass123   | Graduate Student (Master) |
| henry    | pass123   | Librarian            |
| iris     | pass123   | Tech Support         |
| jack     | pass123   | Employee Researcher  |

---

## 📁 `src/` vs `overworked_src/`

- **`src/`** — Original implementation
- **`overworked_src/`** — Improved version with all bug fixes, missing features, and requirement compliance

---

## ✅ Changes Made in `overworked_src/`

### 🔴 Bug Fixes

| # | Bug | File | Fix |
|---|-----|------|-----|
| 1 | **GraduateStudent menu infinite loop** — `handleStudentChoice()` called `super.showMenu()` which started a nested menu loop | `GraduateStudent.java` | Replaced with flat switch-case dispatching individual Student actions directly |
| 2 | **Student credit limit was 60 instead of 21** — spec requires max 21 credits | `Student.java` | Changed `availableCredits = 21` |
| 3 | **Order.reject() reset status to NEW** — should be REJECTED | `Order.java`, `OrderStatus.java` | Added `REJECTED` to enum, `reject()` now sets `REJECTED` |
| 4 | **No equals/hashCode** — `contains()` checks relied on reference equality | `User.java`, `Course.java`, `Book.java` | Added proper `equals()`/`hashCode()` based on username/courseId/bookId |
| 5 | **Student.viewExamsSchedule()** filtered by PRACTICE instead of showing exam info | `Student.java` | Fixed to show relevant exam period info |

### 🟢 New Features Added

| # | Feature | Requirement | Files Changed/Created |
|---|---------|------------|----------------------|
| 1 | **Full i18n Language System (KZ/EN/RU)** | "Switching between languages" | NEW: `common/Messages.java` + `messages_*.properties` files. MODIFIED: All user/menu classes refactored to use `Messages.get()` instead of hardcoded `System.out.println` |
| 2 | **Student Organizations** | "Student organizations. Student can be a member/head" | NEW: `communication/Organization.java`. MODIFIED: `Student.java` (join/create/view org), `Database.java` (org storage), `DataSeeder.java` (demo data) |
| 3 | **Rate Teachers** | "Rate teachers" | MODIFIED: `Teacher.java` (ratings list, `addRating()`, `getAverageRating()`), `Student.java` (`rateTeacherInteractive()`) |
| 4 | **View Teacher Info** | Student should be able to view teacher info | MODIFIED: `Student.java` (`viewTeacherInfoInteractive()`) |
| 5 | **Auto-News on Paper Publish** | "When Researcher publishes paper, there must be an announcement" | MODIFIED: `ResearchProject.publishPaper()` — auto-creates `News` item with "Research:" prefix |
| 6 | **Auto-News for Top Researcher** | "Don't forget to automatically generate news about top cited Researcher" | MODIFIED: `Database.generateTopResearcherNews()`, `Admin.generateTopResearcherNews()` |
| 7 | **Pinned Research News** | Research news topic "will be always on the top" | MODIFIED: `News.java` — implements `Comparable`, `isPinned` flag. `Database.getAllNews()` returns sorted |
| 8 | **Student Fail Counter** | "Students can't fail more than 3 times" | MODIFIED: `Student.java` (failCount, `recordFail()`, check on enroll). `Teacher.putMarksInteractive()` calls `recordFail()` if total < 50 |
| 9 | **Real Action Logging** | Admin "See log files" | NEW: `common/LogManager.java` singleton. MODIFIED: All user classes log key actions. `Admin.viewLogs()` / `viewLogsForUser()` now prints real logs |
| 10 | **TeacherPosition Enum** | "use enumerations to represent teachers' position" | NEW: `enums/TeacherPosition.java` (TUTOR, LECTOR, SENIOR_LECTOR, PROFESSOR). MODIFIED: `Teacher.java` uses position enum instead of boolean |
| 11 | **Manager News Management** | Spec says manager manages news | MODIFIED: `Manager.java` — added add/edit/delete news to menu |
| 12 | **Sorted Student Views** | Manager should "view sorted students" | MODIFIED: `Manager.java` — `viewStudentsByGPA()`, `viewStudentsAlphabetically()`. `Database.java` — sorting methods |
| 13 | **Top Cited by Faculty** | "Print top cited researcher of school" | MODIFIED: `Database.getTopCitedResearcherByFaculty(Faculty)` |
| 14 | **Book ID Field** | Class diagram shows `bookId` | MODIFIED: `Book.java` — added `bookId` auto-increment field |
| 15 | **ResearchPaper DOI & Pages** | Requirement lists "doi, pages" | MODIFIED: `ResearchPaper.java` — added `pages`, `doi` fields + updated `getCitation()` |
| 16 | **Diploma Papers** | "Graduated students must have list of published papers as diploma projects" | MODIFIED: `GraduateStudent.java` — `diplomaPapers` list, `addDiplomaPaper()` |
| 17 | **ResearcherDecorator Usage** | Was dead code | MODIFIED: `DataSeeder.java` — demonstrates wrapping Bachelor student "eve" with decorator |

### 📄 New Files Created

| File | Purpose |
|------|---------|
| `enums/Language.java` | KZ, EN, RU language enum |
| `enums/TeacherPosition.java` | TUTOR, LECTOR, SENIOR_LECTOR, PROFESSOR |
| `communication/Organization.java` | Student organization (name, head, members) |
| `common/LogManager.java` | Singleton action logger for admin log viewing |

---

## 📊 Changes Needed in Diagrams

### Class Diagram Updates

| # | Change | Reason |
|---|--------|--------|
| 1 | **Add `Language` enum** to diagram (KAZAKH, RUSSIAN, ENGLISH) | New enum for language switching |
| 2 | **Add `language: Language` field to `User`** | User now has language preference |
| 3 | **Add `TeacherPosition` enum** (TUTOR, LECTOR, SENIOR_LECTOR, PROFESSOR) | Replace `isProfessor: boolean` in Teacher |
| 4 | **Replace `isProfessor: boolean` with `position: TeacherPosition`** in Teacher | More precise modeling per requirements |
| 5 | **Add `ratings: List<Integer>` to Teacher** | Student rating feature |
| 6 | **Add `addRating(int)`, `getAverageRating(): double`** methods to Teacher | Rating API |
| 7 | **Add `Organization` class** (id, name, headUsername, memberUsernames) | Student organizations |
| 8 | **Add relationship**: Student ──── Organization (many-to-many) | Students join orgs, one is head |
| 9 | **Add `failCount: int` to Student** | Fail tracking |
| 10 | **Change `availableECTS: int = 60` → `availableCredits: int = 21`** in Student | Correct credit limit |
| 11 | **Add `diplomaPapers: List<ResearchPaper>` to GraduateStudent** | Diploma project papers |
| 12 | **Add `bookId: int` to Book** | Book identifier |
| 13 | **Add `pages: int`, `doi: String` to ResearchPaper** | Missing fields from spec |
| 14 | **Add `pinned: boolean` to News** | Research news pinning |
| 15 | **Make News implement `Comparable<News>`** | Sorting support |
| 16 | **Add `REJECTED` to OrderStatus enum** | Missing status |
| 17 | **Add `LogManager` class** (singleton, logs list, log/getLogs/getLogsForUser) | Action logging |
| 18 | **Add `Organization` collection to Database** | Storage for organizations |
| 19 | **Add equals/hashCode** notation to User, Course, Book | Proper identity |

### Use Case Diagram Updates

| # | Change | Actor | Reason |
|---|--------|-------|--------|
| 1 | **Add "Rate Teacher"** use case | Student | New feature |
| 2 | **Add "View Teacher Info"** use case | Student | New feature |
| 3 | **Add "Manage Organizations"** use case cluster | Student | Create/join/view organizations |
| 4 | **Add "Manage News"** use cases to Manager | Manager | Manager now has add/edit/delete news |
| 5 | **Add "View Students Sorted"** use case | Manager | By GPA and alphabetically |
| 6 | **Add "View Logs / View User Logs"** use case | Admin | Real implementation now exists |
| 7 | **Add "Generate Top Researcher News"** use case | Admin | Auto-news feature |
| 8 | **Add "Switch Language"** use case | All Users | Language preference |
| 9 | **Add "Comment on News"** use case | All Users | Explicit in menu now |
| 10 | **Update "View News"** — add `<<extends>>` to show pinned/sorted behavior | All Users | Research news pinned to top |

---

## Architecture

```
src/ or overworked_src/
├── Main.java                  # Entry point
├── common/                    # Shared utilities
│   ├── AppScanner.java        # Singleton Scanner
│   ├── DataSeeder.java        # Bootstrap demo data
│   ├── Login.java             # Authentication
│   ├── LogManager.java        # ⭐ NEW: Action logging singleton
│   └── PaperComparators.java  # Strategy pattern: paper sorting
├── users/                     # User hierarchy
│   ├── User.java              # Abstract base class (+ Language, equals/hashCode)
│   ├── Employee.java          # Abstract employee (salary, hireDate)
│   ├── Student.java           # Student (+ rate teacher, organizations, fail counter)
│   ├── GraduateStudent.java   # Master/PhD (+ diploma papers, FIXED menu)
│   ├── Teacher.java           # Teacher (+ TeacherPosition, ratings)
│   ├── Dean.java              # Dean (extends Teacher)
│   ├── Admin.java             # Admin (+ real log viewing, top researcher news)
│   ├── Manager.java           # Manager (+ news management, sorted views)
│   ├── Librarian.java         # Library management
│   ├── TechSupport.java       # Technical support
│   ├── EmployeeResearcher.java# Non-teaching researcher
│   └── ResearcherDecorator.java# Decorator pattern
├── education/                 # Academic entities
│   ├── Course.java            # Course (+ equals/hashCode)
│   ├── Lesson.java            # Scheduled lesson slot
│   ├── AttestationResult.java # Att1 + Att2 + Exam marks
│   ├── Book.java              # Library book (+ bookId, equals/hashCode)
│   └── Specialty.java         # Academic program
├── communication/             # Messaging & research
│   ├── Message.java           # Internal message (Comparable)
│   ├── News.java              # News (+ Comparable, pinned flag)
│   ├── Request.java           # Formal admin request
│   ├── Order.java             # Tech support order (+ REJECTED status)
│   ├── Organization.java      # ⭐ NEW: Student organizations
│   ├── ResearchPaper.java     # Paper (+ pages, doi)
│   └── ResearchProject.java   # Project (+ auto-news on publish)
├── interfaces/                # Contracts
│   ├── Researcher.java        # Research capabilities + h-index
│   ├── Subscriber.java        # Observer: journal notifications
│   ├── Educationable.java     # Schedule/grade viewing
│   ├── CanBorrowBook.java     # Library borrowing
│   └── Managable.java         # Academic statistics
├── enums/                     # 15 enumerations (+2 new)
│   ├── Language.java           # ⭐ NEW: KAZAKH, RUSSIAN, ENGLISH
│   ├── TeacherPosition.java    # ⭐ NEW: TUTOR, LECTOR, SENIOR_LECTOR, PROFESSOR
│   └── ... (13 original enums)
├── exceptions/
│   ├── LowHIndexException.java
│   └── NotResearcherException.java
└── data/
    └── Database.java          # Singleton (+ organizations, sorted views, top by faculty)
```

## Design Patterns (6 total)

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| **Singleton** | `Database.getInstance()`, `LogManager.getInstance()` | Centralized state |
| **Factory Method** | `Admin.create*()` methods | Encapsulated user creation |
| **Observer** | `ResearchProject` → `Subscriber` | Notify on paper publication |
| **Decorator** | `ResearcherDecorator` wraps `User` | Dynamic research capability |
| **Strategy** | `PaperComparators` + `Researcher.printPapers(Comparator)` | Interchangeable sorting |
| **Comparable** | `Message`, `News` | Natural ordering |

## Class Hierarchy

```
User (abstract) [+ Language, equals/hashCode]
├── Admin [+ real logging, top researcher news]
├── Student [+ fail counter, 21 credits, rate teacher, organizations]
│   └── GraduateStudent (implements Researcher) [+ diploma papers, FIXED menu]
└── Employee (abstract)
    ├── Teacher (implements Researcher, CanBorrowBook, Subscriber) [+ TeacherPosition, ratings]
    │   └── Dean
    ├── Manager (implements Managable) [+ news CRUD, sorted student views]
    ├── Librarian (implements Subscriber)
    ├── TechSupport
    └── EmployeeResearcher (implements Researcher, Subscriber)
```

## UML Diagrams

- `ClassDiagram1.png` — Class diagram (see diagram changes above)
- `Use_Case_Diagram.png` — Use case diagrams (see diagram changes above)
- `OOP_Final_Project.pdf` — Requirements specification

## Requirements

- **Java 17+** (uses text blocks, switch expressions, pattern matching)
- No external dependencies
