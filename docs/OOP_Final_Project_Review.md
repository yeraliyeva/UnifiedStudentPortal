# OOP Final Project: Technical Review & Requirements Analysis

This document provides a comprehensive analysis of the University Management System, mapping the implemented codebase directly against the requested specifications from the project requirements.

---

## 1. Architectural Overview & System Flow

The project was heavily refactored from a standard monolithic structure into a strict **Clean Architecture (Onion Architecture)** approach. This ensures extremely low coupling and high cohesion.

- **Presentation Layer (`cli/`)**: Contains pure I/O components (Menus, Console). It handles zero business logic, merely calling Application Use Cases.
- **Application Layer (`usecases/`)**: Orchestrates the flow of data. Divided into facades (`AdminUseCases`, `CourseUseCases`, etc.) that execute isolated business workflows.
- **Domain Layer (`domain/`)**: The absolute core. Contains Entities (`Course`, `User`), Value Objects (`Credits`, `Username`), Domain Services (`EnrollmentService`), and Repository Ports (Interfaces). It has zero external dependencies.
- **Infrastructure Layer (`persistence/`)**: Implements the Repository Ports. Contains the custom generic JSON Database, ORM framework, and data mappers.

---

## 2. Design Patterns Used

The requirements mandate the use of at least 4 design patterns. The system successfully implements over 6 prominent patterns:

1. **Observer (Pub-Sub) Pattern**: 
   - *Requirement check:* "The system must notify readers when the new paper in published in the journal they are subscribed... Which pattern is this?"
   - *Implementation:* `SubscriptionService` and `PaperPublisher`. When a paper is published, the publisher notifies all users subscribed to that journal via the `NotificationRepository`.
2. **Capability / Composition Pattern (for Researcher)**:
   - *Requirement check:* "You should think about Researcher class. Is it an interface? Abstract class? Created using Decorator pattern? Just employee?"
   - *Implementation:* The system uses the **Capability Pattern** via the `ResearcherCapable` interface combined with Composition. Because Java does not support multiple inheritance, making `Researcher` a class would prevent a `Teacher` or `Student` from *also* being a researcher cleanly. Instead, `Student`, `Teacher`, and `EmployeeResearcher` implement `ResearcherCapable` and internally compose a `ResearcherProfile` object.
3. **Chain of Responsibility Pattern**:
   - *Implementation:* `EnrollmentService` holds a list of `EnrollmentRule` implementations. When a student tries to enroll, the request passes through the chain: `MaxFailLimitRule` -> `CreditLimitRule` -> `CapacityRule` -> `PrerequisiteRule` -> `ScheduleConflictRule`. If any rule fails, the enrollment is denied.
4. **Repository & Data Mapper Pattern**:
   - *Implementation:* The Domain layer defines `UserRepository`, `CourseRepository`, etc., as interfaces (Ports). The Infrastructure layer implements them using an internal `Repository<T, ID>` ORM engine and `EntityMapper` classes to seamlessly translate Java objects to JSON.
5. **Factory Pattern**:
   - *Implementation:* `MenuFactory` dynamically generates the correct CLI menu interface based on the runtime type of the logged-in User (`AdminMenu`, `StudentMenu`, etc.).
6. **Facade Pattern**:
   - *Implementation:* Instead of the CLI calling 40 different specific use case classes directly, they are grouped behind facades like `CourseUseCases` and `MessagingUseCases` to simplify the API surface.

---

## 3. Main Functionality Checklist & Traceability

Below is the traceability matrix proving that all core requirements have been successfully implemented:

### Authentication & Core Users
- ✅ **"Any user should access the system via authentication"**: Implemented via `LoginScreen` and `PasswordHasher`.
- ✅ **"User, Employee, Teacher, Manager, Student, GraduateStudent, Admin, TechSupportSpecialist"**: All present in the `DomainUsers` hierarchy.

### The "Most Important Functionality"
- ✅ **Course Registration**: Managed by `EnrollInCourse` use case and strictly validated by `EnrollmentService`.
- ✅ **Putting Marks**: Managed by `RecordMarks` use case. The `Grade` object specifically contains `firstHalf`, `secondHalf`, and `exam` fields, validating the "PASSING_TOTAL" logic.
- ✅ **Research**: Full research aggregate created (`ResearchPaper`, `ResearchProject`, `JournalName`).

