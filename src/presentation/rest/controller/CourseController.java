package presentation.rest.controller;

import application.Result;
import application.usecase.course.ViewTranscript;
import bootstrap.AppContext;
import domain.course.CourseId;
import domain.course.Grade;
import domain.enums.DisciplineType;
import domain.shared.Username;
import domain.user.Manager;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.User;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import presentation.rest.auth.RequestContext;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;
import presentation.rest.serializer.CourseSerializer;

import java.util.ArrayList;
import java.util.List;

import static presentation.rest.controller.ControllerUtils.*;

/**
 * Handles course management endpoints.
 *
 * <p>Routes are role-specific:
 * <ul>
 *   <li>GET /api/courses — any authenticated user</li>
 *   <li>POST /api/courses — Manager only</li>
 *   <li>POST /api/courses/{id}/enroll — Student only</li>
 *   <li>POST /api/courses/{id}/drop — Student only</li>
 *   <li>POST /api/courses/{id}/marks — Teacher only</li>
 *   <li>GET  /api/courses/{id}/transcript — Student only</li>
 * </ul>
 */
public final class CourseController {
    private final AppContext ctx;

    public CourseController(AppContext ctx) {
        this.ctx = ctx;
    }

    /** GET /api/courses */
    public HttpResponse listCourses(HttpRequest request) {
        List<JsonValue> arr = new ArrayList<>();
        for (var c : ctx.courseRepository.findAll()) arr.add(CourseSerializer.toJson(c));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** GET /api/courses/{id} */
    public HttpResponse getCourse(HttpRequest request) {
        String id = request.pathSegment(2).orElse("");
        return ctx.courseRepository.findById(new CourseId(id))
                .map(c -> HttpResponse.ok(CourseSerializer.toJson(c)))
                .orElse(HttpResponse.notFound("Course not found: " + id));
    }

    /** POST /api/courses — Manager only */
    public HttpResponse createCourse(HttpRequest request) {
        Manager actor = (Manager) RequestContext.current();
        JsonValue.JsonObject body = request.body();
        String name     = str(body, "name");
        int credits     = intVal(body, "credits");
        String typeStr  = str(body, "type");
        int capacity    = intVal(body, "capacity");

        if (name.isBlank()) return HttpResponse.badRequest("'name' is required.");
        try {
            DisciplineType type = DisciplineType.valueOf(typeStr.toUpperCase());
            var course = ctx.createCourse.execute(actor.username(), name, credits, type, capacity);
            return HttpResponse.created(CourseSerializer.toJson(course));
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest("Invalid type. Use: MAJOR, MINOR, FREE");
        }
    }

    /** POST /api/courses/{id}/enroll — Student only */
    public HttpResponse enroll(HttpRequest request) {
        Student student  = (Student) RequestContext.current();
        String  courseId = request.pathSegment(2).orElse("");
        Result  result   = ctx.enrollInCourse.execute(student, new CourseId(courseId));
        return resultToResponse(result);
    }

    /** POST /api/courses/{id}/drop — Student only */
    public HttpResponse drop(HttpRequest request) {
        Student student  = (Student) RequestContext.current();
        String  courseId = request.pathSegment(2).orElse("");
        Result  result   = ctx.dropCourse.execute(student, new CourseId(courseId));
        return resultToResponse(result);
    }

    /** POST /api/courses/{id}/marks — Teacher only */
    public HttpResponse recordMarks(HttpRequest request) {
        Teacher teacher  = (Teacher) RequestContext.current();
        String  courseId = request.pathSegment(2).orElse("");
        JsonValue.JsonObject body = request.body();

        String studentUsername = str(body, "studentUsername");
        int firstHalf  = intVal(body, "firstHalf");
        int secondHalf = intVal(body, "secondHalf");
        int exam       = intVal(body, "exam");

        Grade grade  = new Grade(firstHalf, secondHalf, exam);
        Result result = ctx.recordMarks.execute(teacher, new CourseId(courseId),
                new Username(studentUsername), grade);
        return resultToResponse(result);
    }

    /** GET /api/courses/{id}/grades — Teacher only */
    public HttpResponse viewGrades(HttpRequest request) {
        String courseId = request.pathSegment(2).orElse("");
        return ctx.courseRepository.findById(new CourseId(courseId))
                .map(c -> HttpResponse.ok(CourseSerializer.gradesToJson(c.allGrades())))
                .orElse(HttpResponse.notFound("Course not found."));
    }

    /** GET /api/transcript — Student only */
    public HttpResponse transcript(HttpRequest request) {
        Student student = (Student) RequestContext.current();
        ViewTranscript.Transcript t = ctx.viewTranscript.execute(student);

        List<JsonValue> lines = new ArrayList<>();
        for (var line : t.lines()) {
            lines.add(JsonObjectBuilder.create()
                    .put("course", line.courseName())
                    .put("letter", line.letter())
                    .put("total",  line.total())
                    .build());
        }
        return HttpResponse.ok(JsonObjectBuilder.create()
                .put("student",   t.fullName())
                .put("degree",    t.degreeType())
                .put("year",      t.year())
                .put("failCount", t.failCount())
                .put("gpa",       t.gpa())
                .putObjects("courses", lines)
                .build());
    }

    // ── Helpers ──────────────────────────────────────────────

    private static HttpResponse resultToResponse(Result result) {
        if (result.success())
            return HttpResponse.ok(JsonObjectBuilder.create().put("message", result.message()).build());
        return HttpResponse.badRequest(result.message());
    }
}
