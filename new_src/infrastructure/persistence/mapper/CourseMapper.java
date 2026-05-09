package infrastructure.persistence.mapper;

import domain.course.Capacity;
import domain.course.Course;
import domain.course.CourseId;
import domain.course.Grade;
import domain.course.Lesson;
import domain.course.Room;
import domain.course.TimeSlot;
import domain.enums.DisciplineType;
import domain.enums.LessonType;
import domain.enums.WeekDay;
import domain.shared.Credits;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CourseMapper implements EntityMapper<Course, CourseId> {

    @Override public CourseId idOf(Course course) { return course.id(); }
    @Override public String idAsString(CourseId id) { return id.value(); }

    @Override public JsonValue toJson(Course course) {
        List<JsonValue> lessons = course.lessons().stream().map(this::lessonJson).toList();
        List<JsonValue> grades = course.allGrades().entrySet().stream().map(this::gradeJson).toList();
        return JsonObjectBuilder.create()
                .put("_id", course.id().value())
                .put("courseId", course.id().value())
                .put("name", course.name())
                .put("credits", course.credits().value())
                .put("type", course.type().name())
                .put("maxStudents", course.capacity().max())
                .putStrings("teachers", course.teachers().stream().map(Username::value).toList())
                .putStrings("students", course.students().stream().map(Username::value).toList())
                .putStrings("prerequisites", course.prerequisites().stream().map(CourseId::value).toList())
                .putObjects("lessons", lessons)
                .putObjects("grades", grades)
                .build();
    }

    private JsonValue lessonJson(Lesson l) {
        return JsonObjectBuilder.create()
                .put("type", l.type().name())
                .put("day", l.slot().day().name())
                .put("time", l.slot().time())
                .put("room", l.room().name())
                .build();
    }

    private JsonValue gradeJson(Map.Entry<Username, Grade> e) {
        return JsonObjectBuilder.create()
                .put("student", e.getKey().value())
                .put("att1", e.getValue().firstHalf())
                .put("att2", e.getValue().secondHalf())
                .put("exam", e.getValue().exam())
                .build();
    }

    @Override public Course fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        Course course = new Course(
                new CourseId(MapperHelpers.readString(o, "courseId")),
                MapperHelpers.readString(o, "name"),
                new Credits(MapperHelpers.readInt(o, "credits")),
                Enum.valueOf(DisciplineType.class, MapperHelpers.readString(o, "type")),
                new Capacity(MapperHelpers.readIntOr(o, "maxStudents", 1000)));
        for (String t : MapperHelpers.readStrings(o, "teachers")) course.assignTeacher(new Username(t));
        for (String s : MapperHelpers.readStrings(o, "students")) course.enroll(new Username(s));
        for (String p : MapperHelpers.readStrings(o, "prerequisites")) course.addPrerequisite(new CourseId(p));
        for (JsonValue lj : optionalArray(o, "lessons")) course.addLesson(lessonFrom(lj));
        for (JsonValue gj : optionalArray(o, "grades")) {
            JsonValue.JsonObject g = (JsonValue.JsonObject) gj;
            course.recordGrade(new Username(MapperHelpers.readString(g, "student")),
                    new Grade(MapperHelpers.readInt(g, "att1"),
                              MapperHelpers.readInt(g, "att2"),
                              MapperHelpers.readInt(g, "exam")));
        }
        return course;
    }

    private List<JsonValue> optionalArray(JsonValue.JsonObject o, String key) {
        JsonValue v = o.fields().get(key);
        return (v == null || v.isNull()) ? new ArrayList<>() : v.asArray();
    }

    private Lesson lessonFrom(JsonValue v) {
        JsonValue.JsonObject l = (JsonValue.JsonObject) v;
        return new Lesson(
                Enum.valueOf(LessonType.class, MapperHelpers.readString(l, "type")),
                new TimeSlot(Enum.valueOf(WeekDay.class, MapperHelpers.readString(l, "day")),
                        MapperHelpers.readString(l, "time")),
                new Room(MapperHelpers.readString(l, "room")));
    }
}
