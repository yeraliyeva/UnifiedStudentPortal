# University Management System — Full Technical Analysis

---

## 1. Project Overview

The University Management System is a **pure Java 17+ application** built with zero external dependencies. It implements a complete university lifecycle — authentication, enrollment, grading, research, library, messaging, IT support, and administration — across **239 Java files** organized in a strict four-layer Clean Architecture.

The project was extended with a fully decoupled **REST API backend** added on top of the existing domain logic without modifying a single core class. Both the CLI and REST layers run from the same entry point (`Main.java`) and share the same `AppContext` — the same repositories, use cases, and domain services.

**Stats at a glance:**
| Metric | Count |
|--------|-------|
| Java source files | 239 |
| Domain entities & VOs | ~35 |
| Use cases | 46 |
| Domain services | 7 |
| Enrollment validation rules | 6 |
| REST API endpoints | 36 |
| Test cases (no framework) | 52 |
| Design patterns implemented | 8+ |

---

## 2. Architecture

### 2.1 Clean Architecture (Onion Model)

The dependency rule is strict and one-directional:

```
presentation  →  application  →  domain  ←  infrastructure
```

- **Domain** has zero external imports. It owns entities, value objects, repository interfaces (ports), and domain services.
- **Application** orchestrates use cases. It calls domain objects and repository ports. It never touches I/O or persistence.
- **Infrastructure** implements the repository ports using JSON file storage and the custom ORM. It depends on domain interfaces, not vice versa.
- **Presentation** contains both the CLI menus and the REST controllers. Each delegates entirely to application use cases.

This means the domain is completely portable — it could run with a different database, CLI, or web framework with zero changes to core logic.

### 2.2 Dual-Mode Entry Point

```java
// Main.java
if (args.length > 0 && args[0].equals("--server")) {
    int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
    RestServer server = new RestServer(ctx, port);
    server.start();
    // ...
} else {
    runCli(ctx);
}
```

Both modes share the same `AppContext` object. The REST server and CLI are two different presentation adapters over identical business logic.

### 2.3 Dependency Injection via AppContext

`AppContext` is a manual DI container — all repositories, services, and use cases are constructed once and wired together in a single place. This avoids reflection-based frameworks while providing the same benefits.

```java
// AppContext.java — excerpt showing layered wiring
this.enrollmentService = new EnrollmentService(List.of(
    new MaxFailLimitRule(),
    new AlreadyEnrolledRule(),
    new CreditLimitRule(),
    new PrerequisiteRule(courseRepository),
    new CapacityRule(),
    new ScheduleConflictRule(courseRepository)
));
```

---

## 3. Design Patterns

### 3.1 Chain of Responsibility — Enrollment Validation

The most architecturally significant pattern. Six rules form an ordered chain. Each rule is an independent, testable unit that either permits or denies enrollment.

```java
// EnrollmentRule.java — the contract
public interface EnrollmentRule {
    EnrollmentDecision check(Student student, Course course);
}

// EnrollmentService.java — the chain executor
public EnrollmentDecision tryEnroll(Student student, Course course) {
    for (EnrollmentRule rule : rules) {
        EnrollmentDecision decision = rule.check(student, course);
        if (!decision.allowed()) return decision;
    }
    return EnrollmentDecision.allow();
}
```

**Chain order:** `MaxFailLimitRule` → `AlreadyEnrolledRule` → `CreditLimitRule` → `PrerequisiteRule` → `CapacityRule` → `ScheduleConflictRule`

Business rules enforced:
- Student may not have failed more than 3 courses (`Student.MAX_FAILS = 3`)
- Student may not re-enroll in a course they are already in
- Semester credit load may not exceed 21 credits (`Credits.SEMESTER_LIMIT = 21`)
- Prerequisites must be completed first
- Course must have remaining seats
- New lesson must not cause a schedule conflict in the same room

### 3.2 Observer — Journal Subscription Notifications

`PaperPublisher` acts as the publisher. When a paper is published to a journal, it fans out notifications to all users who have subscribed to that journal.