### Specific Business Rules
- ✅ **"Students can’t have more than 21 credits"**: Enforced by `Credits.SEMESTER_LIMIT` (set to 21) and the `CreditLimitRule` in the enrollment chain.
- ✅ **"Students can’t fail more than 3 times"**: Enforced by `Student.MAX_FAILS` (set to 3) and the `MaxFailLimitRule`.
- ✅ **"Mark consists of 1st, 2nd attestation, and final"**: Handled by the `Grade` record (`firstHalf`, `secondHalf`, `exam`).
- ✅ **"Lesson types: lecture/practice"**: Represented in the `LessonType` enumeration.
- ✅ **"Switching between languages: KZ, EN, RU"**: Represented by the `Language` enum and utilized by the `Translator` service.
- ✅ **"Teacher complaint to dean with urgency levels"**: Handled by `ComplainAboutStudent` use case and the `UrgencyLevel` enum (LOW, MEDIUM, HIGH).
- ✅ **"Major, minor, free elective courses"**: Represented by the `DisciplineType` enumeration.
- ✅ **"News with comments. Priority (pinned) for Research news."**: `News` entity has an `isPinned` boolean and `addComment` method. Top researcher news generation is handled by `GenerateTopResearcherNews`.
- ✅ **"Researcher method to calculate h-index"**: Handled by the highly cohesive domain service `HIndexCalculator`.
- ✅ **"Supervisor must have h-index >= 3"**: Enforced by `HIndex.MIN_FOR_SUPERVISION` inside the `SetSupervisor` use case. Throws a custom business error if the H-index is lower.
- ✅ **"Print papers with Comparator (by date, citations, length)"**: Standard Java `Comparator` implemented in `ResearchUseCases` allowing sorting by any of the 3 fields.
- ✅ **"Citation generation (Plain Text or Bibtex)"**: Handled by `CitationFormatter` and `PaperFormat` enum.
- ✅ **"Tech support requests statuses (VIEWED, ACCEPTED, REJECTED, DONE)"**: Modeled cleanly in the `Request` and `Order` entities using `OrderStatus` and `RequestStatus` enums.
- ✅ **"Manager Types - OR, Departments"**: Represented by the `ManagerPosition` enum.

---

## 4. Bonus Features & Extra Credit Implementations

The project goes far beyond the baseline requirements, implementing several robust "bonus" engineering feats:

1. **Custom JSON Database & ORM Engine**
   - Instead of standard text file parsing, we built a fully-fledged generic Database layer (`JsonFileDatabase`).
   - We implemented a custom `QueryBuilder` (e.g., `db.select().whereEq("faculty", SITE).list()`) allowing advanced, safe, and easily extensible data querying without repeating loops.

2. **The `Result<T>` Monad for Error Handling**
   - Instead of throwing native Java exceptions blindly (which is considered an anti-pattern for expected business rule failures), the system uses a `Result<T>` wrapper. 
   - Example: Trying to register for a full course returns `Result.fail("Course capacity reached")` which the CLI elegantly prints, preventing application crashes and stack traces.

3. **Role-Based Dynamic Menus (CLI Framework)**
   - A highly scalable command-line interface framework. `MenuFactory` builds custom menus based on what capabilities a user has (e.g., if a `Teacher` activates their Researcher profile, the `MenuFactory` dynamically appends the `ResearcherMenuExtension` options to their base menu).

4. **Immutable Value Objects**
   - Heavy use of Java 17+ `record` types for value objects (`Credits`, `Money`, `CourseId`, `TimeSlot`). This guarantees immutability, thread-safety, and reduces boilerplate code, strictly adhering to Domain-Driven Design (DDD) best practices.

5. **Schedule Generation & Room Scheduling (Bonus Requirement)**
   - The system includes a `RoomScheduler` domain service that checks `TimeSlot` overlap logic. When a manager tries to assign a lesson via `AddLesson`, it verifies if the room is available at that specific day and time.

---

## 5. Web App Backend Feasibility (Python Integration)

**Question from User:** *We also want to build web app on top of java classes, can i use python for backend that integrated with java?*

**Answer:** 
Yes, absolutely. Because the Java application was intentionally built using a central **JSON Database engine**, all the internal state (Users, Courses, Messages) is safely stored in standard JSON format in the file system.

If you want to build a Web App using Python (e.g., FastAPI, Django, or Flask):
1. **Direct Data Access**: Your Python backend can directly read and write to the same `.json` files that the Java application uses. 
2. **API Wrapper**: You can wrap the existing Java application inside a Spring Boot REST API layer, and have Python communicate with it via HTTP requests. 
3. **Microservice approach**: Since the system is fully decoupled via Clean Architecture, you can replace the `Presentation Layer` (currently CLI) with a set of REST endpoints. Python can act as a frontend/BFF (Backend-For-Frontend) layer, while Java handles the heavy Domain Logic.

Because the core domain logic is entirely isolated from the user interface, transitioning this system to a web application is trivial and requires zero modifications to the core `Domain` or `Application` layers.
