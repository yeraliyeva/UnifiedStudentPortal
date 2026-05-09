package infrastructure.persistence.mapper;

import domain.library.Book;
import domain.library.BookId;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

public final class BookMapper implements EntityMapper<Book, BookId> {

    @Override public BookId idOf(Book book) { return book.id(); }
    @Override public String idAsString(BookId id) { return Integer.toString(id.value()); }

    @Override public JsonValue toJson(Book book) {
        JsonObjectBuilder b = JsonObjectBuilder.create()
                .put("_id", Integer.toString(book.id().value()))
                .put("id", book.id().value())
                .put("title", book.title())
                .put("author", book.author());
        book.borrower().ifPresent(u -> b.put("borrower", u.value()));
        return b.build();
    }

    @Override public Book fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        Book book = new Book(new BookId(MapperHelpers.readInt(o, "id")),
                MapperHelpers.readString(o, "title"),
                MapperHelpers.readString(o, "author"));
        String b = MapperHelpers.readString(o, "borrower");
        if (b != null) book.lendTo(new Username(b));
        return book;
    }
}
