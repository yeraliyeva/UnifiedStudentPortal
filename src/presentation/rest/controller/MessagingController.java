package presentation.rest.controller;

import application.Result;
import bootstrap.AppContext;
import domain.enums.HelpType;
import domain.enums.UrgencyLevel;
import domain.messaging.Message;
import domain.messaging.News;
import domain.messaging.Order;
import domain.messaging.Request;
import domain.shared.Username;
import domain.user.Employee;
import domain.user.User;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import presentation.rest.auth.RequestContext;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

import static presentation.rest.controller.ControllerUtils.*;

/**
 * Handles messaging, news, help requests, and IT order endpoints.
 */
public final class MessagingController {
    private final AppContext ctx;

    public MessagingController(AppContext ctx) {
        this.ctx = ctx;
    }

    // ── Messages ─────────────────────────────────────────────

    /** GET /api/messages/inbox */
    public HttpResponse inbox(HttpRequest request) {
        User user = RequestContext.current();
        List<Message> msgs = ctx.messageRepository.inboxOf(user.username());
        List<JsonValue> arr = new ArrayList<>();
        for (Message m : msgs) arr.add(messageToJson(m));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** POST /api/messages */
    public HttpResponse sendMessage(HttpRequest request) {
        User user = RequestContext.current();
        JsonValue.JsonObject body = request.body();
        String recipient = str(body, "recipient");
        String subject   = str(body, "subject");
        String msgBody   = str(body, "body");
        String urgency   = str(body, "urgency");

        try {
            UrgencyLevel level = UrgencyLevel.valueOf(urgency.toUpperCase());
            Result result = ctx.sendMessage.execute(user.username(), new Username(recipient),
                    subject, msgBody, level);
            return resultToResponse(result);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest("Invalid urgency. Use: LOW, MEDIUM, HIGH");
        }
    }

    // ── News ─────────────────────────────────────────────────

    /** GET /api/news */
    public HttpResponse listNews(HttpRequest request) {
        List<News> all = ctx.newsRepository.findAllSorted();
        List<JsonValue> arr = new ArrayList<>();
        for (News n : all) arr.add(newsToJson(n));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** POST /api/news — Employee only */
    public HttpResponse publishNews(HttpRequest request) {
        Employee author = (Employee) RequestContext.current();
        JsonValue.JsonObject body = request.body();
        String title   = str(body, "title");
        String content = str(body, "body");
        if (title.isBlank()) return HttpResponse.badRequest("'title' is required.");
        Result result = ctx.publishNews.execute(author.username(), title, content);
        return resultToResponse(result);
    }

    /** POST /api/news/{id}/comment */
    public HttpResponse commentOnNews(HttpRequest request) {
        User   user    = RequestContext.current();
        String idStr   = request.pathSegment(2).orElse("0");
        String comment = str(request.body(), "comment");
        Result result  = ctx.commentOnNews.execute(user, Integer.parseInt(idStr), comment);
        return resultToResponse(result);
    }

    // ── Help Requests ─────────────────────────────────────────

    /** GET /api/requests */
    public HttpResponse listRequests(HttpRequest request) {
        List<Request> all = ctx.requestRepository.findAll();
        List<JsonValue> arr = new ArrayList<>();
        for (Request r : all) arr.add(requestToJson(r));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** POST /api/requests */
    public HttpResponse submitRequest(HttpRequest request) {
        User user = RequestContext.current();
        JsonValue.JsonObject body = request.body();
        try {
            HelpType    type    = HelpType.valueOf(str(body, "type").toUpperCase());
            UrgencyLevel urgency = UrgencyLevel.valueOf(str(body, "urgency").toUpperCase());
            String info  = str(body, "info");
            Result result = ctx.submitRequest.execute(user, type, urgency, info);
            return resultToResponse(result);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest("Invalid type or urgency: " + e.getMessage());
        }
    }

    // ── IT Orders ─────────────────────────────────────────────

    /** GET /api/orders */
    public HttpResponse listOrders(HttpRequest request) {
        List<Order> all = ctx.orderRepository.findAll();
        List<JsonValue> arr = new ArrayList<>();
        for (Order o : all) arr.add(orderToJson(o));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** POST /api/orders */
    public HttpResponse createOrder(HttpRequest request) {
        User user = RequestContext.current();
        String desc = str(request.body(), "description");
        Result result = ctx.createOrder.execute(user, desc);
        return resultToResponse(result);
    }

    /** PUT /api/orders/{id}/accept — TechSupport only */
    public HttpResponse acceptOrder(HttpRequest request) {
        domain.user.TechSupport tech = (domain.user.TechSupport) RequestContext.current();
        int id = Integer.parseInt(request.pathSegment(2).orElse("0"));
        Result result = ctx.acceptOrder.execute(tech, id);
        return resultToResponse(result);
    }

    /** PUT /api/orders/{id}/complete — TechSupport only */
    public HttpResponse completeOrder(HttpRequest request) {
        domain.user.TechSupport tech = (domain.user.TechSupport) RequestContext.current();
        int id = Integer.parseInt(request.pathSegment(2).orElse("0"));
        Result result = ctx.completeOrder.execute(tech, id);
        return resultToResponse(result);
    }

    // ── Serializers ───────────────────────────────────────────

    private static JsonValue messageToJson(Message m) {
        return JsonObjectBuilder.create()
                .put("id",       m.id())
                .put("sender",   m.sender().value())
                .put("subject",  m.subject())
                .put("urgency",  m.urgency().name())
                .put("status",   m.status().name())
                .put("sentAt",   m.sentAt().toString())
                .build();
    }

    private static JsonValue newsToJson(News n) {
        return JsonObjectBuilder.create()
                .put("id",     n.id())
                .put("title",  n.title())
                .put("body",   n.body())
                .put("author", n.author().value())
                .put("pinned", n.isPinned())
                .build();
    }

    private static JsonValue requestToJson(Request r) {
        return JsonObjectBuilder.create()
                .put("id",        r.id())
                .put("requester", r.requester().value())
                .put("type",      r.type().name())
                .put("urgency",   r.urgency().name())
                .put("status",    r.status().name())
                .build();
    }

    private static JsonValue orderToJson(Order o) {
        return JsonObjectBuilder.create()
                .put("id",          o.id())
                .put("requester",   o.requester().value())
                .put("description", o.description())
                .put("status",      o.status().name())
                .build();
    }

    private static HttpResponse resultToResponse(Result result) {
        if (result.success())
            return HttpResponse.ok(JsonObjectBuilder.create().put("message", result.message()).build());
        return HttpResponse.badRequest(result.message());
    }
}
