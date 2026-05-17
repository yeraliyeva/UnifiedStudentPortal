package presentation.rest.controller;

import application.Result;
import application.usecase.admin.GenerateAcademicReport;
import bootstrap.AppContext;
import domain.enums.DegreeType;
import domain.enums.Faculty;
import domain.enums.Gender;
import domain.logging.LogEntry;
import domain.shared.Username;
import domain.user.Admin;
import domain.user.User;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import presentation.rest.auth.RequestContext;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;
import presentation.rest.serializer.UserSerializer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static presentation.rest.controller.ControllerUtils.*;

/** Handles user management and reporting endpoints; all routes require Admin role. */
public final class AdminController {
    private final AppContext ctx;

    public AdminController(AppContext ctx) {
        this.ctx = ctx;
    }

    public HttpResponse listUsers(HttpRequest request) {
        Collection<User> all = ctx.userRepository.findAll();
        List<JsonValue> arr = new ArrayList<>();
        for (User u : all) arr.add(UserSerializer.toJson(u));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    public HttpResponse getUser(HttpRequest request) {
        String username = request.pathSegment(2).orElse("");
        return ctx.userRepository.findByUsername(new Username(username))
                .map(u -> HttpResponse.ok(UserSerializer.toJson(u)))
                .orElse(HttpResponse.notFound("User not found: " + username));
    }

    public HttpResponse createStudent(HttpRequest request) {
        Admin actor  = (Admin) RequestContext.current();
        JsonValue.JsonObject body = request.body();

        String username   = str(body, "username");
        String password   = str(body, "password");
        String firstName  = str(body, "firstName");
        String lastName   = str(body, "lastName");
        String email      = str(body, "email");
        String facultyStr = str(body, "faculty");
        String degreeStr  = str(body, "degreeType");
        int studyYear     = intVal(body, "studyYear");

        if (username.isBlank() || password.isBlank() || firstName.isBlank())
            return HttpResponse.badRequest("username, password, firstName are required.");

        try {
            Faculty    faculty = Faculty.valueOf(facultyStr.toUpperCase());
            DegreeType degree  = DegreeType.valueOf(degreeStr.toUpperCase());

            var student = ctx.createStudent.execute(
                    actor.username(), firstName, lastName, username, password,
                    Gender.MALE, LocalDate.now(), email, faculty, degree, studyYear);
            return HttpResponse.created(UserSerializer.toJson(student));
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest("Invalid enum value: " + e.getMessage());
        }
    }

    public HttpResponse deleteUser(HttpRequest request) {
        Admin  actor    = (Admin) RequestContext.current();
        String username = request.pathSegment(2).orElse("");
        Result result   = ctx.deleteUser.execute(actor.username(), new Username(username));
        return resultToResponse(result);
    }

    public HttpResponse viewLogs(HttpRequest request) {
        List<LogEntry> logs = ctx.logRepository.findAll();
        List<JsonValue> arr = new ArrayList<>();
        for (LogEntry e : logs) {
            arr.add(JsonObjectBuilder.create()
                    .put("at",     e.at().toString())
                    .put("actor",  e.actor().value())
                    .put("action", e.action())
                    .build());
        }
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    public HttpResponse academicReport(HttpRequest request) {
        Admin actor  = (Admin) RequestContext.current();
        GenerateAcademicReport.Report report = ctx.generateAcademicReport.execute(actor.username());

        List<JsonValue> rows = new ArrayList<>();
        for (var row : report.rows()) {
            rows.add(JsonObjectBuilder.create()
                    .put("course",   row.courseName())
                    .put("enrolled", row.enrolled())
                    .put("capacity", row.max())
                    .put("avgScore", row.avg())
                    .put("passing",  row.passing())
                    .build());
        }
        List<JsonValue> top = new ArrayList<>();
        for (var ts : report.top()) {
            top.add(JsonObjectBuilder.create()
                    .put("username", ts.username())
                    .put("fullName", ts.fullName())
                    .put("gpa",      ts.gpa())
                    .build());
        }
        return HttpResponse.ok(JsonObjectBuilder.create()
                .put("totalCourses",    report.totalCourses())
                .put("totalStudents",   report.totalStudents())
                .put("totalTeachers",   report.totalTeachers())
                .put("averageGpa",      report.averageGpa())
                .put("failingStudents", report.failingStudents())
                .putObjects("courseRows",  rows)
                .putObjects("topStudents", top)
                .build());
    }


    private static HttpResponse resultToResponse(Result result) {
        if (result.success()) {
            return HttpResponse.ok(JsonObjectBuilder.create().put("message", result.message()).build());
        }
        return HttpResponse.badRequest(result.message());
    }
}
