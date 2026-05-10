package presentation.rest.controller;

import application.Result;
import bootstrap.AppContext;
import domain.library.Book;
import domain.library.BookId;
import domain.user.User;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import presentation.rest.auth.RequestContext;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

import static presentation.rest.controller.ControllerUtils.*;

/** Handles book listing, addition, removal, borrowing, and returning. */
public final class LibraryController {
    private final AppContext ctx;

    public LibraryController(AppContext ctx) {
        this.ctx = ctx;
    }

    /** GET /api/books */
    public HttpResponse listBooks(HttpRequest request) {
        List<JsonValue> arr = new ArrayList<>();
        for (Book b : ctx.bookRepository.findAll()) arr.add(bookToJson(b));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** POST /api/books — Librarian only */
    public HttpResponse addBook(HttpRequest request) {
        domain.user.Librarian actor = (domain.user.Librarian) RequestContext.current();
        JsonValue.JsonObject body = request.body();
        String title  = str(body, "title");
        String author = str(body, "author");
        if (title.isBlank()) return HttpResponse.badRequest("'title' is required.");
        Result result = ctx.addBook.execute(actor.username(), title, author);
        return resultToResponse(result);
    }

    /** DELETE /api/books/{title} — Librarian only */
    public HttpResponse removeBook(HttpRequest request) {
        domain.user.Librarian actor = (domain.user.Librarian) RequestContext.current();
        String title = request.pathSegment(2).orElse("");
        Result result = ctx.removeBook.execute(actor.username(), title);
        return resultToResponse(result);
    }

    /** POST /api/books/{title}/borrow */
    public HttpResponse borrowBook(HttpRequest request) {
        User   user  = RequestContext.current();
        String title = request.pathSegment(2).orElse("");
        Result result = ctx.borrowBook.execute(user, title);
        return resultToResponse(result);
    }

    /** POST /api/books/{title}/return */
    public HttpResponse returnBook(HttpRequest request) {
        User user  = RequestContext.current();
        String title = request.pathSegment(2).orElse("");
        Result result = ctx.returnBook.execute(user, title);
        return resultToResponse(result);
    }


    private static JsonValue bookToJson(Book b) {
        return JsonObjectBuilder.create()
                .put("id",       b.id().value())
                .put("title",    b.title())
                .put("author",   b.author())
                .put("borrowed", b.isBorrowed())
                .put("borrowedBy", b.borrower().map(u -> u.value()).orElse(""))
                .build();
    }

    private static HttpResponse resultToResponse(Result result) {
        if (result.success())
            return HttpResponse.ok(JsonObjectBuilder.create().put("message", result.message()).build());
        return HttpResponse.badRequest(result.message());
    }
}