```java
// PaperPublisher.java
private void notifySubscribers(ResearchProject project, ResearchPaper paper) {
    String text = "[JOURNAL] New paper in '" + project.journal() + "': " + paper.title();
    for (User u : users.findAll()) {
        if (u instanceof ResearcherCapable rc && rc.isResearcher()
                && rc.researcherProfile().isSubscribedTo(project.journal().value())) {
            notifications.save(Notification.of(u.username(), text));
        }
    }
}
```

### 3.3 Strategy — Citation Formatting

`CitationFormatter` uses a switch expression to select between two interchangeable formatting algorithms (`PLAIN_TEXT`, `BIBTEX`) at runtime.

```java
// CitationFormatter.java
public String format(ResearchPaper paper, PaperFormat format) {
    return switch (format) {
        case BIBTEX     -> bibtex(paper);
        case PLAIN_TEXT -> plain(paper);
    };
}
```

Adding a new format (e.g., `APA`, `MLA`) requires only adding a new `PaperFormat` enum constant and a new branch — zero changes to callers.

### 3.4 Factory Method — Dynamic Role Menus

`DefaultMenuFactory.menuFor(user)` inspects the runtime type of the logged-in user and constructs the appropriate menu. If the user has activated the researcher capability, the researcher menu extension is dynamically appended.

### 3.5 Repository Pattern — Persistence Ports

Every aggregate has a corresponding interface in `domain.repository`. Infrastructure implements them. Domain never touches file I/O.

```
domain.repository.UserRepository         (interface / port)
    ←  infrastructure.persistence.orm.repository.OrmUserRepository  (implementation)
    ←  infrastructure.persistence.inmemory.InMemoryUserRepository   (test double)
```

The in-memory implementations allow unit tests to run in milliseconds without touching the filesystem.

### 3.6 Capability / Interface Composition — Researcher Role

Java's single-inheritance limitation means a `Teacher` cannot extend both `Employee` and `Researcher`. The solution is the `ResearcherCapable` interface combined with internal composition via `ResearcherProfile`.

```java
// ResearcherCapable.java
public interface ResearcherCapable {
    boolean isResearcher();
    void activateResearcher(String field);
    ResearcherProfile researcherProfile();
}
```

`Student`, `Teacher`, and `EmployeeResearcher` all implement this interface and compose a `ResearcherProfile` object. A user becomes a researcher at runtime by calling `activateResearcher()` — this is the `BecomeResearcher` use case.

### 3.7 Strategy (REST Router) — Route Dispatching

The `Router` in the REST layer maintains a list of `Route` objects. Each `Route` stores the pattern, method, handler (as a method reference), and required role. The router iterates and delegates — adding new endpoints is one `router.register(...)` call.

```java
router.register(Route.of(HttpMethod.POST, "/api/courses/{id}/enroll", courses::enroll, Student.class));
```

### 3.8 Query Builder Pattern — Custom ORM

The infrastructure layer provides a fluent, type-safe `QueryBuilder<T, ID>` that works on top of the JSON file database.

```java
// Usage example in a repository
db.select().whereEq("faculty", "SITE").orderBy(Comparator.comparing(...)).limit(10).list()
```

Supports: `whereEq`, `whereContains`, `whereGt`, `whereLt`, `whereMatch`, `orderBy`, `orderByDesc`, `limit`, `offset`, `first`, `count`, `exists`, `deleteAll`, `updateEach`.

---

## 4. SOLID & GRASP Principles

### SOLID

| Principle | Where Applied |
|-----------|--------------|
| **SRP** | Each use case class has exactly one method (`execute()`). Each domain service has a single responsibility. `SecurityFilter` handles only auth — routing is in `Router`. |
| **OCP** | Adding a new enrollment rule: implement `EnrollmentRule`, add to the list in `AppContext`. Adding a REST endpoint: one `router.register(...)` line. No existing class changes. |
| **LSP** | All `User` subtypes can be used anywhere a `User` is expected. `GraduateStudent` extends `Student` without breaking any contract. |
| **ISP** | `BookBorrowerCapable` and `ResearcherCapable` are narrow interfaces. Types implement only what they actually support. |
| **DIP** | Domain defines `UserRepository` as an interface. `AppContext` injects `OrmUserRepository`. No domain class ever mentions `OrmUserRepository`. |

