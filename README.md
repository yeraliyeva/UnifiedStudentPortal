# University Management System — OOP Final Project

A console-based university management system built in **Java 17+** as an OOP final project. The system models the full lifecycle of a university: user management, course registration, grading, research, communication, and administrative workflows.

---

## Quick Start

```bash
# Build and run EVERYTHING (Backend API + React Frontend)
bash scripts/start.sh
```

This will automatically build the backend JAR, install frontend dependencies, and start both servers concurrently. Open `http://localhost:5173` in your browser.

---

### Manual Execution

```bash
# Build the self-contained JAR
bash scripts/build.sh
```

This produces `university-system.jar` in the project root.

```bash
# Run — interactive CLI (default)
java -jar university-system.jar

# Run — REST API server (port 8080)
java -jar university-system.jar --server

# Run — REST API on a custom port
java -jar university-system.jar --server 9000

# Custom data directory (default: ./data)
java -Duni.data=/path/to/data -jar university-system.jar

# Run the test suite (52 tests)
bash scripts/test.sh
```

### Demo Accounts

| Username | Password | Role                      |
|----------|----------|---------------------------|
| admin    | admin123 | Admin                     |
| alice    | pass123  | Teacher (Professor)       |
| bob      | pass123  | Teacher (Lector)          |
| carol    | pass123  | Dean                      |
| david    | pass123  | Manager (OR)              |
| eve      | pass123  | Student (Bachelor)        |
| frank    | pass123  | Student (Bachelor)        |
| grace    | pass123  | Graduate Student (Master) |
| henry    | pass123  | Librarian                 |
| iris     | pass123  | Tech Support              |
| jack     | pass123  | Employee Researcher       |

---

## Architecture

The project uses a layered **Clean Architecture** with strict dependency direction: `presentation` → `application` → `domain` ← `infrastructure`.

```
src/
├── Main.java                        # Entry point
├── domain/                          # Pure business logic — no I/O
│   ├── user/                        # User aggregate (Student, Teacher, Dean, …)
│   ├── course/                      # Course aggregate (Course, Lesson, Grade, …)
│   ├── research/                    # ResearchPaper, ResearchProject
│   ├── library/                     # Book
│   ├── messaging/                   # Message, News, Request, Order, Notification
│   ├── organization/                # Student Organization
│   ├── logging/                     # LogEntry
│   ├── rules/                       # EnrollmentRule chain
│   ├── service/                     # Domain services (EnrollmentService, PaperPublisher, …)
│   ├── repository/                  # Repository interfaces (ports)
│   ├── shared/                      # Value objects (Username, Credits, Money, …)
│   └── enums/                       # All enumerations
├── application/                     # Use-case orchestration
│   ├── usecase/
│   │   ├── admin/                   # CreateTeacher, DeleteUser, GenerateTopResearcherNews, …
│   │   ├── course/                  # EnrollInCourse, DropCourse, RecordMarks, …
│   │   ├── library/                 # BorrowBook, ReturnBook, …
│   │   ├── messaging/               # SendMessage, PublishNews, CreateITOrder, …
│   │   ├── organization/            # CreateOrganization, JoinOrganization
│   │   ├── research/                # PublishPaper, SubscribeToJournal, SetSupervisor, …
│   │   └── user/                    # BecomeResearcher, RateTeacher, ComplainAboutStudent
│   └── Result.java                  # Success/failure wrapper
├── infrastructure/                  # Adapters — implements domain interfaces
│   ├── auth/                        # PlainPasswordHasher
│   ├── i18n/                        # PropertiesTranslator (KZ/EN/RU)
│   ├── logging/                     # RepositoryLogger
│   └── persistence/
│       ├── json/                    # Hand-rolled JSON parser/writer (no deps)
│       ├── database/                # Database interface + JsonFileDatabase
│       ├── orm/                     # Generic Repository<T,ID> + QueryBuilder
│       ├── mapper/                  # EntityMapper implementations (User, Course, …)
│       └── inmemory/                # In-memory repositories (used in tests)
├── presentation/
│   └── cli/
│       ├── auth/                    # LoginScreen
│       └── menu/                    # Role-specific menus (StudentMenu, TeacherMenu, …)
├── bootstrap/                       # AppContext (wiring), DataSeeder, DefaultMenuFactory
├── resources/                       # messages_en/kz/ru.properties (i18n)
└── test/                            # 52 unit & integration tests (no test framework)
```

---

## Design Patterns

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| **Singleton** | `AppContext` wires single instances | Centralized dependency graph |
| **Factory Method** | `DefaultMenuFactory.menuFor(user)` | Role → menu selection |
| **Chain of Responsibility** | `EnrollmentRule` chain | Ordered enrollment validation |
| **Observer** | `PaperPublisher` → `NotificationRepository` | Notify subscribers on paper publish |
| **Decorator** | `BecomeResearcher` activates `ResearcherProfile` | Dynamic capability extension |
| **Strategy** | `CitationFormatter` (plain / BibTeX) | Interchangeable citation formats |
| **Repository** | `UserRepository`, `CourseRepository`, … | Persistence abstraction |
| **Query Builder** | `QueryBuilder<T,ID>` | Fluent, type-safe in-memory queries |

---

## Class Hierarchy

```
User (abstract)
├── Admin
├── Student                        implements ResearcherCapable, BookBorrowerCapable
│   └── GraduateStudent
└── Employee (abstract)
    ├── Teacher                    implements ResearcherCapable, BookBorrowerCapable
    │   └── Dean
    ├── Manager
    ├── Librarian
    ├── TechSupport
    └── EmployeeResearcher         implements ResearcherCapable
```

---

## UML Diagrams

| File | Description |
|------|-------------|
| `src/class_diagram.mermaid` | Full class diagram (4 focused views) |
| `usecase_diagram.mermaid` | Use-case diagram |
| `usecase_diagram_coupled.mermaid` | Detailed coupled variant |
| `ClassDiagram1.png` | Rendered class diagram image |
| `Use_Case_Diagram.png` | Rendered use-case diagram image |
| `UseCaseDiagram1.svg` | SVG use-case diagram |
| `OOP_Final_Project.pdf` | Requirements specification |

---

## Requirements

- **Java 17+** (uses records, switch expressions, sealed classes, text blocks)
- No external dependencies — pure Java SE
