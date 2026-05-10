# University Management System — OOP Final Project

A console-based university management system built in **Java 17+** as an OOP final project. The system models the full lifecycle of a university: user management, course registration, grading, research, communication, and administrative workflows.

---

## Quick Start

```bash
# Build
chmod +x build.sh
./build.sh

# Run
java -cp out/main Main

# Run the automated test suite (52 tests)
chmod +x test.sh
./test.sh
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


```mermaid

classDiagram
    direction TB

    %% ── PRESENTATION ────────────────────────────────────────
    namespace Presentation {
        class Console {
            <<interface>>
            +println(String)
            +print(String)
            +readLine(String) String
            +readInt(String) int
        }
        class StdConsole {
            +println(String)
            +print(String)
            +readLine(String) String
            +readInt(String) int
        }
        class LoginScreen {
            -users UserRepository
            -hasher PasswordHasher
            -console Console
            +run() Optional~User~
        }
        class Menu {
            <<interface>>
            +run()
        }
        class MenuItem {
            +label String
            +action Runnable
        }
        class MenuFactory {
            <<interface>>
            +menuFor(User) Menu
        }
        class BecomeResearcherAction {
            -ctx AppContext
            +run(ResearcherCapable)
        }
        class ResearcherMenuExtension {
            -ctx AppContext
            +items(ResearcherCapable) List~MenuItem~
        }
        class AdminMenu {
            -ctx AppContext
            +run()
        }
        class StudentMenu {
            -ctx AppContext
            +run()
        }
        class GraduateStudentMenu {
            -ctx AppContext
            +run()
        }
        class TeacherMenu {
            -ctx AppContext
            +run()
        }
        class DeanMenu {
            -ctx AppContext
            +run()
        }
        class ManagerMenu {
            -ctx AppContext
            +run()
        }
        class LibrarianMenu {
            -ctx AppContext
            +run()
        }
        class TechSupportMenu {
            -ctx AppContext
            +run()
        }
        class EmployeeResearcherMenu {
            -ctx AppContext
            +run()
        }
    }

    %% ── BOOTSTRAP ───────────────────────────────────────────
    namespace Bootstrap {
        class AppContext {
            +console Console
            +loginScreen LoginScreen
            +menuFactory MenuFactory
            +users UserRepository
            +courses CourseRepository
            +books BookRepository
            +messages MessageRepository
            +news NewsRepository
            +requests RequestRepository
            +orders OrderRepository
            +papers ResearchPaperRepository
            +projects ResearchProjectRepository
            +organizations OrganizationRepository
            +notifications NotificationRepository
            +logs LogRepository
            +enrollmentService EnrollmentService
            +paperPublisher PaperPublisher
            +gpaCalculator GpaCalculator
            +hIndexCalculator HIndexCalculator
            +citationFormatter CitationFormatter
            +roomScheduler RoomScheduler
            +subscriptionService SubscriptionService
            +withJsonStorage(Path)$ AppContext
        }
        class DataSeeder {
            -ctx AppContext
            +seedIfEmpty()
        }
        class DefaultMenuFactory {
            -ctx AppContext
            +menuFor(User) Menu
        }
        class IdSequenceWarmup {
            +warm(AppContext)$
        }
    }

    %% ── APPLICATION USE CASES ───────────────────────────────
    namespace AppAdmin {
        class CreateStudent {
            -users UserRepository
            -log LogRepository
            +execute(Username,String,PersonName,Gender,LocalDate,Email,Faculty,DegreeType,int) Result~Student~
        }
        class CreateTeacher {
            -users UserRepository
            -log LogRepository
            +execute(Username,String,PersonName,Gender,LocalDate,Email,Faculty,Money,LocalDate,String,String,TeacherPosition) Result~Teacher~
        }
        class CreateGradStudent {
            -users UserRepository
            -log LogRepository
            +execute(Username,String,PersonName,Gender,LocalDate,Email,Faculty,DegreeType,int) Result~GraduateStudent~
        }
        class CreateDean {
            -users UserRepository
            -log LogRepository
            +execute(Username,String,PersonName,Gender,LocalDate,Email,Faculty,Money,LocalDate,String,String) Result~Dean~
        }
        class CreateManager {
            -users UserRepository
            -log LogRepository
            +execute(Username,String,PersonName,Gender,LocalDate,Email,Faculty,Money,LocalDate,String,ManagerPosition) Result~Manager~
        }
        class CreateLibrarian {
            -users UserRepository
            -log LogRepository
            +execute(Username,String,PersonName,Gender,LocalDate,Email,Faculty,Money,LocalDate,String) Result~Librarian~
        }
        class CreateTechSupport {
            -users UserRepository
            -log LogRepository
            +execute(Username,String,PersonName,Gender,LocalDate,Email,Faculty,Money,LocalDate,String) Result~TechSupport~
        }
        class CreateEmpResearcher {
            -users UserRepository
            -log LogRepository
            +execute(Username,String,PersonName,Gender,LocalDate,Email,Faculty,Money,LocalDate,String,String) Result~EmployeeResearcher~
        }
        class DeleteUser {
            -users UserRepository
            -log LogRepository
            +execute(Username,Username) Result~Void~
        }
        class GenerateAcademicReport {
            -users UserRepository
            -courses CourseRepository
            -gpa GpaCalculator
            +execute() String
        }
        class GenerateTopResearcherNews {
            -users UserRepository
            -papers ResearchPaperRepository
            -news NewsRepository
            -hIndex HIndexCalculator
            +execute()
        }
    }

    namespace AppCourse {
        class EnrollInCourse {
            -users UserRepository
            -courses CourseRepository
            -enrollment EnrollmentService
            -log LogRepository
            +execute(Username,CourseId) Result~Void~
        }
        class DropCourse {
            -users UserRepository
            -courses CourseRepository
            -log LogRepository
            +execute(Username,CourseId) Result~Void~
        }
        class RecordMarks {
            -users UserRepository
            -courses CourseRepository
            -log LogRepository
            +execute(Username,CourseId,Username,int,int,int) Result~Void~
        }
        class AssignTeacher {
            -users UserRepository
            -courses CourseRepository
            -log LogRepository
            +execute(Username,CourseId) Result~Void~
        }
        class CreateCourse {
            -courses CourseRepository
            -sequence IdSequence
            -log LogRepository
            +execute(String,int,DisciplineType,int) Result~Course~
        }
        class SetCourseCapacity {
            -courses CourseRepository
            +execute(CourseId,int) Result~Void~
        }
        class SetCoursePrerequisite {
            -courses CourseRepository
            +execute(CourseId,CourseId) Result~Void~
        }
        class AddLesson {
            -courses CourseRepository
            -scheduler RoomScheduler
            +execute(CourseId,LessonType,WeekDay,String,String) Result~Void~
        }
        class ViewTranscript {
            -users UserRepository
            -courses CourseRepository
            -gpa GpaCalculator
            +execute(Username) String
        }
    }

    namespace AppLibrary {
        class BorrowBook {
            -books BookRepository
            -users UserRepository
            -log LogRepository
            +execute(Username,String) Result~Void~
        }
        class ReturnBook {
            -books BookRepository
            -log LogRepository
            +execute(Username,BookId) Result~Void~
        }
        class AddBook {
            -books BookRepository
            -sequence IdSequence
            +execute(String,String) Result~Book~
        }
        class RemoveBook {
            -books BookRepository
            +execute(BookId) Result~Void~
        }
    }

    namespace AppMessaging {
        class SendMessage {
            -messages MessageRepository
            -users UserRepository
            -sequence IdSequence
            +execute(Username,Username,String,String,UrgencyLevel) Result~Void~
        }
        class PublishNews {
            -news NewsRepository
            -sequence IdSequence
            -log LogRepository
            +execute(Username,String,String) Result~Void~
        }
        class CommentOnNews {
            -news NewsRepository
            +execute(int,String) Result~Void~
        }
        class SubmitRequest {
            -requests RequestRepository
            -sequence IdSequence
            +execute(Username,HelpType,Faculty,UrgencyLevel,String) Result~Void~
        }
        class ProcessRequest {
            -requests RequestRepository
            -users UserRepository
            +execute(int,RequestStatus) Result~Void~
        }
        class CreateITOrder {
            -orders OrderRepository
            -sequence IdSequence
            +execute(Username,String) Result~Void~
        }
        class AcceptOrder {
            -orders OrderRepository
            -log LogRepository
            +execute(Username,int) Result~Void~
        }
        class CompleteOrder {
            -orders OrderRepository
            -log LogRepository
            +execute(Username,int) Result~Void~
        }
    }

    namespace AppOrganization {
        class CreateOrganization {
            -organizations OrganizationRepository
            -users UserRepository
            -log LogRepository
            +execute(Username,String) Result~Organization~
        }
        class JoinOrganization {
            -organizations OrganizationRepository
            -log LogRepository
            +execute(Username,String) Result~Void~
        }
    }

    namespace AppResearch {
        class CreateResearchProject {
            -projects ResearchProjectRepository
            -users UserRepository
            -sequence IdSequence
            -log LogRepository
            +execute(Username,String,String) Result~ResearchProject~
        }
        class JoinResearchProject {
            -projects ResearchProjectRepository
            -users UserRepository
            +execute(Username,String) Result~Void~
        }
        class PublishPaper {
            -publisher PaperPublisher
            -users UserRepository
            -sequence IdSequence
            +execute(Username,String,String,int,String) Result~ResearchPaper~
        }
        class SubscribeToJournal {
            -subscription SubscriptionService
            -users UserRepository
            +execute(Username,String) Result~Void~
        }
        class UnsubscribeFromJournal {
            -subscription SubscriptionService
            -users UserRepository
            +execute(Username,String) Result~Void~
        }
        class GenerateCitation {
            -papers ResearchPaperRepository
            -formatter CitationFormatter
            +execute(PaperId,PaperFormat) Result~String~
        }
        class SetSupervisor {
            -users UserRepository
            -projects ResearchProjectRepository
            -hIndex HIndexCalculator
            -papers ResearchPaperRepository
            +execute(Username,Username) Result~Void~
        }
    }

    namespace AppUser {
        class BecomeResearcher {
            -users UserRepository
            -log LogRepository
            +execute(Username,String) Result~Void~
        }
        class RateTeacher {
            -users UserRepository
            -log LogRepository
            +execute(Username,Username,int) Result~Void~
        }
        class ComplainAboutStudent {
            -users UserRepository
            -messages MessageRepository
            -sequence IdSequence
            +execute(Username,Username,String) Result~Void~
        }
        class Result~T~ {
            -value T
            -error String
            -success boolean
            +ok(T)$ Result~T~
            +fail(String)$ Result~T~
            +isOk() boolean
            +value() T
            +error() String
        }
    }

    %% ── DOMAIN: CAPABILITIES ────────────────────────────────
    namespace DomainCapabilities {
        class ResearcherCapable {
            <<interface>>
            +isResearcher() boolean
            +activateResearcher(String)
            +researcherProfile() ResearcherProfile
        }
        class BookBorrowerCapable {
            <<interface>>
        }
        class PasswordHasher {
            <<interface>>
            +hash(String) String
            +matches(String,String) boolean
        }
    }

    %% ── DOMAIN: USERS ───────────────────────────────────────
    namespace DomainUsers {
        class User {
            <<abstract>>
            -username Username
            -passwordHash String
            -name PersonName
            -gender Gender
            -dateOfBirth LocalDate
            -email Email
            -faculty Faculty
            -language Language
            +username() Username
            +name() PersonName
            +email() Email
            +faculty() Faculty
            +language() Language
            +changePassword(String)
            +changeEmail(Email)
            +changeLanguage(Language)
            +matchesPassword(String) boolean
            +equals(Object) boolean
            +hashCode() int
        }
        class Employee {
            <<abstract>>
            -salary Money
            -hireDate LocalDate
            -insuranceNumber String
            +salary() Money
            +hireDate() LocalDate
            +insuranceNumber() String
        }
        class Admin {
        }
        class Student {
            -degreeType DegreeType
            -studyYear int
            -availableCredits Credits
            -failCount int
            -enrolled Set~CourseId~
            -completed Set~CourseId~
            -researcherProfile ResearcherProfile
            +MAX_FAILS$ int
            +degreeType() DegreeType
            +availableCredits() Credits
            +failCount() int
            +hasReachedFailLimit() boolean
            +enrolledCourses() Set~CourseId~
            +completedCourses() Set~CourseId~
            +recordEnrollment(CourseId,Credits)
            +recordDrop(CourseId,Credits)
            +recordCompletion(CourseId)
            +recordFail()
            +rehydrate(int,int,Iterable~CourseId~,Iterable~CourseId~)
            +isResearcher() boolean
            +activateResearcher(String)
            +researcherProfile() ResearcherProfile
        }
        class GraduateStudent {
            -supervisorUsername Username
            +supervisor() Optional~Username~
            +setSupervisor(Username)
        }
        class Teacher {
            -degree String
            -position TeacherPosition
            -taughtCourses Set~CourseId~
            -ratings List~Integer~
            -researcherProfile ResearcherProfile
            +degree() String
            +position() TeacherPosition
            +setPosition(TeacherPosition)
            +taughtCourses() Set~CourseId~
            +recordCourseAssignment(CourseId)
            +addRating(int)
            +ratings() List~Integer~
            +averageRating() double
            +rehydrate(Iterable~CourseId~,Iterable~Integer~)
            +isResearcher() boolean
            +activateResearcher(String)
            +researcherProfile() ResearcherProfile
        }
        class Dean {
        }
        class Manager {
            -position ManagerPosition
            +position() ManagerPosition
        }
        class Librarian {
        }
        class TechSupport {
        }
        class EmployeeResearcher {
            -defaultField String
            -researcherProfile ResearcherProfile
            +defaultField() String
            +isResearcher() boolean
            +activateResearcher(String)
            +researcherProfile() ResearcherProfile
        }
        class ResearcherProfile {
            -owner Username
            -field String
            -subscribedJournals Set~String~
            +owner() Username
            +field() String
            +subscribe(String)
            +unsubscribe(String)
            +subscribedJournals() Set~String~
            +isSubscribedTo(String) boolean
        }
    }

    %% ── DOMAIN: COURSE AGGREGATE ────────────────────────────
    namespace DomainCourse {
        class Course {
            -id CourseId
            -name String
            -credits Credits
            -type DisciplineType
            -capacity Capacity
            -teacherUsernames Set~Username~
            -studentUsernames Set~Username~
            -lessons List~Lesson~
            -prerequisites Set~CourseId~
            -grades Map~Username,Grade~
            +id() CourseId
            +credits() Credits
            +capacity() Capacity
            +rename(String)
            +changeCapacity(Capacity)
            +enroll(Username)
            +unenroll(Username)
            +hasStudent(Username) boolean
            +students() Set~Username~
            +isFull() boolean
            +remainingSeats() int
            +assignTeacher(Username)
            +teachers() Set~Username~
            +addLesson(Lesson)
            +lessons() List~Lesson~
            +addPrerequisite(CourseId)
            +prerequisites() Set~CourseId~
            +recordGrade(Username,Grade)
            +gradeOf(Username) Optional~Grade~
            +allGrades() Map~Username,Grade~
        }
        class CourseId {
            <<record>>
            +value String
            +of(int)$ CourseId
        }
        class Capacity {
            <<record>>
            +max int
            +UNLIMITED$ Capacity
            +canFit(int) boolean
            +remaining(int) int
        }
        class Lesson {
            <<record>>
            +type LessonType
            +slot TimeSlot
            +room Room
            +isExam() boolean
        }
        class TimeSlot {
            <<record>>
            +day WeekDay
            +time String
            +overlaps(TimeSlot) boolean
        }
        class Room {
            <<record>>
            +name String
            +sameAs(Room) boolean
        }
        class Grade {
            <<record>>
            +firstHalf int
            +secondHalf int
            +exam int
            +PASSING_TOTAL$ int
            +total() int
            +isPassing() boolean
            +letter() String
        }
    }

    %% ── DOMAIN: RESEARCH ────────────────────────────────────
    namespace DomainResearch {
        class ResearchPaper {
            -id PaperId
            -title String
            -author Username
            -journal JournalName
            -abstractText String
            -pages int
            -doi String
            -publishedDate LocalDate
            -citations int
            +id() PaperId
            +author() Username
            +journal() JournalName
            +citations() int
            +cite()
            +length() int
        }
        class ResearchProject {
            -id int
            -journal JournalName
            -topic String
            -supervisor Username
            -participants Set~Username~
            -publishedPapers List~PaperId~
            +id() int
            +journal() JournalName
            +supervisor() Username
            +participants() Set~Username~
            +publishedPapers() List~PaperId~
            +addParticipant(Username)
            +recordPublication(PaperId)
        }
        class PaperId {
            <<record>>
            +value int
        }
        class JournalName {
            <<record>>
            +value String
            +matches(String) boolean
        }
        class HIndex {
            <<record>>
            +value int
            +MIN_FOR_SUPERVISION$ HIndex
            +atLeast(HIndex) boolean
        }
    }

    %% ── DOMAIN: LIBRARY ─────────────────────────────────────
    namespace DomainLibrary {
        class Book {
            -id BookId
            -title String
            -author String
            -borrowedBy Username
            +id() BookId
            +isBorrowed() boolean
            +borrower() Optional~Username~
            +lendTo(Username)
            +returnFrom(Username)
        }
        class BookId {
            <<record>>
            +value int
        }
    }

    %% ── DOMAIN: MESSAGING ───────────────────────────────────
    namespace DomainMessaging {
        class Message {
            -id int
            -sender Username
            -recipient Username
            -subject String
            -body String
            -urgency UrgencyLevel
            -sentAt LocalDateTime
            -status MessageStatus
            +sender() Username
            +recipient() Username
            +urgency() UrgencyLevel
            +status() MessageStatus
            +markRead()
            +compareTo(Message) int
        }
        class News {
            -id int
            -title String
            -body String
            -author Username
            -publishedAt LocalDateTime
            -comments List~String~
            -pinned boolean
            +isPinned() boolean
            +edit(String,String)
            +addComment(String)
            +compareTo(News) int
        }
        class Request {
            -id int
            -requester Username
            -type HelpType
            -faculty Faculty
            -urgency UrgencyLevel
            -additionalInfo String
            -status RequestStatus
            +status() RequestStatus
            +changeStatus(RequestStatus)
        }
        class Order {
            -id int
            -requester Username
            -description String
            -createdAt LocalDate
            -status OrderStatus
            -executor Username
            +status() OrderStatus
            +executor() Optional~Username~
            +accept(Username)
            +reject()
            +complete()
        }
        class Notification {
            <<record>>
            +recipient Username
            +text String
            +at LocalDateTime
            +of(Username,String)$ Notification
        }
    }

    namespace DomainOrganization {
        class Organization {
            -name String
            -head Username
            -members Set~Username~
            +name() String
            +head() Username
            +members() Set~Username~
            +isMember(Username) boolean
            +addMember(Username)
        }
    }

    namespace DomainLogging {
        class LogEntry {
            <<record>>
            +at LocalDateTime
            +actor Username
            +action String
            +now(Username,String)$ LogEntry
        }
    }

    %% ── DOMAIN: ENROLLMENT RULES ────────────────────────────
    namespace DomainRules {
        class EnrollmentRule {
            <<interface>>
            +check(Student,Course) EnrollmentDecision
        }
        class EnrollmentDecision {
            <<record>>
            +allowed boolean
            +reason String
            +allow()$ EnrollmentDecision
            +deny(String)$ EnrollmentDecision
        }
        class MaxFailLimitRule {
            +check(Student,Course) EnrollmentDecision
        }
        class AlreadyEnrolledRule {
            +check(Student,Course) EnrollmentDecision
        }
        class CreditLimitRule {
            +check(Student,Course) EnrollmentDecision
        }
        class CapacityRule {
            +check(Student,Course) EnrollmentDecision
        }
        class PrerequisiteRule {
            -courses CourseRepository
            +check(Student,Course) EnrollmentDecision
        }
        class ScheduleConflictRule {
            -courses CourseRepository
            +check(Student,Course) EnrollmentDecision
        }
    }

    %% ── DOMAIN: SERVICES ────────────────────────────────────
    namespace DomainServices {
        class EnrollmentService {
            -rules List~EnrollmentRule~
            +tryEnroll(Student,Course) EnrollmentDecision
        }
        class RoomScheduler {
            -courses CourseRepository
            +isAvailable(Room,TimeSlot) boolean
        }
        class GpaCalculator {
            -courses CourseRepository
            +of(Student) double
        }
        class HIndexCalculator {
            +calculate(List~ResearchPaper~) HIndex
        }
        class CitationFormatter {
            +format(ResearchPaper,PaperFormat) String
        }
        class PaperPublisher {
            -papers ResearchPaperRepository
            -projects ResearchProjectRepository
            -notifications NotificationRepository
            -news NewsRepository
            -users UserRepository
            +publish(ResearchPaper)
        }
        class SubscriptionService {
            -projects ResearchProjectRepository
            +subscribe(ResearcherCapable,String) boolean
            +unsubscribe(ResearcherCapable,String)
        }
    }

    %% ── DOMAIN: SHARED VALUE OBJECTS ────────────────────────
    namespace DomainShared {
        class Username {
            -value String
            +value() String
            +equals(Object) boolean
            +hashCode() int
        }
        class PersonName {
            <<record>>
            +first String
            +last String
            +full() String
        }
        class Email {
            <<record>>
            +address String
        }
        class Money {
            <<record>>
            +amount double
        }
        class Credits {
            <<record>>
            +value int
            +SEMESTER_LIMIT$ Credits
            +covers(Credits) boolean
            +minus(Credits) Credits
            +plus(Credits) Credits
        }
        class IdSequence {
            -counter AtomicInteger
            +next() int
            +peek() int
            +seedAtLeast(int)
        }
    }

    %% ── DOMAIN: ENUMERATIONS ────────────────────────────────
    namespace DomainEnums {
        class DegreeType { <<enumeration>> BACHELOR
            MASTER
            DOCTORATE }
        class Faculty { <<enumeration>> SITE
            SEOGI
            SG
            KMA
            ISE
            BS }
        class Gender { <<enumeration>> MALE
            FEMALE }
        class Language { <<enumeration>> ENGLISH
            KAZAKH
            RUSSIAN }
        class TeacherPosition { <<enumeration>> LECTOR
            SENIOR_LECTOR
            PROFESSOR }
        class ManagerPosition { <<enumeration>> OR
            DEPARTMENT
            DEANS_OFFICE }
        class DisciplineType { <<enumeration>> MAJOR
            MINOR
            FREE }
        class LessonType { <<enumeration>> LECTURE
            PRACTICE
            OFFICE_HOURS
            EXAM }
        class WeekDay { <<enumeration>> MONDAY
            TUESDAY
            WEDNESDAY
            THURSDAY
            FRIDAY
            SATURDAY
            SUNDAY }
        class MessageStatus { <<enumeration>> UNREAD
            READ }
        class UrgencyLevel { <<enumeration>> LOW
            MEDIUM
            HIGH }
        class OrderStatus { <<enumeration>> NEW
            ACCEPTED
            REJECTED
            DONE }
        class RequestStatus { <<enumeration>> PENDING
            ACCEPTED
            NOT_APPROVED
            APPROVED
            REJECTED }
        class HelpType { <<enumeration>> TRANSCRIPT_FOR_SEMESTER
            TRANSCRIPT_FOR_YEAR
            CERTIFICATE_OF_EDUCATION
            ACADEMIC_MOBILITY
            COORDINATION_OF_DIPLOMA_TOPIC
            REQUEST_FOR_CREATING_ORGANIZATION }
        class PaperFormat { <<enumeration>> PLAIN_TEXT
            BIBTEX }
    }

    %% ── DOMAIN: REPOSITORY PORTS ────────────────────────────
    namespace DomainRepositoryPorts {
        class UserRepository {
            <<interface>>
            +save(User)
            +findByUsername(Username) Optional~User~
            +exists(Username) boolean
            +delete(Username)
            +findAll() Collection~User~
        }
        class CourseRepository {
            <<interface>>
            +save(Course)
            +findById(CourseId) Optional~Course~
            +findAll() Collection~Course~
        }
        class BookRepository {
            <<interface>>
            +save(Book)
            +findById(BookId) Optional~Book~
            +findFirstAvailableByTitle(String) Optional~Book~
            +findAll() Collection~Book~
            +delete(BookId)
        }
        class MessageRepository {
            <<interface>>
            +save(Message)
            +inboxOf(Username) List~Message~
        }
        class NewsRepository {
            <<interface>>
            +save(News)
            +findById(int) Optional~News~
            +findAllSorted() List~News~
            +delete(int)
        }
        class RequestRepository {
            <<interface>>
            +save(Request)
            +findById(int) Optional~Request~
            +findAll() List~Request~
            +findByRequester(Username) List~Request~
        }
        class OrderRepository {
            <<interface>>
            +save(Order)
            +findById(int) Optional~Order~
            +findAll() List~Order~
        }
        class ResearchPaperRepository {
            <<interface>>
            +save(ResearchPaper)
            +findById(PaperId) Optional~ResearchPaper~
            +findByAuthor(Username) List~ResearchPaper~
            +findAll() Collection~ResearchPaper~
        }
        class ResearchProjectRepository {
            <<interface>>
            +save(ResearchProject)
            +findByJournal(JournalName) Optional~ResearchProject~
            +findAll() Collection~ResearchProject~
        }
        class OrganizationRepository {
            <<interface>>
            +save(Organization)
            +findByName(String) Optional~Organization~
            +findAll() Collection~Organization~
        }
        class NotificationRepository {
            <<interface>>
            +save(Notification)
            +findFor(Username) List~Notification~
            +clearFor(Username)
        }
        class LogRepository {
            <<interface>>
            +append(LogEntry)
            +findAll() List~LogEntry~
            +findByActor(Username) List~LogEntry~
        }
    }

    %% ── INFRASTRUCTURE ──────────────────────────────────────
    namespace InfraAuth {
        class PlainPasswordHasher {
            +hash(String) String
            +matches(String,String) boolean
        }
    }
    namespace InfraI18n {
        class Translator {
            <<interface>>
            +get(String) String
            +fmt(String,Object[]) String
        }
        class PropertiesTranslator {
            -bundle ResourceBundle
            +get(String) String
            +fmt(String,Object[]) String
            +setLanguage(Language)
        }
    }
    namespace InfraLogging {
        class Logger {
            <<interface>>
            +log(Username,String)
        }
        class RepositoryLogger {
            -repository LogRepository
            +log(Username,String)
        }
    }
    namespace InfraJson {
        class JsonValue {
            <<sealed>>
            +asString() String
            +asInt() int
            +asDouble() double
            +asBoolean() boolean
            +asArray() List~JsonValue~
            +asObject() Map~String,JsonValue~
            +isNull() boolean
        }
        class JsonString { +value String }
        class JsonNumber { +value double }
        class JsonBool   { +value boolean }
        class JsonNull   { }
        class JsonArray  { +elements List~JsonValue~ }
        class JsonObject {
            +fields Map~String,JsonValue~
            +get(String) JsonValue
            +has(String) boolean
        }
        class JsonReader  { +parse(String)$ JsonValue }
        class JsonWriter  { +write(JsonValue)$ String }
        class JsonObjectBuilder {
            +put(String,JsonValue) JsonObjectBuilder
            +put(String,String) JsonObjectBuilder
            +put(String,int) JsonObjectBuilder
            +put(String,double) JsonObjectBuilder
            +put(String,boolean) JsonObjectBuilder
            +build() JsonObject
        }
    }
    namespace InfraDatabase {
        class Database {
            <<interface>>
            +readTable(String) List~JsonValue~
            +writeTable(String,List~JsonValue~)
        }
        class JsonFileDatabase {
            -root Path
            +readTable(String) List~JsonValue~
            +writeTable(String,List~JsonValue~)
        }
    }
    namespace InfraOrm {
        class EntityMapper~T,ID~ {
            <<interface>>
            +toJson(T) JsonValue
            +fromJson(JsonValue) T
            +idOf(T) ID
            +idAsString(ID) String
        }
        class Repository~T,ID~ {
            -db Database
            -tableName String
            -mapper EntityMapper~T,ID~
            +save(T) T
            +find(ID) Optional~T~
            +findAll() List~T~
            +delete(T) boolean
            +count() int
            +select() QueryBuilder~T,ID~
            +where(String,Op,Object) QueryBuilder~T,ID~
        }
        class QueryBuilder~T,ID~ {
            -conditions List~Condition~
            +where(String,Op,Object) QueryBuilder~T,ID~
            +whereEq(String,Object) QueryBuilder~T,ID~
            +whereContains(String,Object) QueryBuilder~T,ID~
            +whereMatch(Predicate~T~) QueryBuilder~T,ID~
            +orderBy(Comparator~T~) QueryBuilder~T,ID~
            +limit(int) QueryBuilder~T,ID~
            +offset(int) QueryBuilder~T,ID~
            +list() List~T~
            +first() Optional~T~
            +count() int
            +deleteAll() int
            +updateEach(Consumer~T~) int
        }
        class Condition {
            +field String
            +op Op
            +value Object
            +matches(JsonObject) boolean
        }
        class Op {
            <<enumeration>>
            EQ
            NEQ
            GT
            LT
            GTE
            LTE
            CONTAINS
            STARTS_WITH
            ENDS_WITH
            IN
        }
        class MapperHelpers {
            +strOrNull(JsonObject,String)$ String
            +intVal(JsonObject,String)$ int
            +doubleVal(JsonObject,String)$ double
            +boolVal(JsonObject,String)$ boolean
        }
    }

    %% ORM-backed repositories (implement domain ports via generic Repository)
    namespace InfraOrmRepos {
        class OrmUserRepository         { -repo Repository~User,Username~ }
        class OrmCourseRepository       { -repo Repository~Course,CourseId~ }
        class OrmBookRepository         { -repo Repository~Book,BookId~ }
        class OrmMessageRepository      { -repo Repository~Message,Integer~ }
        class OrmNewsRepository         { -repo Repository~News,Integer~ }
        class OrmRequestRepository      { -repo Repository~Request,Integer~ }
        class OrmOrderRepository        { -repo Repository~Order,Integer~ }
        class OrmResearchPaperRepository  { -repo Repository~ResearchPaper,PaperId~ }
        class OrmResearchProjectRepository{ -repo Repository~ResearchProject,Integer~ }
        class OrmOrganizationRepository { -repo Repository~Organization,String~ }
        class OrmNotificationRepository { -repo Repository~Notification,String~ }
        class OrmLogRepository          { -repo Repository~LogEntry,String~ }
    }

    %% Entity mappers (implement EntityMapper interface)
    namespace InfraMappers {
        class UserMapper             { }
        class CourseMapper           { }
        class BookMapper             { }
        class MessageMapper          { }
        class NewsMapper             { }
        class RequestMapper          { }
        class OrderMapper            { }
        class ResearchPaperMapper    { }
        class ResearchProjectMapper  { }
        class OrganizationMapper     { }
        class NotificationMapper     { }
        class LogEntryMapper         { }
    }

    %% In-memory repos for tests (implement same domain ports)
    namespace InfraInMemory {
        class InMemoryUserRepository              { -store Map~Username,User~ }
        class InMemoryCourseRepository            { -store Map~CourseId,Course~ }
        class InMemoryBookRepository              { -store Map~BookId,Book~ }
        class InMemoryMessageRepository           { -store List~Message~ }
        class InMemoryNewsRepository              { -store Map~Integer,News~ }
        class InMemoryOrderRepository             { -store Map~Integer,Order~ }
        class InMemoryResearchPaperRepository     { -store Map~PaperId,ResearchPaper~ }
        class InMemoryResearchProjectRepository   { -store Map~Integer,ResearchProject~ }
        class InMemoryOrganizationRepository      { -store Map~String,Organization~ }
        class InMemoryNotificationRepository      { -store List~Notification~ }
        class InMemoryLogRepository               { -store List~LogEntry~ }
    }

    %% ════════════════════════════════════════════════════════
    %%  RELATIONSHIPS
    %% ════════════════════════════════════════════════════════

    %% ── Inheritance  <|-- ────────────────────────────────────
    User <|-- Admin
    User <|-- Student
    User <|-- Employee
    Student <|-- GraduateStudent
    Employee <|-- Teacher
    Employee <|-- Manager
    Employee <|-- Librarian
    Employee <|-- TechSupport
    Employee <|-- EmployeeResearcher
    Teacher <|-- Dean
    JsonValue <|-- JsonString
    JsonValue <|-- JsonNumber
    JsonValue <|-- JsonBool
    JsonValue <|-- JsonNull
    JsonValue <|-- JsonArray
    JsonValue <|-- JsonObject

    %% ── Realisation  ..|> ────────────────────────────────────
    StdConsole ..|> Console
    DefaultMenuFactory ..|> MenuFactory
    AdminMenu ..|> Menu
    StudentMenu ..|> Menu
    GraduateStudentMenu ..|> Menu
    TeacherMenu ..|> Menu
    DeanMenu ..|> Menu
    ManagerMenu ..|> Menu
    LibrarianMenu ..|> Menu
    TechSupportMenu ..|> Menu
    EmployeeResearcherMenu ..|> Menu
    Student ..|> ResearcherCapable
    Student ..|> BookBorrowerCapable
    GraduateStudent ..|> ResearcherCapable
    Teacher ..|> ResearcherCapable
    Teacher ..|> BookBorrowerCapable
    EmployeeResearcher ..|> ResearcherCapable
    MaxFailLimitRule ..|> EnrollmentRule
    AlreadyEnrolledRule ..|> EnrollmentRule
    CreditLimitRule ..|> EnrollmentRule
    CapacityRule ..|> EnrollmentRule
    PrerequisiteRule ..|> EnrollmentRule
    ScheduleConflictRule ..|> EnrollmentRule
    PlainPasswordHasher ..|> PasswordHasher
    PropertiesTranslator ..|> Translator
    RepositoryLogger ..|> Logger
    JsonFileDatabase ..|> Database
    UserMapper ..|> EntityMapper~T,ID~
    CourseMapper ..|> EntityMapper~T,ID~
    BookMapper ..|> EntityMapper~T,ID~
    MessageMapper ..|> EntityMapper~T,ID~
    NewsMapper ..|> EntityMapper~T,ID~
    RequestMapper ..|> EntityMapper~T,ID~
    OrderMapper ..|> EntityMapper~T,ID~
    ResearchPaperMapper ..|> EntityMapper~T,ID~
    ResearchProjectMapper ..|> EntityMapper~T,ID~
    OrganizationMapper ..|> EntityMapper~T,ID~
    NotificationMapper ..|> EntityMapper~T,ID~
    LogEntryMapper ..|> EntityMapper~T,ID~
    OrmUserRepository ..|> UserRepository
    OrmCourseRepository ..|> CourseRepository
    OrmBookRepository ..|> BookRepository
    OrmMessageRepository ..|> MessageRepository
    OrmNewsRepository ..|> NewsRepository
    OrmRequestRepository ..|> RequestRepository
    OrmOrderRepository ..|> OrderRepository
    OrmResearchPaperRepository ..|> ResearchPaperRepository
    OrmResearchProjectRepository ..|> ResearchProjectRepository
    OrmOrganizationRepository ..|> OrganizationRepository
    OrmNotificationRepository ..|> NotificationRepository
    OrmLogRepository ..|> LogRepository
    InMemoryUserRepository ..|> UserRepository
    InMemoryCourseRepository ..|> CourseRepository
    InMemoryBookRepository ..|> BookRepository
    InMemoryMessageRepository ..|> MessageRepository
    InMemoryNewsRepository ..|> NewsRepository
    InMemoryOrderRepository ..|> OrderRepository
    InMemoryResearchPaperRepository ..|> ResearchPaperRepository
    InMemoryResearchProjectRepository ..|> ResearchProjectRepository
    InMemoryOrganizationRepository ..|> OrganizationRepository
    InMemoryNotificationRepository ..|> NotificationRepository
    InMemoryLogRepository ..|> LogRepository
    Message ..|> Comparable
    News ..|> Comparable

    %% ── Composition  *-- ─────────────────────────────────────
    User *-- Username : username
    User *-- PersonName : name
    User *-- Email : email
    Employee *-- Money : salary
    Student "0..1" *-- ResearcherProfile
    Teacher "0..1" *-- ResearcherProfile
    EmployeeResearcher "0..1" *-- ResearcherProfile
    ResearcherProfile *-- Username : owner
    Course *-- CourseId : id
    Course *-- Credits : credits
    Course *-- Capacity : capacity
    Course "1" *-- "0..*" Lesson : lessons
    Lesson *-- TimeSlot : slot
    Lesson *-- Room : room
    ResearchPaper *-- PaperId : id
    ResearchPaper *-- Username : author
    ResearchPaper *-- JournalName : journal
    ResearchProject *-- JournalName : journal
    ResearchProject *-- Username : supervisor
    Book *-- BookId : id
    Message *-- Username : sender/recipient
    News *-- Username : author
    Request *-- Username : requester
    Order *-- Username : requester
    Notification *-- Username : recipient
    Organization *-- Username : head
    LogEntry *-- Username : actor
    QueryBuilder~T,ID~ "1" *-- "0..*" Condition : conditions

    %% ── Aggregation  o-- ─────────────────────────────────────
    ResearchProject "1" o-- "0..*" PaperId : publishedPapers
    ResearchProject "1" o-- "0..*" Username : participants
    Organization "1" o-- "1..*" Username : members
    Student "1" o-- "0..*" CourseId : enrolled/completed
    Teacher "1" o-- "0..*" CourseId : taughtCourses
    Course "1" o-- "0..*" CourseId : prerequisites
    Course "1" o-- "0..*" Grade : grades
    EnrollmentService "1" o-- "1..*" EnrollmentRule : rules

    %% ── Association  --> ─────────────────────────────────────
    User --> Faculty
    User --> Gender
    User --> Language
    Student --> DegreeType
    Student --> Credits : availableCredits
    Teacher --> TeacherPosition
    Manager --> ManagerPosition
    Course --> DisciplineType
    Lesson --> LessonType
    TimeSlot --> WeekDay
    Message --> UrgencyLevel
    Message --> MessageStatus
    Request --> HelpType
    Request --> Faculty
    Request --> UrgencyLevel
    Request --> RequestStatus
    Order --> OrderStatus
    Order "0..1" --> Username : executor
    Book "0..1" --> Username : borrowedBy
    GraduateStudent "0..1" --> Username : supervisorUsername
    Condition --> Op

    %% ── Dependency  ..> ──────────────────────────────────────
    %% Bootstrap
    AppContext ..> UserRepository
    AppContext ..> CourseRepository
    AppContext ..> BookRepository
    AppContext ..> MessageRepository
    AppContext ..> NewsRepository
    AppContext ..> RequestRepository
    AppContext ..> OrderRepository
    AppContext ..> ResearchPaperRepository
    AppContext ..> ResearchProjectRepository
    AppContext ..> OrganizationRepository
    AppContext ..> NotificationRepository
    AppContext ..> LogRepository
    AppContext ..> EnrollmentService
    AppContext ..> PaperPublisher
    DefaultMenuFactory ..> AppContext
    LoginScreen ..> UserRepository
    LoginScreen ..> PasswordHasher
    %% Admin use cases
    CreateStudent ..> UserRepository
    CreateStudent ..> LogRepository
    CreateTeacher ..> UserRepository
    CreateGradStudent ..> UserRepository
    CreateDean ..> UserRepository
    CreateManager ..> UserRepository
    CreateLibrarian ..> UserRepository
    CreateTechSupport ..> UserRepository
    CreateEmpResearcher ..> UserRepository
    DeleteUser ..> UserRepository
    DeleteUser ..> LogRepository
    GenerateAcademicReport ..> UserRepository
    GenerateAcademicReport ..> CourseRepository
    GenerateAcademicReport ..> GpaCalculator
    GenerateTopResearcherNews ..> UserRepository
    GenerateTopResearcherNews ..> ResearchPaperRepository
    GenerateTopResearcherNews ..> NewsRepository
    GenerateTopResearcherNews ..> HIndexCalculator
    %% Course use cases
    EnrollInCourse ..> UserRepository
    EnrollInCourse ..> CourseRepository
    EnrollInCourse ..> EnrollmentService
    EnrollInCourse ..> LogRepository
    DropCourse ..> UserRepository
    DropCourse ..> CourseRepository
    DropCourse ..> LogRepository
    RecordMarks ..> UserRepository
    RecordMarks ..> CourseRepository
    RecordMarks ..> LogRepository
    AssignTeacher ..> UserRepository
    AssignTeacher ..> CourseRepository
    CreateCourse ..> CourseRepository
    CreateCourse ..> IdSequence
    CreateCourse ..> LogRepository
    SetCourseCapacity ..> CourseRepository
    SetCoursePrerequisite ..> CourseRepository
    AddLesson ..> CourseRepository
    AddLesson ..> RoomScheduler
    ViewTranscript ..> UserRepository
    ViewTranscript ..> CourseRepository
    ViewTranscript ..> GpaCalculator
    %% Library use cases
    BorrowBook ..> BookRepository
    BorrowBook ..> UserRepository
    BorrowBook ..> LogRepository
    ReturnBook ..> BookRepository
    ReturnBook ..> LogRepository
    AddBook ..> BookRepository
    AddBook ..> IdSequence
    RemoveBook ..> BookRepository
    %% Messaging use cases
    SendMessage ..> MessageRepository
    SendMessage ..> UserRepository
    SendMessage ..> IdSequence
    PublishNews ..> NewsRepository
    PublishNews ..> IdSequence
    PublishNews ..> LogRepository
    CommentOnNews ..> NewsRepository
    SubmitRequest ..> RequestRepository
    SubmitRequest ..> IdSequence
    ProcessRequest ..> RequestRepository
    ProcessRequest ..> UserRepository
    CreateITOrder ..> OrderRepository
    CreateITOrder ..> IdSequence
    AcceptOrder ..> OrderRepository
    AcceptOrder ..> LogRepository
    CompleteOrder ..> OrderRepository
    CompleteOrder ..> LogRepository
    %% Org use cases
    CreateOrganization ..> OrganizationRepository
    CreateOrganization ..> UserRepository
    CreateOrganization ..> LogRepository
    JoinOrganization ..> OrganizationRepository
    JoinOrganization ..> LogRepository
    %% Research use cases
    CreateResearchProject ..> ResearchProjectRepository
    CreateResearchProject ..> UserRepository
    CreateResearchProject ..> IdSequence
    CreateResearchProject ..> LogRepository
    JoinResearchProject ..> ResearchProjectRepository
    JoinResearchProject ..> UserRepository
    PublishPaper ..> PaperPublisher
    PublishPaper ..> UserRepository
    PublishPaper ..> IdSequence
    SubscribeToJournal ..> SubscriptionService
    SubscribeToJournal ..> UserRepository
    UnsubscribeFromJournal ..> SubscriptionService
    UnsubscribeFromJournal ..> UserRepository
    GenerateCitation ..> ResearchPaperRepository
    GenerateCitation ..> CitationFormatter
    SetSupervisor ..> UserRepository
    SetSupervisor ..> ResearchProjectRepository
    SetSupervisor ..> HIndexCalculator
    SetSupervisor ..> ResearchPaperRepository
    %% User use cases
    BecomeResearcher ..> UserRepository
    BecomeResearcher ..> LogRepository
    RateTeacher ..> UserRepository
    RateTeacher ..> LogRepository
    ComplainAboutStudent ..> UserRepository
    ComplainAboutStudent ..> MessageRepository
    ComplainAboutStudent ..> IdSequence
    %% Domain Services → repos
    PrerequisiteRule ..> CourseRepository
    ScheduleConflictRule ..> CourseRepository
    EnrollmentService ..> EnrollmentDecision
    RoomScheduler ..> CourseRepository
    GpaCalculator ..> CourseRepository
    HIndexCalculator ..> HIndex
    CitationFormatter ..> PaperFormat
    PaperPublisher ..> ResearchPaperRepository
    PaperPublisher ..> ResearchProjectRepository
    PaperPublisher ..> NotificationRepository
    PaperPublisher ..> NewsRepository
    PaperPublisher ..> UserRepository
    SubscriptionService ..> ResearchProjectRepository
    %% Repository ports → entities
    UserRepository ..> User
    CourseRepository ..> Course
    BookRepository ..> Book
    MessageRepository ..> Message
    NewsRepository ..> News
    RequestRepository ..> Request
    OrderRepository ..> Order
    ResearchPaperRepository ..> ResearchPaper
    ResearchProjectRepository ..> ResearchProject
    OrganizationRepository ..> Organization
    NotificationRepository ..> Notification
    LogRepository ..> LogEntry
    %% ORM internals
    Repository~T,ID~ ..> Database
    Repository~T,ID~ ..> EntityMapper~T,ID~
    Repository~T,ID~ ..> QueryBuilder~T,ID~
    QueryBuilder~T,ID~ ..> Database
    QueryBuilder~T,ID~ ..> EntityMapper~T,ID~
    JsonFileDatabase ..> JsonReader
    JsonFileDatabase ..> JsonWriter
    RepositoryLogger ..> LogRepository

```