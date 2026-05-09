package infrastructure.persistence.mapper;

import domain.course.CourseId;
import domain.enums.DegreeType;
import domain.enums.Faculty;
import domain.enums.Gender;
import domain.enums.Language;
import domain.enums.ManagerPosition;
import domain.enums.TeacherPosition;
import domain.shared.Email;
import domain.shared.Money;
import domain.shared.PersonName;
import domain.shared.Username;
import domain.user.Admin;
import domain.user.Dean;
import domain.user.EmployeeResearcher;
import domain.user.GraduateStudent;
import domain.user.Librarian;
import domain.user.Manager;
import domain.user.ResearcherCapable;
import domain.user.ResearcherProfile;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.TechSupport;
import domain.user.User;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class UserMapper implements EntityMapper<User, Username> {

    @Override public Username idOf(User user) { return user.username(); }
    @Override public String idAsString(Username id) { return id.value(); }

    @Override public JsonValue toJson(User user) {
        JsonObjectBuilder b = JsonObjectBuilder.create()
                .put("_id", user.username().value())
                .put("type", user.getClass().getSimpleName())
                .put("username", user.username().value())
                .put("passwordHash", user.passwordHash())
                .put("firstName", user.name().first())
                .put("lastName", user.name().last())
                .put("gender", user.gender() == null ? null : user.gender().name())
                .put("dateOfBirth", user.dateOfBirth() == null ? null : user.dateOfBirth().toString())
                .put("email", user.email() == null ? null : user.email().address())
                .put("faculty", user.faculty() == null ? null : user.faculty().name())
                .put("language", user.language().name());

        if (user instanceof Student s) writeStudentFields(b, s);
        if (user instanceof GraduateStudent gs) writeGradFields(b, gs);
        if (user instanceof Teacher t) writeTeacherFields(b, t);
        if (user instanceof Manager m) b.put("managerPosition", m.position().name());
        if (user instanceof EmployeeResearcher er) {
            writeEmployeeFields(b, er);
            b.put("defaultField", er.defaultField());
            writeResearcherFields(b, er);
        }
        if (user instanceof Librarian l) writeEmployeeFields(b, l);
        if (user instanceof TechSupport ts) writeEmployeeFields(b, ts);
        if (user instanceof Manager m) writeEmployeeFields(b, m);

        return b.build();
    }

    private void writeEmployeeFields(JsonObjectBuilder b, domain.user.Employee e) {
        b.put("salary", e.salary().amount());
        b.put("hireDate", e.hireDate() == null ? null : e.hireDate().toString());
        b.put("insuranceNumber", e.insuranceNumber());
    }

    private void writeStudentFields(JsonObjectBuilder b, Student s) {
        b.put("degreeType", s.degreeType().name());
        b.put("studyYear", s.studyYear());
        b.put("availableCredits", s.availableCredits().value());
        b.put("failCount", s.failCount());
        b.putStrings("enrolled", s.enrolledCourses().stream().map(CourseId::value).toList());
        b.putStrings("completed", s.completedCourses().stream().map(CourseId::value).toList());
        writeResearcherFields(b, s);
    }

    private void writeGradFields(JsonObjectBuilder b, GraduateStudent gs) {
        gs.supervisor().ifPresent(u -> b.put("supervisor", u.value()));
    }

    private void writeTeacherFields(JsonObjectBuilder b, Teacher t) {
        writeEmployeeFields(b, t);
        b.put("degree", t.degree());
        b.put("position", t.position().name());
        b.putStrings("taughtCourses", t.taughtCourses().stream().map(CourseId::value).toList());
        writeResearcherFields(b, t);
    }

    private void writeResearcherFields(JsonObjectBuilder b, ResearcherCapable rc) {
        if (!rc.isResearcher()) return;
        ResearcherProfile p = rc.researcherProfile();
        b.put("researcherField", p.field());
        b.putStrings("subscribedJournals", p.subscribedJournals());
    }

    @Override public User fromJson(JsonValue json) {
        JsonValue.JsonObject obj = (JsonValue.JsonObject) json;
        String type = MapperHelpers.readString(obj, "type");
        Username username = new Username(MapperHelpers.readString(obj, "username"));
        String passwordHash = MapperHelpers.readString(obj, "passwordHash");
        PersonName name = new PersonName(MapperHelpers.readString(obj, "firstName"), MapperHelpers.readString(obj, "lastName"));
        Gender gender = readEnum(obj, "gender", Gender.class);
        LocalDate dob = readDate(obj, "dateOfBirth");
        Email email = readEmail(obj);
        Faculty faculty = readEnum(obj, "faculty", Faculty.class);
        Language language = readEnum(obj, "language", Language.class);

        User user = switch (type) {
            case "Student" -> buildStudent(obj, username, passwordHash, name, gender, dob, email, faculty);
            case "GraduateStudent" -> buildGrad(obj, username, passwordHash, name, gender, dob, email, faculty);
            case "Teacher" -> buildTeacher(obj, username, passwordHash, name, gender, dob, email, faculty);
            case "Dean" -> buildDean(obj, username, passwordHash, name, gender, dob, email, faculty);
            case "Manager" -> buildManager(obj, username, passwordHash, name, gender, dob, email, faculty);
            case "EmployeeResearcher" -> buildEmpRes(obj, username, passwordHash, name, gender, dob, email, faculty);
            case "Librarian" -> buildLibrarian(obj, username, passwordHash, name, gender, dob, email, faculty);
            case "TechSupport" -> buildTechSupport(obj, username, passwordHash, name, gender, dob, email, faculty);
            case "Admin" -> new Admin(username, passwordHash, name, gender, dob, email, faculty);
            default -> throw new IllegalStateException("Unknown user type: " + type);
        };

        if (language != null) user.changeLanguage(language);
        applyResearcher(obj, user);
        return user;
    }

    private Student buildStudent(JsonValue.JsonObject o, Username u, String h, PersonName n, Gender g,
                                 LocalDate dob, Email e, Faculty f) {
        Student s = new Student(u, h, n, g, dob, e, f,
                readEnum(o, "degreeType", DegreeType.class), MapperHelpers.readInt(o, "studyYear"));
        restoreStudentState(o, s);
        return s;
    }

    private GraduateStudent buildGrad(JsonValue.JsonObject o, Username u, String h, PersonName n, Gender g,
                                      LocalDate dob, Email e, Faculty f) {
        GraduateStudent gs = new GraduateStudent(u, h, n, g, dob, e, f,
                readEnum(o, "degreeType", DegreeType.class), MapperHelpers.readInt(o, "studyYear"));
        restoreStudentState(o, gs);
        String sup = MapperHelpers.readString(o, "supervisor");
        if (sup != null) gs.setSupervisor(new Username(sup));
        return gs;
    }

    private void restoreStudentState(JsonValue.JsonObject o, Student s) {
        int avail = MapperHelpers.readIntOr(o, "availableCredits", 21);
        int fails = MapperHelpers.readIntOr(o, "failCount", 0);
        domain.shared.Credits start = new domain.shared.Credits(21);
        if (avail < start.value()) s.recordEnrollment(new CourseId("__warm__"), new domain.shared.Credits(start.value() - avail));
        s.recordDrop(new CourseId("__warm__"), new domain.shared.Credits(0));
        for (int i = 0; i < fails; i++) s.recordFail();
        for (String cid : MapperHelpers.readStrings(o, "enrolled"))
            s.recordEnrollment(new CourseId(cid), new domain.shared.Credits(0));
        for (String cid : MapperHelpers.readStrings(o, "completed"))
            s.recordCompletion(new CourseId(cid));
    }

    private Teacher buildTeacher(JsonValue.JsonObject o, Username u, String h, PersonName n, Gender g,
                                 LocalDate dob, Email e, Faculty f) {
        Teacher t = new Teacher(u, h, n, g, dob, e, f,
                new Money(MapperHelpers.readDoubleOr(o, "salary", 0)),
                readDate(o, "hireDate"),
                MapperHelpers.readString(o, "insuranceNumber"),
                MapperHelpers.readString(o, "degree"),
                readEnum(o, "position", TeacherPosition.class));
        restoreTeacherState(o, t);
        return t;
    }

    private Dean buildDean(JsonValue.JsonObject o, Username u, String h, PersonName n, Gender g,
                           LocalDate dob, Email e, Faculty f) {
        Dean d = new Dean(u, h, n, g, dob, e, f,
                new Money(MapperHelpers.readDoubleOr(o, "salary", 0)),
                readDate(o, "hireDate"),
                MapperHelpers.readString(o, "insuranceNumber"),
                MapperHelpers.readString(o, "degree"));
        restoreTeacherState(o, d);
        return d;
    }

    private void restoreTeacherState(JsonValue.JsonObject o, Teacher t) {
        for (String cid : MapperHelpers.readStrings(o, "taughtCourses"))
            t.recordCourseAssignment(new CourseId(cid));
    }

    private Manager buildManager(JsonValue.JsonObject o, Username u, String h, PersonName n, Gender g,
                                 LocalDate dob, Email e, Faculty f) {
        return new Manager(u, h, n, g, dob, e, f,
                new Money(MapperHelpers.readDoubleOr(o, "salary", 0)),
                readDate(o, "hireDate"),
                MapperHelpers.readString(o, "insuranceNumber"),
                readEnum(o, "managerPosition", ManagerPosition.class));
    }

    private EmployeeResearcher buildEmpRes(JsonValue.JsonObject o, Username u, String h, PersonName n, Gender g,
                                           LocalDate dob, Email e, Faculty f) {
        return new EmployeeResearcher(u, h, n, g, dob, e, f,
                new Money(MapperHelpers.readDoubleOr(o, "salary", 0)),
                readDate(o, "hireDate"),
                MapperHelpers.readString(o, "insuranceNumber"),
                MapperHelpers.readString(o, "defaultField"));
    }

    private Librarian buildLibrarian(JsonValue.JsonObject o, Username u, String h, PersonName n, Gender g,
                                     LocalDate dob, Email e, Faculty f) {
        return new Librarian(u, h, n, g, dob, e, f,
                new Money(MapperHelpers.readDoubleOr(o, "salary", 0)),
                readDate(o, "hireDate"),
                MapperHelpers.readString(o, "insuranceNumber"));
    }

    private TechSupport buildTechSupport(JsonValue.JsonObject o, Username u, String h, PersonName n, Gender g,
                                         LocalDate dob, Email e, Faculty f) {
        return new TechSupport(u, h, n, g, dob, e, f,
                new Money(MapperHelpers.readDoubleOr(o, "salary", 0)),
                readDate(o, "hireDate"),
                MapperHelpers.readString(o, "insuranceNumber"));
    }

    private void applyResearcher(JsonValue.JsonObject o, User user) {
        String field = MapperHelpers.readString(o, "researcherField");
        if (field == null) return;
        if (!(user instanceof ResearcherCapable rc)) return;
        rc.activateResearcher(field);
        for (String journal : MapperHelpers.readStrings(o, "subscribedJournals"))
            rc.researcherProfile().subscribe(journal);
    }

    private <E extends Enum<E>> E readEnum(JsonValue.JsonObject obj, String key, Class<E> type) {
        String v = MapperHelpers.readString(obj, key);
        return v == null ? null : Enum.valueOf(type, v);
    }

    private LocalDate readDate(JsonValue.JsonObject obj, String key) {
        String v = MapperHelpers.readString(obj, key);
        return v == null ? null : LocalDate.parse(v);
    }

    private Email readEmail(JsonValue.JsonObject obj) {
        String v = MapperHelpers.readString(obj, "email");
        return v == null ? null : new Email(v);
    }
}