### GRASP

| Principle | Implementation |
|-----------|---------------|
| **Information Expert** | `EnrollmentService` owns enrollment logic; `GpaCalculator` owns GPA logic; `HIndexCalculator` owns H-index logic. |
| **Controller** | Each REST controller class (`AdminController`, `CourseController`) is a thin façade delegating to use cases — no business logic inside controllers. |
| **Creator** | `AppContext` is the sole creator of all use case instances. |
| **Low Coupling** | Domain has zero dependencies on any other layer. Use cases depend only on repository interfaces. |
| **High Cohesion** | Each class/package has a clear single focus. `domain.rules` contains only enrollment rule logic. `domain.shared` contains only value objects. |
| **Pure Fabrication** | `CitationFormatter`, `HIndexCalculator`, `GpaCalculator`, `RoomScheduler` are domain services that do not represent real-world entities — they exist purely to assign behavior to the right place. |

---

## 5. Key Domain Concepts

### 5.1 Value Objects (Java Records)

All domain primitives are modeled as immutable value objects using Java 17+ records. They enforce their own invariants in compact constructors.

```java
// Grade.java
public record Grade(int firstHalf, int secondHalf, int exam) {
    public static final int PASSING_TOTAL = 50;
    public Grade {
        if (firstHalf  < 0 || firstHalf  > 30) throw new IllegalArgumentException("att1 0..30");
        if (secondHalf < 0 || secondHalf > 30) throw new IllegalArgumentException("att2 0..30");
        if (exam       < 0 || exam       > 40) throw new IllegalArgumentException("exam 0..40");
    }
    public int total()       { return firstHalf + secondHalf + exam; }
    public boolean isPassing() { return total() >= PASSING_TOTAL; }
    public String letter()   { /* A/B/C/D/F */ }
}
```

```java
// Credits.java
public record Credits(int value) {
    public static final Credits SEMESTER_LIMIT = new Credits(21);
    // supports plus(), minus(), covers()
}
```

Other value objects: `Username`, `PersonName`, `Email`, `Money`, `CourseId`, `BookId`, `JournalName`, `HIndex`, `PaperId`, `TimeSlot`, `Room`.

### 5.2 Result Monad — Error Handling Without Exceptions

All use cases return `Result` instead of throwing for expected business failures. This keeps the call stack clean and forces callers to explicitly handle the failure case.

```java
// Result.java
public record Result(boolean success, String message) {
    public static Result ok()               { return new Result(true, ""); }
    public static Result ok(String message) { return new Result(true, message); }
    public static Result fail(String message){ return new Result(false, message); }
}
```

A `Result.fail("Course is full")` propagates cleanly through the REST controller as a `400 Bad Request` and through the CLI as a printed error message.

### 5.3 User Hierarchy

```
User (abstract)
├── Admin
├── Student                        implements ResearcherCapable, BookBorrowerCapable
│   └── GraduateStudent
└── Employee (abstract)
    ├── Teacher                    implements ResearcherCapable, BookBorrowerCapable
    │   └── Dean
    ├── Manager                    (position: OR | DEPARTMENT_HEAD)
    ├── Librarian
    ├── TechSupport
    └── EmployeeResearcher         implements ResearcherCapable
```

### 5.4 Enumerations (15 total)

`DegreeType`, `DisciplineType`, `Faculty`, `Gender`, `HelpType`, `Language`, `LessonType`, `ManagerPosition`, `MessageStatus`, `OrderStatus`, `PaperFormat`, `RequestStatus`, `TeacherPosition`, `UrgencyLevel`, `WeekDay`

These model business concepts as explicit types rather than magic strings.

---

## 6. Infrastructure Layer

### 6.1 Custom JSON Engine

Zero external libraries. Four hand-rolled classes handle the entire serialization pipeline:

- `JsonValue` — sealed interface with record subtypes (`JsonString`, `JsonNumber`, `JsonBool`, `JsonArray`, `JsonObject`, `JsonNull`)
- `JsonReader` — recursive-descent parser
- `JsonWriter` — serializer
- `JsonObjectBuilder` — fluent builder for constructing JSON objects

