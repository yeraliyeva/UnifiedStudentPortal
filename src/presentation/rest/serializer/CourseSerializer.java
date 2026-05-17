package presentation.rest.serializer;

import domain.course.Course;
import domain.course.Grade;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Converts domain Course objects to JSON for API responses. */
public final class CourseSerializer {

    private CourseSerializer() {}

    public static JsonValue toJson(Course course) {
        List<JsonValue> teachers = new ArrayList<>();
        for (Username u : course.teachers()) {
            teachers.add(JsonValue.of(u.value()));
        }
        List<JsonValue> students = new ArrayList<>();
        for (Username u : course.students()) {
            students.add(JsonValue.of(u.value()));
        }
        return JsonObjectBuilder.create()
                .put("id",             course.id().value())
                .put("name",           course.name())
                .put("credits",        course.credits().value())
                .put("type",           course.type().name())
                .put("capacity",       course.capacity().max())
                .put("remainingSeats", course.remainingSeats())
                .put("isFull",         course.isFull())
                .putObjects("teachers", teachers)
                .putObjects("students", students)
                .build();
    }

    public static JsonValue gradeToJson(Username student, Grade grade) {
        return JsonObjectBuilder.create()
                .put("student",          student.value())
                .put("firstHalf",        grade.firstHalf())
                .put("secondHalf",       grade.secondHalf())
                .put("exam",             grade.exam())
                .put("attestationTotal", grade.attestationTotal())
                .put("total",            grade.total())
                .put("letter",           grade.letter())
                .put("admittedToExam",   grade.isAdmittedToExam())
                .put("passing",          grade.isPassing())
                .build();
    }

    public static JsonValue gradesToJson(Map<Username, Grade> grades) {
        List<JsonValue> arr = new ArrayList<>();
        grades.forEach((u, g) -> arr.add(gradeToJson(u, g)));
        JsonValue.JsonArray array = new JsonValue.JsonArray(arr);
        return array;
    }
}
