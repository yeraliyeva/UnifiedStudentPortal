package presentation.rest.controller;

import application.Result;
import bootstrap.AppContext;
import domain.enums.PaperFormat;
import domain.research.ResearchPaper;
import domain.research.ResearchProject;
import domain.shared.Username;
import domain.user.User;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import presentation.rest.auth.RequestContext;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

import static presentation.rest.controller.ControllerUtils.*;

/** Handles research paper publication, project management, subscriptions, and citation generation. */
public final class ResearchController {
    private final AppContext ctx;

    public ResearchController(AppContext ctx) {
        this.ctx = ctx;
    }

    /** GET /api/papers */
    public HttpResponse listPapers(HttpRequest request) {
        List<JsonValue> arr = new ArrayList<>();
        for (ResearchPaper p : ctx.paperRepository.findAll()) arr.add(paperToJson(p));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** POST /api/papers */
    public HttpResponse publishPaper(HttpRequest request) {
        User user = RequestContext.current();
        JsonValue.JsonObject body = request.body();
        String title    = str(body, "title");
        String journal  = str(body, "journal");
        String abstract_ = str(body, "abstract");
        int pages       = intVal(body, "pages");
        String doi      = str(body, "doi");

        if (title.isBlank() || journal.isBlank())
            return HttpResponse.badRequest("'title' and 'journal' are required.");

        Result result = ctx.publishPaper.execute(user, title, journal, abstract_, pages, doi);
        return resultToResponse(result);
    }

    /** GET /api/papers/{id}/cite?format=PLAIN_TEXT|BIBTEX */
    public HttpResponse getCitation(HttpRequest request) {
        String idStr    = request.pathSegment(2).orElse("0");
        String formatStr = request.pathSegment(4)
                .or(() -> request.header("X-Citation-Format"))
                .orElse("PLAIN_TEXT");
        try {
            PaperFormat format = PaperFormat.valueOf(formatStr.toUpperCase());
            var paperId = new domain.research.PaperId(Integer.parseInt(idStr));
            Result result = ctx.generateCitation.execute(paperId, format);
            if (!result.success()) return HttpResponse.badRequest(result.message());
            return HttpResponse.ok(JsonObjectBuilder.create().put("citation", result.message()).build());
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest("Invalid format. Use: PLAIN_TEXT, BIBTEX");
        }
    }

    /** GET /api/projects */
    public HttpResponse listProjects(HttpRequest request) {
        List<JsonValue> arr = new ArrayList<>();
        for (ResearchProject p : ctx.projectRepository.findAll()) arr.add(projectToJson(p));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** POST /api/projects */
    public HttpResponse createProject(HttpRequest request) {
        User user = RequestContext.current();
        JsonValue.JsonObject body = request.body();
        String topic   = str(body, "topic");
        String journal = str(body, "journal");
        if (topic.isBlank() || journal.isBlank())
            return HttpResponse.badRequest("'topic' and 'journal' are required.");
        Result result = ctx.createProject.execute(user, journal, topic);
        return resultToResponse(result);
    }

    /** POST /api/projects/{journal}/join */
    public HttpResponse joinProject(HttpRequest request) {
        User   user    = RequestContext.current();
        String journal = request.pathSegment(2).orElse("");
        Result result  = ctx.joinResearchProject.execute(user, journal);
        return resultToResponse(result);
    }

    /** POST /api/subscriptions */
    public HttpResponse subscribe(HttpRequest request) {
        User   user    = RequestContext.current();
        String journal = str(request.body(), "journal");
        Result result  = ctx.subscribeToJournal.execute(user, journal);
        return resultToResponse(result);
    }

    /** DELETE /api/subscriptions/{journal} */
    public HttpResponse unsubscribe(HttpRequest request) {
        User   user    = RequestContext.current();
        String journal = request.pathSegment(2).orElse("");
        Result result  = ctx.unsubscribeFromJournal.execute(user, journal);
        return resultToResponse(result);
    }

    /** POST /api/research/become */
    public HttpResponse becomeResearcher(HttpRequest request) {
        User user = RequestContext.current();
        String field = str(request.body(), "field");
        if (field.isBlank()) return HttpResponse.badRequest("Field is required.");
        Result result = ctx.becomeResearcher.execute(user, field);
        if (result.success()) {
            ctx.userRepository.save(user);
        }
        return resultToResponse(result);
    }

    private JsonValue paperToJson(ResearchPaper p) {
        JsonObjectBuilder b = JsonObjectBuilder.create()
                .put("id",            p.id().value())
                .put("title",         p.title())
                .put("author",        p.author().value())
                .put("journal",       p.journal().value())
                .put("pages",         p.length())
                .put("citations",     p.citations())
                .put("doi",           p.doi())
                .put("publishedDate", p.publishedDate().toString());
        ctx.userRepository.findByUsername(p.author())
                .ifPresent(u -> b.put("authorFullName", u.name().first() + " " + u.name().last()));
        return b.build();
    }

    private JsonValue projectToJson(ResearchProject p) {
        List<JsonValue> participants = new ArrayList<>();
        for (Username u : p.participants()) participants.add(JsonValue.of(u.value()));
        JsonObjectBuilder b = JsonObjectBuilder.create()
                .put("id",         p.id())
                .put("topic",      p.topic())
                .put("journal",    p.journal().value())
                .put("supervisor", p.supervisor() != null ? p.supervisor().value() : "")
                .putObjects("participants", participants);
        if (p.supervisor() != null) {
            ctx.userRepository.findByUsername(p.supervisor())
                    .ifPresent(u -> b.put("supervisorFullName", u.name().first() + " " + u.name().last()));
        }
        return b.build();
    }

    private static HttpResponse resultToResponse(Result result) {
        if (result.success())
            return HttpResponse.ok(JsonObjectBuilder.create().put("message", result.message()).build());
        return HttpResponse.badRequest(result.message());
    }
}