```java
// JsonValue.java — sealed type hierarchy
public sealed interface JsonValue {
    record JsonString(String value)      implements JsonValue {}
    record JsonNumber(double value)      implements JsonValue {}
    record JsonBool(boolean value)       implements JsonValue {}
    record JsonArray(List<JsonValue>)    implements JsonValue {}
    record JsonObject(Map<String,JsonValue>) implements JsonValue {}
    final class JsonNull implements JsonValue { ... }
}
```

### 6.2 Custom ORM & QueryBuilder

`Repository<T, ID>` is a generic base that wraps `JsonFileDatabase` and exposes a `select()` method returning a `QueryBuilder<T, ID>`. Each aggregate has a corresponding `EntityMapper<T, ID>` that converts between Java objects and JSON.

The ORM supports:
- Full CRUD (save, findById, findAll, delete)
- Fluent querying (`whereEq`, `whereContains`, `whereGt`, `limit`, `orderBy`)
- In-memory alternatives (same interface) used exclusively in tests

### 6.3 Persistence Dual Stack

| Mode | Implementation | Purpose |
|------|---------------|---------|
| JSON files | `OrmXxxRepository` | Production — data stored in `/data/*.json` |
| In-memory | `InMemoryXxxRepository` | Tests — zero I/O, always clean state |

The switch is done entirely in `AppContext.withJsonStorage(path)` vs directly passing an in-memory repository. Test code uses in-memory; `Main.java` uses JSON.

---

## 7. REST API Backend

### 7.1 Infrastructure

Built on Java SE's built-in `com.sun.net.httpserver.HttpServer`. Zero external HTTP framework dependencies.

**Components:**
| Class | Role |
|-------|------|
| `RestServer` | Bootstrap — wires all controllers and routes, starts the server |
| `Router` | Dispatches requests to matching `Route` handlers via linear scan |
| `Route` | Immutable record: `(HttpMethod, pattern, handler, requiredRole)` |
| `SecurityFilter` | Middleware — validates Bearer token, enforces role-based access |
| `TokenStore` | Thread-safe UUID session registry (`ConcurrentHashMap`) |
| `RequestContext` | `ThreadLocal<User>` — passes authenticated user to controllers |

### 7.2 Security Flow

```
Request → Router.handle()
          → SecurityFilter.guard()
              → extract "Authorization: Bearer {token}"
              → TokenStore.resolve(token) → Optional<Username>
              → UserRepository.findByUsername() → Optional<User>
              → route.requiredRole().isInstance(user) → allow / 403
              → RequestContext.set(user)
          → Controller.method(request)
          → RequestContext.clear()
          → HttpResponse.send()
```

RBAC is done entirely with `Class.isInstance()`. Adding a new role restriction is one argument in `Route.of(...)`. No annotations, no reflection magic.

### 7.3 Route Catalogue (36 endpoints)

| Method | Path | Role |
|--------|------|------|
| POST | `/api/login` | Public |
| POST | `/api/logout` | Public |
| GET | `/api/users` | Admin |
| GET | `/api/users/{username}` | Admin |
| POST | `/api/users/students` | Admin |
| DELETE | `/api/users/{username}` | Admin |
| GET | `/api/logs` | Admin |
| GET | `/api/reports/academic` | Admin |
| GET | `/api/courses` | User |
| GET | `/api/courses/{id}` | User |
| POST | `/api/courses` | Manager |
| POST | `/api/courses/{id}/enroll` | Student |
| POST | `/api/courses/{id}/drop` | Student |
| POST | `/api/courses/{id}/marks` | Teacher |
| GET | `/api/courses/{id}/grades` | Teacher |
| GET | `/api/transcript` | Student |
| GET | `/api/books` | User |
| POST | `/api/books` | Librarian |
| DELETE | `/api/books/{title}` | Librarian |
| POST | `/api/books/{title}/borrow` | User |
| POST | `/api/books/{title}/return` | User |
| GET | `/api/messages/inbox` | User |
| POST | `/api/messages` | User |
| GET | `/api/news` | User |
| POST | `/api/news` | Employee |
| POST | `/api/news/{id}/comment` | User |
| GET | `/api/requests` | User |
| POST | `/api/requests` | User |
| GET | `/api/orders` | TechSupport |
| POST | `/api/orders` | User |
| PUT | `/api/orders/{id}/accept` | TechSupport |
| PUT | `/api/orders/{id}/complete` | TechSupport |
| GET | `/api/papers` | User |
| POST | `/api/papers` | User |
| GET | `/api/papers/{id}/cite` | User |
| GET | `/api/projects` | User |
| POST | `/api/projects` | User |
| POST | `/api/projects/{journal}/join` | User |
| POST | `/api/subscriptions` | User |
| DELETE | `/api/subscriptions/{journal}` | User |

