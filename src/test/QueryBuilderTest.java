package test;

import domain.library.Book;
import domain.library.BookId;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.database.JsonFileDatabase;
import infrastructure.persistence.mapper.BookMapper;
import infrastructure.persistence.orm.Op;
import infrastructure.persistence.orm.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class QueryBuilderTest {

    public static void runAll() {
        TestRunner.run("QueryBuilder: where EQ filters by exact field", QueryBuilderTest::testWhereEq);
        TestRunner.run("QueryBuilder: where CONTAINS does substring search", QueryBuilderTest::testContains);
        TestRunner.run("QueryBuilder: orderBy + limit + offset paginate", QueryBuilderTest::testOrderLimitOffset);
        TestRunner.run("QueryBuilder: count returns matching size", QueryBuilderTest::testCount);
        TestRunner.run("QueryBuilder: first returns Optional empty when no match", QueryBuilderTest::testFirstEmpty);
        TestRunner.run("QueryBuilder: deleteAll removes only matching rows", QueryBuilderTest::testDeleteAll);
        TestRunner.run("QueryBuilder: whereMatch chains a typed predicate", QueryBuilderTest::testWhereMatch);
        TestRunner.run("Repository: create + find + delete aliases work", QueryBuilderTest::testAliases);
    }

    private static Path freshDir(String name) throws IOException { return Files.createTempDirectory("uni-qb-" + name); }
    private static void cleanup(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.delete(p); } catch (IOException ignored) {} });
        }
    }

    private static Repository<Book, BookId> seed(Database db) {
        Repository<Book, BookId> repo = new Repository<>(db, "books", new BookMapper());
        repo.create(new Book(new BookId(1), "Clean Code", "Robert Martin"));
        repo.create(new Book(new BookId(2), "Effective Java", "Joshua Bloch"));
        repo.create(new Book(new BookId(3), "Refactoring", "Martin Fowler"));
        repo.create(new Book(new BookId(4), "Clean Architecture", "Robert Martin"));
        return repo;
    }

    private static void testWhereEq() throws Exception {
        Path dir = freshDir("eq");
        try {
            Repository<Book, BookId> repo = seed(new JsonFileDatabase(dir));
            List<Book> martin = repo.where("author", Op.EQ, "Robert Martin").list();
            Assert.equals(2, martin.size(), "Should match 2 Robert Martin books");
        } finally { cleanup(dir); }
    }

    private static void testContains() throws Exception {
        Path dir = freshDir("contains");
        try {
            Repository<Book, BookId> repo = seed(new JsonFileDatabase(dir));
            List<Book> clean = repo.where("title", Op.CONTAINS, "Clean").list();
            Assert.equals(2, clean.size(), "Should match Clean Code + Clean Architecture");
        } finally { cleanup(dir); }
    }

    private static void testOrderLimitOffset() throws Exception {
        Path dir = freshDir("page");
        try {
            Repository<Book, BookId> repo = seed(new JsonFileDatabase(dir));
            List<Book> page = repo.select()
                    .orderBy(Comparator.comparing(Book::title))
                    .limit(2)
                    .offset(1)
                    .list();
            Assert.equals(2, page.size(), "Should return 2 rows");
            Assert.equals("Clean Code", page.get(0).title(), "First row after offset 1");
            Assert.equals("Effective Java", page.get(1).title(), "Second row");
        } finally { cleanup(dir); }
    }

    private static void testCount() throws Exception {
        Path dir = freshDir("count");
        try {
            Repository<Book, BookId> repo = seed(new JsonFileDatabase(dir));
            Assert.equals(4, repo.count(), "Total count");
            Assert.equals(2, repo.where("author", Op.EQ, "Robert Martin").count(), "Filtered count");
        } finally { cleanup(dir); }
    }

    private static void testFirstEmpty() throws Exception {
        Path dir = freshDir("first");
        try {
            Repository<Book, BookId> repo = seed(new JsonFileDatabase(dir));
            Assert.isFalse(repo.where("author", Op.EQ, "Nobody").first().isPresent(), "No author -> empty");
            Assert.isTrue(repo.where("author", Op.EQ, "Joshua Bloch").first().isPresent(), "Match -> present");
        } finally { cleanup(dir); }
    }

    private static void testDeleteAll() throws Exception {
        Path dir = freshDir("delete");
        try {
            Repository<Book, BookId> repo = seed(new JsonFileDatabase(dir));
            int removed = repo.where("author", Op.EQ, "Robert Martin").deleteAll();
            Assert.equals(2, removed, "Should report 2 deletions");
            Assert.equals(2, repo.count(), "Two left after delete");
            Assert.isFalse(repo.where("author", Op.EQ, "Robert Martin").exists(), "Robert Martin gone");
        } finally { cleanup(dir); }
    }

    private static void testWhereMatch() throws Exception {
        Path dir = freshDir("match");
        try {
            Repository<Book, BookId> repo = seed(new JsonFileDatabase(dir));
            List<Book> longTitles = repo.select()
                    .whereMatch(b -> b.title().length() > 10)
                    .orderBy(Comparator.comparing(Book::title))
                    .list();
            Assert.isTrue(longTitles.size() >= 2, "Should find books with title length > 10");
        } finally { cleanup(dir); }
    }

    private static void testAliases() throws Exception {
        Path dir = freshDir("aliases");
        try {
            Repository<Book, BookId> repo = new Repository<>(new JsonFileDatabase(dir), "books", new BookMapper());
            Book b = repo.create(new Book(new BookId(1), "Domain Driven Design", "Eric Evans"));
            Assert.isTrue(repo.find(b.id()).isPresent(), "find(id) returns saved book");
            repo.delete(b);
            Assert.isFalse(repo.find(b.id()).isPresent(), "delete(entity) removes by id");
        } finally { cleanup(dir); }
    }
}
