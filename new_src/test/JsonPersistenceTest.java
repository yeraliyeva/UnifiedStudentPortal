package test;

import domain.course.Course;
import domain.course.CourseId;
import domain.library.Book;
import domain.library.BookId;
import domain.repository.BookRepository;
import domain.repository.CourseRepository;
import domain.repository.UserRepository;
import domain.user.Student;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.database.JsonFileDatabase;
import infrastructure.persistence.json.JsonReader;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.json.JsonWriter;
import infrastructure.persistence.orm.repository.OrmBookRepository;
import infrastructure.persistence.orm.repository.OrmCourseRepository;
import infrastructure.persistence.orm.repository.OrmUserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public final class JsonPersistenceTest {

    public static void runAll() {
        TestRunner.run("Json: roundtrip primitives + objects + arrays", JsonPersistenceTest::testRoundtrip);
        TestRunner.run("Persistence: User saved + reloaded keeps state", JsonPersistenceTest::testUserPersist);
        TestRunner.run("Persistence: Course saved + reloaded keeps lessons + grades", JsonPersistenceTest::testCoursePersist);
        TestRunner.run("Persistence: Book lend/return survives reload", JsonPersistenceTest::testBookPersist);
    }

    private static void testRoundtrip() {
        String src = "{\"name\":\"eve\",\"age\":21,\"isStudent\":true,\"tags\":[\"a\",\"b\"],\"nested\":{\"x\":1.5}}";
        JsonValue v = JsonReader.parse(src);
        String back = JsonWriter.write(v);
        JsonValue v2 = JsonReader.parse(back);
        Assert.equals("eve", v2.asObject().get("name").asString(), "Name preserved");
        Assert.equals(21, v2.asObject().get("age").asInt(), "Age preserved");
        Assert.isTrue(v2.asObject().get("isStudent").asBool(), "Bool preserved");
        Assert.equals(2, v2.asObject().get("tags").asArray().size(), "Array preserved");
    }

    private static Path freshDir(String name) throws IOException {
        Path p = Files.createTempDirectory("uni-" + name);
        return p;
    }

    private static void cleanup(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.delete(p); } catch (IOException ignored) {} });
        }
    }

    private static void testUserPersist() throws Exception {
        Path dir = freshDir("user");
        try {
            Database db = new JsonFileDatabase(dir);
            UserRepository repo = new OrmUserRepository(db);
            Student original = Fixtures.student("eve");
            original.recordCompletion(new CourseId("CRS-1"));
            original.recordFail();
            repo.save(original);

            UserRepository fresh = new OrmUserRepository(new JsonFileDatabase(dir));
            Student loaded = (Student) fresh.findByUsername(original.username()).orElseThrow();
            Assert.equals("eve", loaded.username().value(), "username persisted");
            Assert.isTrue(loaded.completedCourses().contains(new CourseId("CRS-1")), "completion persisted");
            Assert.equals(1, loaded.failCount(), "failCount persisted");
        } finally { cleanup(dir); }
    }

    private static void testCoursePersist() throws Exception {
        Path dir = freshDir("course");
        try {
            Database db = new JsonFileDatabase(dir);
            CourseRepository repo = new OrmCourseRepository(db);
            Course c = Fixtures.course("Math", 5, 30, 1);
            c.addPrerequisite(new CourseId("CRS-99"));
            repo.save(c);

            CourseRepository fresh = new OrmCourseRepository(new JsonFileDatabase(dir));
            Course loaded = fresh.findById(c.id()).orElseThrow();
            Assert.equals("Math", loaded.name(), "name persisted");
            Assert.isTrue(loaded.prerequisites().contains(new CourseId("CRS-99")), "prereqs persisted");
        } finally { cleanup(dir); }
    }

    private static void testBookPersist() throws Exception {
        Path dir = freshDir("book");
        try {
            Database db = new JsonFileDatabase(dir);
            BookRepository repo = new OrmBookRepository(db);
            Book book = new Book(new BookId(1), "Clean Code", "Robert Martin");
            book.lendTo(Fixtures.student("eve").username());
            repo.save(book);

            BookRepository fresh = new OrmBookRepository(new JsonFileDatabase(dir));
            Book loaded = fresh.findById(new BookId(1)).orElseThrow();
            Assert.isTrue(loaded.isBorrowed(), "borrowed flag persisted");
            Assert.equals("eve", loaded.borrower().orElseThrow().value(), "borrower persisted");
        } finally { cleanup(dir); }
    }
}