### 7.4 Concurrency

```java
server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
```

Running on Java 21+, the server uses virtual threads — each request gets its own lightweight thread with near-zero overhead. `RequestContext` uses `ThreadLocal<User>` ensuring per-request isolation with no shared mutable state.

### 7.5 Adapter Pattern — Domain-to-JSON Serializers

`UserSerializer` and `CourseSerializer` translate domain objects to API responses without polluting domain classes with JSON logic.

```java
// UserSerializer.java — type-safe dispatch with pattern matching
public static JsonValue toJson(User user) {
    JsonObjectBuilder builder = JsonObjectBuilder.create()
        .put("username", user.username().value())
        .put("role",     user.getClass().getSimpleName());
    if (user instanceof Student s) {
        builder.put("degreeType", s.degreeType().name())
               .put("studyYear",  s.studyYear());
    }
    // ... Teacher, Employee, etc.
    return builder.build();
}
```

---

## 8. Requirements Coverage

### 8.1 Core Requirements ✅

| Requirement | Implementation |
|-------------|---------------|
| Authentication for all users | `LoginScreen` + `AuthenticationService` + `PlainPasswordHasher` |
| 9 user types | `Admin`, `Student`, `GraduateStudent`, `Teacher`, `Dean`, `Manager`, `Librarian`, `TechSupport`, `EmployeeResearcher` |
| Course registration | `EnrollInCourse` use case → `EnrollmentService` chain |
| Grading (1st/2nd attestation + exam) | `Grade` record with `firstHalf`, `secondHalf`, `exam` |
| Max 21 credits per semester | `CreditLimitRule` + `Credits.SEMESTER_LIMIT = 21` |
| Max 3 course failures | `MaxFailLimitRule` + `Student.MAX_FAILS = 3` |
| Schedule conflict detection | `ScheduleConflictRule` + `RoomScheduler` |
| Research papers with H-index | `ResearchPaper` + `HIndexCalculator` |
| Supervisor requires H-index ≥ 3 | `SetSupervisor` use case + `HIndex.MIN_FOR_SUPERVISION = 3` |
| Citations in PLAIN_TEXT and BIBTEX | `CitationFormatter` + `PaperFormat` enum |
| Sorting papers by date/citations/length | Standard `Comparator` applied via `QueryBuilder.orderBy()` |
| Journal subscriptions + notifications | `SubscriptionService` + `PaperPublisher` (Observer) |
| News with comments and pinning | `News` entity with `isPinned` + `addComment()` |
| Tech support request statuses | `RequestStatus` (`VIEWED`, `ACCEPTED`, `REJECTED`, `DONE`) |
| IT orders statuses | `OrderStatus` (`PENDING`, `ACCEPTED`, `DONE`) |
| Message urgency levels | `UrgencyLevel` (`LOW`, `MEDIUM`, `HIGH`) |
| Multilingual support (KZ/EN/RU) | `Language` enum + `PropertiesTranslator` + `.properties` files |
| Teacher complaints to Dean | `ComplainAboutStudent` use case |
| Lesson types (lecture/practice) | `LessonType` enum |
| Discipline types | `DisciplineType` enum (`MAJOR`, `MINOR`, `FREE`) |
| Manager positions | `ManagerPosition` enum (`OR`, `DEPARTMENT_HEAD`) |
| Teacher positions | `TeacherPosition` enum (`PROFESSOR`, `LECTOR`, `SENIOR_LECTOR`, `ASSISTANT`) |
| Teacher rating | `RateTeacher` use case |

### 8.2 Bonus Features ✅

| Bonus | Implementation |
|-------|---------------|
| Custom JSON engine | `JsonValue` sealed type + `JsonReader` + `JsonWriter` (no deps) |
| Custom ORM with QueryBuilder | `Repository<T,ID>` + `QueryBuilder<T,ID>` + `EntityMapper` |
| Result monad for error handling | `Result` record (used by all 46 use cases) |
| Dual-mode launcher (CLI + REST) | `Main.java` with `--server` flag |
| Full REST API (36 endpoints) | `RestServer`, `Router`, 6 controllers |
| Stateless auth (Bearer token / RBAC) | `SecurityFilter`, `TokenStore`, `RequestContext` |
| Virtual thread concurrency | `Executors.newVirtualThreadPerTaskExecutor()` |
| Room scheduling | `RoomScheduler` + `TimeSlot` + `Room` value objects |
| 52 unit + integration tests | Custom `TestRunner` + `Assert` — zero external frameworks |
| Academic report generation | `GenerateAcademicReport` use case |
| Dynamic researcher menus | `ResearcherMenuExtension` injected by `DefaultMenuFactory` |
| Dual persistence stack | `OrmXxxRepository` (production) + `InMemoryXxxRepository` (tests) |
| Student organizations | `Organization` entity + `CreateOrganization` / `JoinOrganization` |
| Audit logging | `RepositoryLogger` writes every mutating operation to `LogRepository` |

---

## 9. Testing

52 tests with zero external test framework (no JUnit, no TestNG). The project ships its own `Assert` utility and `TestRunner`.

| Test Class | What It Covers |
|------------|---------------|
| `AuthenticationTest` | Login success/failure, bad credentials |
| `EnrollmentRulesTest` | Each of the 6 rules in isolation |
| `EnrollmentServiceTest` | Full chain execution |
| `RecordMarksTest` | Grade validation, letter assignment |
| `BecomeResearcherTest` | Researcher capability activation |
| `ResearcherSupportTest` | H-index, supervision constraint, subscriptions |
| `PaperPublisherTest` | Observer notification delivery |
| `RoomSchedulerTest` | Schedule conflict detection |
| `QueryBuilderTest` | ORM filtering, ordering, pagination |
| `JsonPersistenceTest` | Round-trip JSON read/write |
| `NewUseCasesTest` | IT orders, organizations, rating, complaints |

In-memory repositories are used exclusively, so tests never touch the filesystem.

---

## 10. Project Structure (Final)

```
university-system/
├── src/                        Java source (239 files)
│   ├── Main.java               Entry point — CLI or REST mode
│   ├── domain/                 Entities, VOs, ports, services, rules
│   ├── application/            Use cases (46) + Result monad
│   ├── infrastructure/         JSON engine, ORM, mappers, auth, i18n
│   ├── presentation/
│   │   ├── cli/                Console menus, login screen
│   │   └── rest/               HTTP server, router, controllers, security
│   ├── bootstrap/              AppContext DI container, DataSeeder
│   ├── resources/              i18n property files (en/kz/ru)
│   └── test/                   52 tests, custom runner
├── scripts/
│   ├── build.sh                Compile all sources to out/
│   ├── test.sh                 Run the test suite
│   └── plantuml.jar            Diagram renderer
├── docs/
│   ├── OOP_Final_Project.pdf   Requirements specification
│   ├── OOP_Final_Project_Review.md  Requirements traceability
│   └── PROJECT_ANALYSIS.md    ← this file
├── diagrams/
│   ├── class_diagram.puml      Full PlantUML class diagram
│   ├── class_diagram_compact.puml  Compact version (~50 elements)
│   ├── class_diagram_overview.puml Overview version
│   ├── usecase_diagram.mermaid Use-case diagram
│   └── *.png / *.svg           Rendered diagram images
└── data/                       JSON data files (gitignored in prod)
```
